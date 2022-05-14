package com.example.plato.element;

import static org.reflections.Reflections.log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.collections4.CollectionUtils;

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

    public void run(ExecutorService executorService, PlatoNodeProxy comingNode, GraphRunningInfo graphRunningInfo) {
        this.graphRunningInfo = graphRunningInfo;
        graphRunningInfo.putResultData(uniqueId, resultData);
        if (getState() == CurrentState.FINISH || getState() == CurrentState.ERROR) {
            runNext(executorService);
            return;
        }
        if (CollectionUtils.isNotEmpty(preProxies)) {
            if (preProxies.size() == 1) {
                if (runPreProxy(comingNode)) {
                    normalRun(executorService, comingNode);
                }
            } else {
                runPreProxies(executorService, comingNode);
            }
        } else {
            normalRun(executorService, comingNode);
        }
    }

    private void normalRun(ExecutorService executorService, PlatoNodeProxy comingNode) {
        executor(comingNode);
        runNext(executorService);
    }

    private void runNext(ExecutorService executorService) {
        if (CollectionUtils.isEmpty(nextProxies)) {
            return;
        }
        if (nextProxies.size() == 1) {
            nextProxies.get(0).run(executorService, PlatoNodeProxy.this, graphRunningInfo);
            return;
        }
        nextProxies.forEach(platoNodeProxy -> platoNodeProxy.run(executorService, this, graphRunningInfo));
        /*List<CompletableFuture<Void>> completableFutureList =
                nextProxies.stream().map(platoNodeProxy -> CompletableFuture.runAsync(
                                () -> platoNodeProxy.run(executorService, this, graphRunningInfo), executorService))
                        .collect(Collectors.toList());
        try {
            CompletableFuture.allOf(completableFutureList.toArray(new CompletableFuture[] {})).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("runNext异常{}", e.getMessage(), e);
            throw new PlatoException("runNext异常");
        }*/
    }

    private boolean runPreProxy(PlatoNodeProxy<?, ?> preProxy) {
        boolean result = false;
        if (ResultState.TIMEOUT == preProxy.getResult().getResultState()) {
            resultData.defaultResult();
            fastFail(CurrentState.INIT, null);
        } else if (ResultState.EXCEPTION == preProxy.getResult().getResultState()) {
            resultData.defaultExResult(preProxy.getResult().getEx());
            fastFail(CurrentState.INIT, null);
        } else {
            result = true;
        }
        return result;
    }

    private synchronized void runPreProxies(ExecutorService executorService, PlatoNodeProxy<?, ?> comingNode) {
        boolean nowDependIsMust = false;
        Set<PrePlatoNodeProxy> mustProxy = new HashSet<>();
        for (PrePlatoNodeProxy prePlatoNodeProxy : preProxies) {
            if (prePlatoNodeProxy.isMust()) {
                mustProxy.add(prePlatoNodeProxy);
            }
            if (prePlatoNodeProxy.getPlatoNodeProxy().equals(comingNode)) {
                nowDependIsMust = prePlatoNodeProxy.isMust();
            }
        }
        if (mustProxy.size() == 0) {
            if (ResultState.TIMEOUT == comingNode.getResult().getResultState()) {
                fastFail(CurrentState.INIT, null);
            } else {
                executor(comingNode);
                runNext(executorService);
            }
            return;
        }
        if (!nowDependIsMust) {
            return;
        }
        boolean existNoFinish = false;
        for (PrePlatoNodeProxy dependProxy : mustProxy) {
            PlatoNodeProxy<?, ?> platoNodeProxy = dependProxy.getPlatoNodeProxy();
            ResultData<?> tempResultData = platoNodeProxy.getResult();
            if (platoNodeProxy.getState() == CurrentState.INIT || platoNodeProxy.getState() == CurrentState.WORKING) {
                existNoFinish = true;
                break;
            }
            if (ResultState.TIMEOUT == tempResultData.getResultState()) {
                resultData.defaultResult();
                fastFail(CurrentState.INIT, null);
                runNext(executorService);
                break;
            }
            if (ResultState.EXCEPTION == tempResultData.getResultState()) {
                resultData.defaultExResult(platoNodeProxy.getResult().getEx());
                fastFail(CurrentState.INIT, null);
                runNext(executorService);
                break;
            }
        }
        if (!existNoFinish) {
            normalRun(executorService, comingNode);
        }
    }

    private boolean fastFail(CurrentState expect, Exception e) {
        if (!compareAndSetState(expect, CurrentState.ERROR)) {
            return false;
        }
        if (resultData.checkIsNullResult()) {
            resultData = e == null ? resultData.defaultResult() : resultData.defaultExResult(e);
        }
        iNodeWork.hook(p, resultData);
        return true;
    }

    private ResultData<R> executor(PlatoNodeProxy fromProxy) {
        if (!resultData.checkIsNullResult()) {
            return resultData;
        }
        try {
            if (!compareAndSetState(CurrentState.INIT, CurrentState.WORKING)) {
                return resultData;
            }
            if (fromProxy != null) {
                this.p = getPreHandlerParam();
            }
            R resultValue = iNodeWork.work(p);
            if (!compareAndSetState(CurrentState.WORKING, CurrentState.FINISH)) {
                return resultData;
            }
            resultData.setResultState(ResultState.SUCCESS);
            resultData.setResult(resultValue);
            iNodeWork.hook(p, resultData);
            return resultData;
        } catch (Exception e) {
            log.error("executor error {}", e.getMessage(), e);
            if (!resultData.checkIsNullResult()) {
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

    void addPreProxy(PlatoNodeProxy<?, ?> platoNodeProxy, boolean must) {
        addPreProxy(new PrePlatoNodeProxy(platoNodeProxy, must));
    }

    private void addPreProxy(PrePlatoNodeProxy prePlatoNodeProxy) {
        if (preProxies.parallelStream().noneMatch(this::equals)) {
            preProxies.add(prePlatoNodeProxy);
        }
    }

    void addNextProxy(PlatoNodeProxy<?, ?> nextPlatoNodeProxy) {
        if (nextProxies.parallelStream().noneMatch(this::equals)) {
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
