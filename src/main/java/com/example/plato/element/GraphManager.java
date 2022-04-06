package com.example.plato.element;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.example.plato.exception.PlatoException;
import com.example.plato.runningData.GraphRunningInfo;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/3/31 11:25 下午
 */
public class GraphManager {

    private final String graphId;

    public GraphManager(String graphId) {
        this.graphId = graphId;
    }

    public <P, R> void run(P p,
            ThreadPoolExecutor threadPoolExecutor,
            PlatoNodeProxy<P, R> firstPlatoNodeProxy,
            Long timeOut,
            TimeUnit timeUnit) {
        firstPlatoNodeProxy.setParam(p);
        Map<String, PlatoNodeProxy> forParamUseProxies = new ConcurrentHashMap<>();
        GraphRunningInfo graphRunningInfo = new GraphRunningInfo();
        CompletableFuture<Void> completableFuture =
                CompletableFuture.runAsync(
                        () -> firstPlatoNodeProxy.work(threadPoolExecutor, null, graphRunningInfo),
                        threadPoolExecutor);
        try {
            completableFuture.get(timeOut, timeUnit);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new PlatoException(e, "GraphManager run error");
        }
    }
}
