package com.example.plato.element;

import static org.reflections.Reflections.log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import com.example.plato.exception.PlatoException;
import com.example.plato.handler.AfterHandler;
import com.example.plato.handler.INodeWork;
import com.example.plato.handler.PreHandler;
import com.example.plato.runningData.GraphRunningInfo;
import com.example.plato.runningData.ResultData;
import com.example.plato.runningData.ResultState;
import com.example.plato.util.PlatoAssert;

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
    private List<PrePlatoNodeProxy> preProxies = new ArrayList<>();
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
        PlatoAssert.nullException(() -> "PlatoNodeProxy param error", iNodeWork);
        PlatoAssert.emptyException(() -> "PlatoNodeProxy param error", uniqueId);
        this.iNodeWork = iNodeWork;
        this.uniqueId = uniqueId;
        this.afterHandler = afterHandler;
        this.preHandler = preHandler;
    }

    public void run(ExecutorService executorService, PlatoNodeProxy fromProxy,
            GraphRunningInfo graphRunningInfo) {
        this.graphRunningInfo = graphRunningInfo;
        graphRunningInfo.putResultData(uniqueId, resultData);
        if (getState() == FINISH || getState() == ERROR) {
            runNext(executorService);
            return;
        }
        if (checkNextResult && !checkNextProxyResult()) {
            fastFail(INIT, new RuntimeException());
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
            return getState() == INIT;
        }
        PlatoNodeProxy<?, ?> nextProxy = nextProxies.get(0);
        return nextProxy.getState() == INIT && nextProxy.checkNextProxyResult();
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

    private boolean runPreProxy(PlatoNodeProxy<?, ?> dependProxy) {
        if (ResultState.TIMEOUT == dependProxy.getWorkResult().getResultState()) {
            resultData = defaultResult();
            fastFail(INIT, null);
            return false;
        } else if (ResultState.EXCEPTION == dependProxy.getWorkResult().getResultState()) {
            resultData = defaultExResult(dependProxy.getWorkResult().getEx());
            fastFail(INIT, null);
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
    private boolean fastFail(int expect, Exception e) {
        //试图将它从expect状态,改成Error
        if (!compareAndSetState(expect, ERROR)) {
            return false;
        }
        //尚未处理过结果
        if (checkIsNullResult()) {
            resultData = e == null ? defaultResult() : defaultExResult(e);
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

    private void addPreProxy(PlatoNodeProxy<?, ?> platoNodeProxy, boolean must) {
        addPreProxy(new PrePlatoNodeProxy(platoNodeProxy, must));
    }

    private void addPreProxy(PrePlatoNodeProxy prePlatoNodeProxy) {
        //如果依赖的是重复的同一个，就不重复添加了
        if (!preProxies.stream().filter(this::equals).findFirst().isPresent()) {
            preProxies.add(prePlatoNodeProxy);
        }
    }

    private void addNextProxy(PlatoNodeProxy<?, ?> nextPlatoNodeProxy) {
        if (!nextProxies.stream().filter(this::equals).findFirst().isPresent()) {
            nextProxies.add(nextPlatoNodeProxy);
        }
    }

    private void addNextProxies(List<PlatoNodeProxy<?, ?>> nextProxies) {
        PlatoAssert.nullException(() -> "addNextProxies nextProxies must not null", nextProxies);
        for (PlatoNodeProxy<?, ?> proxy : nextProxies) {
            addNextProxy(proxy);
        }
    }

    private void addPreProxies(List<PrePlatoNodeProxy> preNodeProxies) {
        PlatoAssert.nullException(() -> "addNextProxies preNodeProxies must not null", preNodeProxies);
        for (PrePlatoNodeProxy proxy : preNodeProxies) {
            addPreProxy(proxy);
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
                Objects.equals(preProxies, that.preProxies) &&
                Objects.equals(state, that.state) &&
                Objects.equals(resultData, that.resultData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(p, iNodeWork, nextProxies, preProxies, state, resultData,
                checkNextResult);
    }

    public static class PlatoNodeBuilder<W, C> {

        private AfterHandler afterHandler;
        private PreHandler<W> preHandler;
        private String graphId;
        private String uniqueId;
        private INodeWork<W, C> worker;
        private AtomicBoolean reBuild = new AtomicBoolean(false);  // B到D。C也到D。那么创建的时候就会出现D呗创建两次，加上这个属性保证D在并发时只创建一次
        private Set<PlatoNodeBuilder<?, ?>> nextProxies = new HashSet<>();
        private Set<PlatoNodeBuilder<?, ?>> selfIsMustSet = new HashSet<>();
        private boolean checkNextResult = true;

        public PlatoNodeBuilder<W, C> setAfterHandler(AfterHandler afterHandler) {
            this.afterHandler = afterHandler;
            return this;
        }

        public PlatoNodeBuilder<W, C> setPreHandler(PreHandler<W> preHandler) {
            this.preHandler = preHandler;
            return this;
        }

        public PlatoNodeBuilder<W, C> setINodeWork(INodeWork<W, C> worker) {
            this.worker = worker;
            return this;
        }

        public PlatoNodeBuilder<W, C> setUniqueId(String uniqueId) {
            PlatoAssert.emptyException(() -> "setUniqueId uniqueId error", uniqueId);
            this.uniqueId = uniqueId;
            return this;
        }

        public PlatoNodeBuilder<W, C> setGraphId(String graphId) {
            PlatoAssert.emptyException(() -> "setGraphId graphId error", graphId);
            this.graphId = graphId;
            return this;
        }

        public PlatoNodeBuilder<W, C> checkNextResult(boolean checkNextResult) {
            this.checkNextResult = checkNextResult;
            return this;
        }

        public PlatoNodeBuilder<W, C> next(PlatoNodeBuilder<?, ?> proxy) {
            return next(proxy, true);
        }

        public PlatoNodeBuilder<W, C> next(PlatoNodeBuilder<?, ?> proxy, boolean selfIsMust) {
            nextProxies.add(proxy);
            if (selfIsMust) {
                selfIsMustSet.add(proxy);
            }
            return this;
        }

        public PlatoNodeBuilder<W, C> next(PlatoNodeBuilder<?, ?>... proxies) {
            PlatoAssert.nullException(() -> "next param must not null", proxies);
            for (PlatoNodeBuilder<?, ?> proxy : proxies) {
                next(proxy);
            }
            return this;
        }

        PlatoNodeProxy<W, C> build() {
            if (!reBuild.compareAndSet(false, true)) {
                return null;
            }
            PlatoNodeProxy<W, C> proxy = new PlatoNodeProxy<>(uniqueId, worker, afterHandler, preHandler);
            proxy.setcheckNextResult(checkNextResult);
            if (CollectionUtils.isNotEmpty(this.nextProxies)) {
                convertBuild2Bean(proxy, this.nextProxies);
            }
            return proxy;
        }

        private void convertBuild2Bean(PlatoNodeProxy<W, C> proxy,
                Set<PlatoNodeBuilder<?, ?>> nextProxies) {
            nextProxies.forEach(platoNodeBuilder -> {
                if (!platoNodeBuilder.reBuild.get()) {
                    PlatoNodeProxy<?, ?> platoNodeProxy = platoNodeBuilder.build();
                    if (Optional.ofNullable(platoNodeProxy).isPresent()) {
                        platoNodeProxy.addPreProxy(proxy,
                                selfIsMustSet != null && selfIsMustSet.contains(platoNodeProxy));
                        proxy.addNextProxy(platoNodeProxy);
                    }
                }
            });
        }
    }
}
