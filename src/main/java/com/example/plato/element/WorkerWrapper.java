package com.example.plato.element;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import com.example.plato.handler.AfterHandler;
import com.example.plato.handler.INodeWork;
import com.example.plato.handler.PreHandler;
import com.example.plato.runningData.ResultState;
import com.example.plato.runningData.WorkResult;

/**
 * 对每个worker及callback进行包装，一对一
 */
public class WorkerWrapper<T, V> {

    private String uniqueId;
    private T param;
    private INodeWork<T, V> iNodeWork;
    private List<WorkerWrapper<?, ?>> nextWrappers;
    private List<PrePlatoNodeProxy> dependWrappers;
    private AfterHandler afterHandler;
    private PreHandler<T> preHandler;
    private AtomicInteger state = new AtomicInteger(0);
    private Map<String, WorkerWrapper> forParamUseWrappers;
    private volatile WorkResult<V> workResult = WorkResult.defaultResult();
    private volatile boolean needCheckNextWrapperResult = true;

    private static final int FINISH = 1;
    private static final int ERROR = 2;
    private static final int WORKING = 3;
    private static final int INIT = 0;

    private WorkerWrapper(String uniqueId, INodeWork<T, V> iNodeWork, AfterHandler afterHandler,
            PreHandler preHandler) {
        if (iNodeWork == null) {
            throw new NullPointerException("async.worker is null");
        }
        this.iNodeWork = iNodeWork;
        this.uniqueId = uniqueId;
        this.afterHandler = afterHandler;
        this.preHandler = preHandler;
    }

    public void work(ExecutorService executorService, WorkerWrapper fromWrapper,
            Map<String, WorkerWrapper> forParamUseWrappers) {
        System.out.println(Thread.currentThread().getName());
        this.forParamUseWrappers = forParamUseWrappers;
        forParamUseWrappers.put(uniqueId, this);
        if (getState() == FINISH || getState() == ERROR) {
            beginNext(executorService);
            return;
        }
        if (needCheckNextWrapperResult) {
            if (!checkNextWrapperResult()) {
                fastFail(INIT, new RuntimeException());
                beginNext(executorService);
                return;
            }
        }
        if (dependWrappers == null || dependWrappers.size() == 0) {
            fire(fromWrapper);
            beginNext(executorService);
            return;
        }
        if (dependWrappers.size() == 1) {
            doDependsOneJob(fromWrapper);
            beginNext(executorService);
        } else {
            doDependsJobs(executorService, dependWrappers, fromWrapper);
        }
    }


    public void work(ExecutorService executorService, Map<String, WorkerWrapper> forParamUseWrappers) {
        work(executorService, null, forParamUseWrappers);
    }

    public void stopNow() {
        if (getState() == INIT || getState() == WORKING) {
            fastFail(getState(), null);
        }
    }

    private boolean checkNextWrapperResult() {
        //如果自己就是最后一个，或者后面有并行的多个，就返回true
        if (nextWrappers == null || nextWrappers.size() != 1) {
            return getState() == INIT;
        }
        WorkerWrapper nextWrapper = nextWrappers.get(0);
        boolean state = nextWrapper.getState() == INIT;
        //继续校验自己的next的状态
        return state && nextWrapper.checkNextWrapperResult();
    }

    /**
     * 进行下一个任务
     */
    private void beginNext(ExecutorService executorService) {
        //花费的时间
        if (nextWrappers == null) {
            return;
        }
        if (nextWrappers.size() == 1) {
            nextWrappers.get(0).work(executorService, WorkerWrapper.this, forParamUseWrappers);
            return;
        }
        CompletableFuture[] futures = new CompletableFuture[nextWrappers.size()];
        for (int i = 0; i < nextWrappers.size(); i++) {
            int finalI = i;
            futures[i] = CompletableFuture.runAsync(() -> nextWrappers.get(finalI)
                    .work(executorService, WorkerWrapper.this, forParamUseWrappers), executorService);
        }
        try {
            CompletableFuture.allOf(futures).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void doDependsOneJob(WorkerWrapper dependWrapper) {
        if (ResultState.TIMEOUT == dependWrapper.getWorkResult().getResultState()) {
            workResult = defaultResult();
            fastFail(INIT, null);
        } else if (ResultState.EXCEPTION == dependWrapper.getWorkResult().getResultState()) {
            workResult = defaultExResult(dependWrapper.getWorkResult().getEx());
            fastFail(INIT, null);
        } else {
            //前面任务正常完毕了，该自己了
            fire(dependWrapper);
        }
    }

    private synchronized void doDependsJobs(ExecutorService executorService, List<PrePlatoNodeProxy> dependWrappers,
            WorkerWrapper fromWrapper) {
        boolean nowDependIsMust = false;
        //创建必须完成的上游wrapper集合
        Set<PrePlatoNodeProxy> mustWrapper = new HashSet<>();
        for (PrePlatoNodeProxy dependWrapper : dependWrappers) {
            if (dependWrapper.isMust()) {
                mustWrapper.add(dependWrapper);
            }
            if (dependWrapper.getWorkerWrapper().equals(fromWrapper)) {
                nowDependIsMust = dependWrapper.isMust();
            }
        }

        //如果全部是不必须的条件，那么只要到了这里，就执行自己。
        if (mustWrapper.size() == 0) {
            if (ResultState.TIMEOUT == fromWrapper.getWorkResult().getResultState()) {
                fastFail(INIT, null);
            } else {
                fire(fromWrapper);
            }
            beginNext(executorService);
            return;
        }

        //如果存在需要必须完成的，且fromWrapper不是必须的，就什么也不干
        if (!nowDependIsMust) {
            return;
        }

        //如果fromWrapper是必须的
        boolean existNoFinish = false;
        boolean hasError = false;
        //先判断前面必须要执行的依赖任务的执行结果，如果有任何一个失败，那就不用走action了，直接给自己设置为失败，进行下一步就是了
        for (PrePlatoNodeProxy dependWrapper : mustWrapper) {
            WorkerWrapper<?, ?> workerWrapper = dependWrapper.getWorkerWrapper();
            WorkResult tempWorkResult = workerWrapper.getWorkResult();
            //为null或者isWorking，说明它依赖的某个任务还没执行到或没执行完
            if (workerWrapper.getState() == INIT || workerWrapper.getState() == WORKING) {
                existNoFinish = true;
                break;
            }
            if (ResultState.TIMEOUT == tempWorkResult.getResultState()) {
                workResult = defaultResult();
                hasError = true;
                break;
            }
            if (ResultState.EXCEPTION == tempWorkResult.getResultState()) {
                workResult = defaultExResult(workerWrapper.getWorkResult().getEx());
                hasError = true;
                break;
            }

        }
        //只要有失败的
        if (hasError) {
            fastFail(INIT, null);
            beginNext(executorService);
            return;
        }

        //如果上游都没有失败，分为两种情况，一种是都finish了，一种是有的在working
        //都finish的话
        if (!existNoFinish) {
            //上游都finish了，进行自己
            fire(fromWrapper);
            beginNext(executorService);
            return;
        }
    }

    /**
     * 执行自己的job.具体的执行是在另一个线程里,但判断阻塞超时是在work线程
     */
    private void fire(WorkerWrapper fromWrapper) {
        //阻塞取结果
        workResult = workerDoJob(fromWrapper);
    }

    /**
     * 快速失败
     */
    private boolean fastFail(int expect, Exception e) {
        //试图将它从expect状态,改成Error
        if (!compareAndSetState(expect, ERROR)) {
            return false;
        }
        //尚未处理过结果
        if (checkIsNullResult()) {
            if (e == null) {
                workResult = defaultResult();
            } else {
                workResult = defaultExResult(e);
            }
        }
        iNodeWork.hook(param, workResult);
        return true;
    }

    /**
     * 具体的单个worker执行任务
     */
    private WorkResult<V> workerDoJob(WorkerWrapper fromWrapper) {
        //避免重复执行
        if (!checkIsNullResult()) {
            return workResult;
        }
        try {
            //如果已经不是init状态了，说明正在被执行或已执行完毕。这一步很重要，可以保证任务不被重复执行
            if (!compareAndSetState(INIT, WORKING)) {
                return workResult;
            }

            if (fromWrapper != null) {
                this.param = getHandlerParam();
            }

            //执行耗时操作
            V resultValue = iNodeWork.work(param);

            //如果状态不是在working,说明别的地方已经修改了
            if (!compareAndSetState(WORKING, FINISH)) {
                return workResult;
            }

            workResult.setResultState(ResultState.SUCCESS);
            workResult.setResult(resultValue);
            //回调成功
            iNodeWork.hook(param, workResult);

            return workResult;
        } catch (Exception e) {
            //避免重复回调
            if (!checkIsNullResult()) {
                return workResult;
            }
            fastFail(WORKING, e);
            return workResult;
        }
    }

    private T getHandlerParam() {
        if (this.preHandler == null) {
            return null;
        }
        T p = this.preHandler.paramHandle(forParamUseWrappers);
        return p;
    }

    public WorkResult<V> getWorkResult() {
        return workResult;
    }

    public List<WorkerWrapper<?, ?>> getNextWrappers() {
        return nextWrappers;
    }

    public void setParam(T param) {
        this.param = param;
    }

    private boolean checkIsNullResult() {
        return ResultState.DEFAULT == workResult.getResultState();
    }

    private void addDepend(WorkerWrapper<?, ?> workerWrapper, boolean must) {
        addDepend(new PrePlatoNodeProxy(workerWrapper, must));
    }

    private void addDepend(PrePlatoNodeProxy dependWrapper) {
        if (dependWrappers == null) {
            dependWrappers = new ArrayList<>();
        }
        //如果依赖的是重复的同一个，就不重复添加了
        for (PrePlatoNodeProxy wrapper : dependWrappers) {
            if (wrapper.equals(dependWrapper)) {
                return;
            }
        }
        dependWrappers.add(dependWrapper);
    }

    private void addNext(WorkerWrapper<?, ?> workerWrapper) {
        if (nextWrappers == null) {
            nextWrappers = new ArrayList<>();
        }
        //避免添加重复
        for (WorkerWrapper wrapper : nextWrappers) {
            if (workerWrapper.equals(wrapper)) {
                return;
            }
        }
        nextWrappers.add(workerWrapper);
    }

    private void addNextWrappers(List<WorkerWrapper<?, ?>> wrappers) {
        if (wrappers == null) {
            return;
        }
        for (WorkerWrapper<?, ?> wrapper : wrappers) {
            addNext(wrapper);
        }
    }

    private void addDependWrappers(List<PrePlatoNodeProxy> dependWrappers) {
        if (dependWrappers == null) {
            return;
        }
        for (PrePlatoNodeProxy wrapper : dependWrappers) {
            addDepend(wrapper);
        }
    }

    private WorkResult<V> defaultResult() {
        workResult.setResultState(ResultState.TIMEOUT);
        workResult.setResult(null);
        return workResult;
    }

    private WorkResult<V> defaultExResult(Exception ex) {
        workResult.setResultState(ResultState.EXCEPTION);
        workResult.setResult(null);
        workResult.setEx(ex);
        return workResult;
    }


    private int getState() {
        return state.get();
    }

    public String getUniqueId() {
        return uniqueId;
    }

    private boolean compareAndSetState(int expect, int update) {
        return this.state.compareAndSet(expect, update);
    }

    private void setNeedCheckNextWrapperResult(boolean needCheckNextWrapperResult) {
        this.needCheckNextWrapperResult = needCheckNextWrapperResult;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WorkerWrapper<?, ?> that = (WorkerWrapper<?, ?>) o;
        return needCheckNextWrapperResult == that.needCheckNextWrapperResult &&
                Objects.equals(param, that.param) &&
                Objects.equals(iNodeWork, that.iNodeWork) &&
                Objects.equals(nextWrappers, that.nextWrappers) &&
                Objects.equals(dependWrappers, that.dependWrappers) &&
                Objects.equals(state, that.state) &&
                Objects.equals(workResult, that.workResult);
    }

    @Override
    public int hashCode() {
        return Objects.hash(param, iNodeWork, nextWrappers, dependWrappers, state, workResult,
                needCheckNextWrapperResult);
    }

    public static class Builder<W, C> {

        private AfterHandler afterHandler;
        private PreHandler<W> preHandler;
        private String uniqueId;
        private W param;
        private INodeWork<W, C> worker;
        private List<WorkerWrapper<?, ?>> nextWrappers;
        private List<PrePlatoNodeProxy> dependWrappers;
        private Set<WorkerWrapper<?, ?>> selfIsMustSet;
        private boolean needCheckNextWrapperResult = true;

        public Builder<W, C> setAfterHandler(AfterHandler afterHandler) {
            this.afterHandler = afterHandler;
            return this;
        }

        public Builder<W, C> setPreHandler(PreHandler preHandler) {
            this.preHandler = preHandler;
            return this;
        }

        public Builder<W, C> setINodeWork(INodeWork<W, C> worker) {
            this.worker = worker;
            return this;
        }

        public Builder<W, C> setUniqueId(String uniqueId) {
            if (uniqueId != null) {
                this.uniqueId = uniqueId;
            }
            return this;
        }

        public Builder<W, C> needCheckNextWrapperResult(boolean needCheckNextWrapperResult) {
            this.needCheckNextWrapperResult = needCheckNextWrapperResult;
            return this;
        }

        public Builder<W, C> depend(WorkerWrapper<?, ?>... wrappers) {
            if (wrappers == null) {
                return this;
            }
            for (WorkerWrapper<?, ?> wrapper : wrappers) {
                depend(wrapper);
            }
            return this;
        }

        public Builder<W, C> depend(WorkerWrapper<?, ?> wrapper) {
            return depend(wrapper, true);
        }

        public Builder<W, C> depend(WorkerWrapper<?, ?> wrapper, boolean isMust) {
            if (wrapper == null) {
                return this;
            }
            PrePlatoNodeProxy dependWrapper = new PrePlatoNodeProxy(wrapper, isMust);
            if (dependWrappers == null) {
                dependWrappers = new ArrayList<>();
            }
            dependWrappers.add(dependWrapper);
            return this;
        }

        public Builder<W, C> next(WorkerWrapper<?, ?> wrapper) {
            return next(wrapper, true);
        }

        public Builder<W, C> next(WorkerWrapper<?, ?> wrapper, boolean selfIsMust) {
            if (nextWrappers == null) {
                nextWrappers = new ArrayList<>();
            }
            nextWrappers.add(wrapper);

            //强依赖自己
            if (selfIsMust) {
                if (selfIsMustSet == null) {
                    selfIsMustSet = new HashSet<>();
                }
                selfIsMustSet.add(wrapper);
            }
            return this;
        }

        public Builder<W, C> next(WorkerWrapper<?, ?>... wrappers) {
            if (wrappers == null) {
                return this;
            }
            for (WorkerWrapper<?, ?> wrapper : wrappers) {
                next(wrapper);
            }
            return this;
        }

        public WorkerWrapper<W, C> build() {
            WorkerWrapper<W, C> wrapper = new WorkerWrapper<>(uniqueId, worker, afterHandler, preHandler);
            wrapper.setNeedCheckNextWrapperResult(needCheckNextWrapperResult);
            if (dependWrappers != null) {
                for (PrePlatoNodeProxy workerWrapper : dependWrappers) {
                    workerWrapper.getWorkerWrapper().addNext(wrapper);
                    wrapper.addDepend(workerWrapper);
                }
            }
            if (nextWrappers != null) {
                for (WorkerWrapper<?, ?> workerWrapper : nextWrappers) {
                    boolean must = false;
                    if (selfIsMustSet != null && selfIsMustSet.contains(workerWrapper)) {
                        must = true;
                    }
                    workerWrapper.addDepend(wrapper, must);
                    wrapper.addNext(workerWrapper);
                }
            }

            return wrapper;
        }

    }
}
