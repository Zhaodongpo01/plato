package com.example.plato.element;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.CollectionUtils;

import com.example.plato.handler.AfterHandler;
import com.example.plato.handler.INodeWork;
import com.example.plato.handler.PreHandler;
import com.example.plato.runningData.GraphRunningInfo;
import com.example.plato.runningData.ResultData;
import com.example.plato.runningData.ResultState;

/**
 * @author zhaodongpo
 * @version 1.0
 * 2022/3/31 11:25 下午
 */
public class PlatoNodeProxy<P, R> {

    private P p;
    private final String uniqueId;
    private final INodeWork<P, R> iNodeWork;
    private List<PlatoNodeProxy<?, ?>> nextProxies = new ArrayList<>();
    private List<PrePlatoNodeProxy> dependProxies = new ArrayList<>();
    private AfterHandler afterHandler;
    private PreHandler<P> preHandler;
    private AtomicInteger state = new AtomicInteger(0);
    private GraphRunningInfo graphRunningInfo;
    private volatile ResultData<R> resultData = ResultData.defaultResult();
    private volatile boolean checkNextResult = true;

    private static final int FINISH = 1;
    private static final int ERROR = 2;
    private static final int WORKING = 3;
    private static final int INIT = 0;

    private PlatoNodeProxy(String uniqueId, INodeWork<P, R> iNodeWork, AfterHandler afterHandler,
            PreHandler preHandler) {
        if (iNodeWork == null) {
            throw new NullPointerException("async.worker is null");
        }
        this.iNodeWork = iNodeWork;
        this.uniqueId = uniqueId;
        this.afterHandler = afterHandler;
        this.preHandler = preHandler;
    }

    public void work(ExecutorService executorService, PlatoNodeProxy fromProxy,
            GraphRunningInfo graphRunningInfo) {
        this.graphRunningInfo = graphRunningInfo;
        graphRunningInfo.putUniqueResultData(uniqueId, resultData);
        if (getState() == FINISH || getState() == ERROR) {
            beginNext(executorService);
            return;
        }
        if (checkNextResult && !checkNextProxyResult()) {
            fastFail(INIT, new RuntimeException());
            beginNext(executorService);
            return;
        }
        if (CollectionUtils.isEmpty(dependProxies)) {
            executor(fromProxy);
            beginNext(executorService);
            return;
        }
        if (dependProxies.size() == 1) {
            doDependsOneJob(fromProxy);
            beginNext(executorService);
        } else {
            doDependsJobs(executorService, dependProxies, fromProxy);
        }
    }

    private boolean checkNextProxyResult() {
        //如果自己就是最后一个，或者后面有并行的多个，就返回true
        if (nextProxies.size() != 1) {
            return getState() == INIT;
        }
        PlatoNodeProxy<?, ?> nextProxy = nextProxies.get(0);
        return nextProxy.getState() == INIT && nextProxy.checkNextProxyResult();
    }

    /**
     * 进行下一个任务
     */
    private void beginNext(ExecutorService executorService) {
        if (nextProxies.size() == 1) {
            nextProxies.get(0).work(executorService, PlatoNodeProxy.this, graphRunningInfo);
            return;
        }
        CompletableFuture[] futures = new CompletableFuture[nextProxies.size()];
        for (int i = 0; i < nextProxies.size(); i++) {
            int finalI = i;
            futures[i] = CompletableFuture.runAsync(() -> nextProxies.get(finalI)
                    .work(executorService, PlatoNodeProxy.this, graphRunningInfo), executorService);
        }
        try {
            CompletableFuture.allOf(futures).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void doDependsOneJob(PlatoNodeProxy<?, ?> dependProxy) {
        if (ResultState.TIMEOUT == dependProxy.getWorkResult().getResultState()) {
            resultData = defaultResult();
            fastFail(INIT, null);
        } else if (ResultState.EXCEPTION == dependProxy.getWorkResult().getResultState()) {
            resultData = defaultExResult(dependProxy.getWorkResult().getEx());
            fastFail(INIT, null);
        } else {
            //前面任务正常完毕了，该自己了
            executor(dependProxy);
        }
    }

    private synchronized void doDependsJobs(ExecutorService executorService, List<PrePlatoNodeProxy> dependProxies,
            PlatoNodeProxy<?, ?> fromProxy) {
        boolean nowDependIsMust = false;
        //创建必须完成的上游proxy集合
        Set<PrePlatoNodeProxy> mustProxy = new HashSet<>();
        for (PrePlatoNodeProxy dependProxy : dependProxies) {
            if (dependProxy.isMust()) {
                mustProxy.add(dependProxy);
            }
            if (dependProxy.getWorkerProxy().equals(fromProxy)) {
                nowDependIsMust = dependProxy.isMust();
            }
        }

        //如果全部是不必须的条件，那么只要到了这里，就执行自己。
        if (mustProxy.size() == 0) {
            if (ResultState.TIMEOUT == fromProxy.getWorkResult().getResultState()) {
                fastFail(INIT, null);
            } else {
                executor(fromProxy);
            }
            beginNext(executorService);
            return;
        }

        //如果存在需要必须完成的，且fromProxy不是必须的，就什么也不干
        if (!nowDependIsMust) {
            return;
        }

        //如果fromProxy是必须的
        boolean existNoFinish = false;
        boolean hasError = false;
        //先判断前面必须要执行的依赖任务的执行结果，如果有任何一个失败，那就不用走action了，直接给自己设置为失败，进行下一步就是了
        for (PrePlatoNodeProxy dependProxy : mustProxy) {
            PlatoNodeProxy<?, ?> platoNodeProxy = dependProxy.getWorkerProxy();
            ResultData<?> tempResultData = platoNodeProxy.getWorkResult();
            //为null或者isWorking，说明它依赖的某个任务还没执行到或没执行完
            if (platoNodeProxy.getState() == INIT || platoNodeProxy.getState() == WORKING) {
                existNoFinish = true;
                break;
            }
            if (ResultState.TIMEOUT == tempResultData.getResultState()) {
                resultData = defaultResult();
                hasError = true;
                break;
            }
            if (ResultState.EXCEPTION == tempResultData.getResultState()) {
                resultData = defaultExResult(platoNodeProxy.getWorkResult().getEx());
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
            executor(fromProxy);
            beginNext(executorService);
        }
    }

    /**
     * 执行自己的job.具体的执行是在另一个线程里,但判断阻塞超时是在work线程
     */
    private void executor(PlatoNodeProxy fromProxy) {
        //阻塞取结果
        resultData = workerDoJob(fromProxy);
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
                resultData = defaultResult();
            } else {
                resultData = defaultExResult(e);
            }
        }
        iNodeWork.hook(p, resultData);
        return true;
    }

    /**
     * 具体的单个worker执行任务
     */
    private ResultData<R> workerDoJob(PlatoNodeProxy fromProxy) {
        //避免重复执行
        if (!checkIsNullResult()) {
            return resultData;
        }
        try {
            //如果已经不是init状态了，说明正在被执行或已执行完毕。这一步很重要，可以保证任务不被重复执行
            if (!compareAndSetState(INIT, WORKING)) {
                return resultData;
            }

            if (fromProxy != null) {
                this.p = getPreHandlerParam();
            }

            //执行耗时操作
            R resultValue = iNodeWork.work(p);

            //如果状态不是在working,说明别的地方已经修改了
            if (!compareAndSetState(WORKING, FINISH)) {
                return resultData;
            }

            resultData.setResultState(ResultState.SUCCESS);
            resultData.setResult(resultValue);
            //回调成功
            iNodeWork.hook(p, resultData);

            return resultData;
        } catch (Exception e) {
            //避免重复回调
            if (!checkIsNullResult()) {
                return resultData;
            }
            fastFail(WORKING, e);
            return resultData;
        }
    }

    private P getPreHandlerParam() {
        if (this.preHandler == null) {
            return null;
        }
        return this.preHandler.paramHandle(graphRunningInfo);
    }

    public ResultData<R> getWorkResult() {
        return resultData;
    }

    public List<PlatoNodeProxy<?, ?>> getNextProxies() {
        return nextProxies;
    }

    public void setp(P p) {
        this.p = p;
    }

    private boolean checkIsNullResult() {
        return ResultState.DEFAULT == resultData.getResultState();
    }

    private void addDepend(PlatoNodeProxy<?, ?> platoNodeProxy, boolean must) {
        addDepend(new PrePlatoNodeProxy(platoNodeProxy, must));
    }

    private void addDepend(PrePlatoNodeProxy dependProxy) {
        //如果依赖的是重复的同一个，就不重复添加了
        for (PrePlatoNodeProxy proxy : dependProxies) {
            if (proxy.equals(dependProxy)) {
                return;
            }
        }
        dependProxies.add(dependProxy);
    }

    private void addNext(PlatoNodeProxy<?, ?> platoNodeProxy) {
        //避免添加重复
        for (PlatoNodeProxy proxy : nextProxies) {
            if (platoNodeProxy.equals(proxy)) {
                return;
            }
        }
        nextProxies.add(platoNodeProxy);
    }

    private void addNextProxies(List<PlatoNodeProxy<?, ?>> proxys) {
        if (proxys == null) {
            return;
        }
        for (PlatoNodeProxy<?, ?> proxy : proxys) {
            addNext(proxy);
        }
    }

    private void addDependProxies(List<PrePlatoNodeProxy> dependProxies) {
        if (dependProxies == null) {
            return;
        }
        for (PrePlatoNodeProxy proxy : dependProxies) {
            addDepend(proxy);
        }
    }

    private ResultData<R> defaultResult() {
        resultData.setResultState(ResultState.TIMEOUT);
        resultData.setResult(null);
        return resultData;
    }

    private ResultData<R> defaultExResult(Exception ex) {
        resultData.setResultState(ResultState.EXCEPTION);
        resultData.setResult(null);
        resultData.setEx(ex);
        return resultData;
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

    private void setcheckNextResult(boolean checkNextResult) {
        this.checkNextResult = checkNextResult;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PlatoNodeProxy<?, ?> that = (PlatoNodeProxy<?, ?>) o;
        return checkNextResult == that.checkNextResult &&
                Objects.equals(p, that.p) &&
                Objects.equals(iNodeWork, that.iNodeWork) &&
                Objects.equals(nextProxies, that.nextProxies) &&
                Objects.equals(dependProxies, that.dependProxies) &&
                Objects.equals(state, that.state) &&
                Objects.equals(resultData, that.resultData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(p, iNodeWork, nextProxies, dependProxies, state, resultData,
                checkNextResult);
    }

    public static class Builder<W, C> {

        private AfterHandler afterHandler;
        private PreHandler<W> preHandler;
        private String uniqueId;
        private INodeWork<W, C> worker;
        private List<PlatoNodeProxy<?, ?>> nextProxies = new ArrayList<>();
        private List<PrePlatoNodeProxy> dependProxies = new ArrayList<>();
        private Set<PlatoNodeProxy<?, ?>> selfIsMustSet;
        private boolean checkNextResult = true;

        public Builder<W, C> setAfterHandler(AfterHandler afterHandler) {
            this.afterHandler = afterHandler;
            return this;
        }

        public Builder<W, C> setPreHandler(PreHandler<W> preHandler) {
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

        public Builder<W, C> checkNextResult(boolean checkNextResult) {
            this.checkNextResult = checkNextResult;
            return this;
        }

        public Builder<W, C> depend(PlatoNodeProxy<?, ?>... proxys) {
            if (proxys == null) {
                return this;
            }
            for (PlatoNodeProxy<?, ?> proxy : proxys) {
                depend(proxy);
            }
            return this;
        }

        public Builder<W, C> depend(PlatoNodeProxy<?, ?> proxy) {
            return depend(proxy, true);
        }

        public Builder<W, C> depend(PlatoNodeProxy<?, ?> proxy, boolean isMust) {
            if (proxy == null) {
                return this;
            }
            PrePlatoNodeProxy dependProxy = new PrePlatoNodeProxy(proxy, isMust);
            if (dependProxies == null) {
                dependProxies = new ArrayList<>();
            }
            dependProxies.add(dependProxy);
            return this;
        }

        public Builder<W, C> next(PlatoNodeProxy<?, ?> proxy) {
            return next(proxy, true);
        }

        public Builder<W, C> next(PlatoNodeProxy<?, ?> proxy, boolean selfIsMust) {
            nextProxies.add(proxy);
            //强依赖自己
            if (selfIsMust) {
                if (selfIsMustSet == null) {
                    selfIsMustSet = new HashSet<>();
                }
                selfIsMustSet.add(proxy);
            }
            return this;
        }

        public Builder<W, C> next(PlatoNodeProxy<?, ?>... proxys) {
            if (proxys == null) {
                return this;
            }
            for (PlatoNodeProxy<?, ?> proxy : proxys) {
                next(proxy);
            }
            return this;
        }

        public PlatoNodeProxy<W, C> build() {
            PlatoNodeProxy<W, C> proxy = new PlatoNodeProxy<>(uniqueId, worker, afterHandler, preHandler);
            proxy.setcheckNextResult(checkNextResult);
            for (PrePlatoNodeProxy workerProxy : dependProxies) {
                workerProxy.getWorkerProxy().addNext(proxy);
                proxy.addDepend(workerProxy);
            }
            for (PlatoNodeProxy<?, ?> platoNodeProxy : nextProxies) {
                platoNodeProxy.addDepend(proxy, selfIsMustSet != null && selfIsMustSet.contains(platoNodeProxy));
                proxy.addNext(platoNodeProxy);
            }
            return proxy;
        }

    }
}
