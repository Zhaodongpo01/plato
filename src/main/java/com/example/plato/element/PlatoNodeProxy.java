package com.example.plato.element;

import static org.reflections.Reflections.log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import com.example.plato.exception.PlatoException;
import com.example.plato.handler.AfterHandler;
import com.example.plato.handler.INodeWork;
import com.example.plato.handler.PreHandler;
import com.example.plato.platoEnum.CurrentState;
import com.example.plato.runningData.GraphRunningInfo;
import com.example.plato.runningData.ResultData;
import com.example.plato.runningData.ResultState;
import com.example.plato.util.PlatoAssert;

import lombok.Getter;

/**
 * @author zhaodongpo
 * @version 1.0
 * 2022/3/31 11:25 下午
 */
public class PlatoNodeProxy<P, R> {

    private P p;
    private final String uniqueId;
    private final INodeWork<P, R> iNodeWork;
    private final List<PlatoNodeProxy<?, ?>> nextProxies = new ArrayList<>();
    private final List<PrePlatoNodeProxy> preProxies = new ArrayList<>();
    private final AfterHandler afterHandler;
    private final PreHandler<P> preHandler;
    private final AtomicReference<CurrentState> state = new AtomicReference<>(CurrentState.INIT);
    private GraphRunningInfo<R> graphRunningInfo;
    private volatile ResultData<R> resultData;
    private volatile boolean checkNextResult = true;

    PlatoNodeProxy(String uniqueId, INodeWork<P, R> iNodeWork, AfterHandler afterHandler,
            PreHandler<P> preHandler) {
        PlatoAssert.nullException(() -> "PlatoNodeProxy iNodeWork error", iNodeWork);
        PlatoAssert.emptyException(() -> "PlatoNodeProxy uniqueId error", uniqueId);
        this.iNodeWork = iNodeWork;
        this.uniqueId = uniqueId;
        this.afterHandler = afterHandler;
        this.preHandler = preHandler;
        resultData = ResultData.defaultResult(uniqueId);
    }

    public void run(ExecutorService executorService, PlatoNodeProxy fromProxy,
            GraphRunningInfo graphRunningInfo) {
        this.graphRunningInfo = graphRunningInfo;
        graphRunningInfo.putResultData(uniqueId, resultData);
        if (getState() == CurrentState.FINISH || getState() == CurrentState.ERROR) {
            runNext(executorService);
            return;
        }
        if (checkNextResult && !checkNextProxyResult()) {
            fastFail(CurrentState.INIT, new RuntimeException());
            runNext(executorService);
            return;
        }
        if (CollectionUtils.isEmpty(preProxies)) {
            executor(fromProxy);
            runNext(executorService);
            return;
        }
        if (preProxies.size() == 1) {
            if (runPreProxy(fromProxy)) {
                executor(fromProxy);
            }
            runNext(executorService);
        }
        runPreProxies(executorService, fromProxy);
    }

    private boolean checkNextProxyResult() {
        //如果自己就是最后一个，或者后面有并行的多个，就返回true
        if (nextProxies.size() != 1) {
            return getState() == CurrentState.INIT;
        }
        PlatoNodeProxy<?, ?> nextProxy = nextProxies.get(0);
        return nextProxy.getState() == CurrentState.INIT && nextProxy.checkNextProxyResult();
    }

    /**
     * 进行下一个任务
     */
    private void runNext(ExecutorService executorService) {
        if (nextProxies.size() == 1) {
            nextProxies.get(0).run(executorService, PlatoNodeProxy.this, graphRunningInfo);
            return;
        }
        List<CompletableFuture<Void>> completableFutureList =
                nextProxies.stream().map(platoNodeProxy -> CompletableFuture.runAsync(
                        () -> platoNodeProxy.run(executorService, this, graphRunningInfo), executorService)).collect(
                        Collectors.toList());
        try {
            CompletableFuture.allOf(completableFutureList.toArray(new CompletableFuture[] {}))
                    .get(6000_0, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            log.error("runNext异常{}", e.getMessage(), e);
            throw new PlatoException("runNext异常");
        }
    }

    private boolean runPreProxy(PlatoNodeProxy<?, ?> preProxy) {
        if (ResultState.TIMEOUT == preProxy.getResult().getResultState()) {
            resultData.defaultResult();
            fastFail(CurrentState.INIT, null);
            return false;
        } else if (ResultState.EXCEPTION == preProxy.getResult().getResultState()) {
            resultData.defaultExResult(preProxy.getResult().getEx());
            fastFail(CurrentState.INIT, null);
            return false;
        }
        return true;
    }

    private synchronized void runPreProxies(ExecutorService executorService, PlatoNodeProxy<?, ?> fromProxy) {
        boolean nowDependIsMust = false;
        //创建必须完成的上游proxy集合
        Set<PrePlatoNodeProxy> mustProxy = new HashSet<>();
        for (PrePlatoNodeProxy dependProxy : preProxies) {
            if (dependProxy.isMust()) {
                mustProxy.add(dependProxy);
            }
            if (dependProxy.getPlatoNodeProxy().equals(fromProxy)) {
                nowDependIsMust = dependProxy.isMust();
            }
        }

        //如果全部是不必须的条件，那么只要到了这里，就执行自己。
        if (mustProxy.size() == 0) {
            if (ResultState.TIMEOUT == fromProxy.getResult().getResultState()) {
                fastFail(CurrentState.INIT, null);
            } else {
                executor(fromProxy);
            }
            runNext(executorService);
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
            PlatoNodeProxy<?, ?> platoNodeProxy = dependProxy.getPlatoNodeProxy();
            ResultData<?> tempResultData = platoNodeProxy.getResult();
            //为null或者isWorking，说明它依赖的某个任务还没执行到或没执行完
            if (platoNodeProxy.getState() == CurrentState.INIT || platoNodeProxy.getState() == CurrentState.WORKING) {
                existNoFinish = true;
                break;
            }
            if (ResultState.TIMEOUT == tempResultData.getResultState()) {
                resultData.defaultResult();
                hasError = true;
                break;
            }
            if (ResultState.EXCEPTION == tempResultData.getResultState()) {
                resultData.defaultExResult(platoNodeProxy.getResult().getEx());
                hasError = true;
                break;
            }
        }
        //只要有失败的
        if (hasError) {
            fastFail(CurrentState.INIT, null);
            runNext(executorService);
            return;
        }
        //如果上游都没有失败，分为两种情况，一种是都finish了，一种是有的在working
        //都finish的话
        if (!existNoFinish) {
            //上游都finish了，进行自己
            executor(fromProxy);
            runNext(executorService);
        }
    }

    /**
     * 快速失败
     */
    private boolean fastFail(CurrentState expect, Exception e) {
        //试图将它从expect状态,改成Error
        if (!compareAndSetState(expect, CurrentState.ERROR)) {
            return false;
        }
        //尚未处理过结果
        if (checkIsNullResult()) {
            resultData = e == null ? resultData.defaultResult() : resultData.defaultExResult(e);
        }
        iNodeWork.hook(p, resultData);
        return true;
    }

    /**
     * 具体的单个worker执行任务
     */
    private ResultData<R> executor(PlatoNodeProxy fromProxy) {
        //避免重复执行
        if (!checkIsNullResult()) {
            return resultData;
        }
        try {
            //如果已经不是init状态了，说明正在被执行或已执行完毕。这一步很重要，可以保证任务不被重复执行
            if (!compareAndSetState(CurrentState.INIT, CurrentState.WORKING)) {
                return resultData;
            }
            if (fromProxy != null) {
                this.p = getPreHandlerParam();
            }
            //执行耗时操作
            R resultValue = iNodeWork.work(p);

            //如果状态不是在working,说明别的地方已经修改了
            if (!compareAndSetState(CurrentState.WORKING, CurrentState.FINISH)) {
                return resultData;
            }
            resultData.setResultState(ResultState.SUCCESS);
            resultData.setResult(resultValue);
            iNodeWork.hook(p, resultData);
            return resultData;
        } catch (Exception e) {
            //避免重复回调
            log.error("executor error {}", e.getMessage(), e);
            if (!checkIsNullResult()) {
                return resultData;
            }
            fastFail(CurrentState.WORKING, e);
            return resultData;
        }
    }

    private P getPreHandlerParam() {
        if (this.preHandler == null) {
            return null;
        }
        return this.preHandler.paramHandle(graphRunningInfo);
    }

    public ResultData<R> getResult() {
        return resultData;
    }

    public void setP(P p) {
        this.p = p;
    }

    private boolean checkIsNullResult() {
        return ResultState.DEFAULT == resultData.getResultState();
    }

    void addPreProxy(PlatoNodeProxy<?, ?> platoNodeProxy, boolean must) {
        addPreProxy(new PrePlatoNodeProxy(platoNodeProxy, must));
    }

    private void addPreProxy(PrePlatoNodeProxy prePlatoNodeProxy) {
        //如果依赖的是重复的同一个，就不重复添加了
        if (preProxies.stream().noneMatch(this::equals)) {
            preProxies.add(prePlatoNodeProxy);
        }
    }

    void addNextProxy(PlatoNodeProxy<?, ?> nextPlatoNodeProxy) {
        if (nextProxies.stream().noneMatch(this::equals)) {
            nextProxies.add(nextPlatoNodeProxy);
        }
    }

    private CurrentState getState() {
        return state.get();
    }

    private boolean compareAndSetState(CurrentState expect, CurrentState update) {
        return this.state.compareAndSet(expect, update);
    }

    void setCheckNextResult(boolean checkNextResult) {
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
                Objects.equals(preProxies, that.preProxies) &&
                Objects.equals(state, that.state) &&
                Objects.equals(resultData, that.resultData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(p, iNodeWork, nextProxies, preProxies, state, resultData,
                checkNextResult);
    }

    @Getter
    public static class PrePlatoNodeProxy {
        private PlatoNodeProxy<?, ?> platoNodeProxy;
        private boolean must;
        public PrePlatoNodeProxy(PlatoNodeProxy<?, ?> platoNodeProxy, boolean must) {
            this.platoNodeProxy = platoNodeProxy;
            this.must = must;
        }
    }
}
