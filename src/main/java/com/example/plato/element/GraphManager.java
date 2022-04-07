package com.example.plato.element;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.plato.exception.PlatoException;
import com.example.plato.loader.factory.NodeFactory;
import com.example.plato.runningData.GraphRunningInfo;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/3/31 11:25 下午
 */
@Slf4j
public class GraphManager {

    private final String graphId;

    private static final ThreadPoolExecutor COMMON_POOL = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    @Autowired
    private NodeFactory nodeFactory;

    public GraphManager(String graphId) {
        this.graphId = graphId;
    }

    public <P, R> GraphRunningInfo run(P p,
            ThreadPoolExecutor threadPoolExecutor,
            PlatoNodeProxy<P, R> firstPlatoNodeProxy,
            Long timeOut, TimeUnit timeUnit) {
        firstPlatoNodeProxy.setp(p);
        GraphRunningInfo graphRunningInfo = new GraphRunningInfo();
        CompletableFuture<Void> completableFuture =
                CompletableFuture.runAsync(
                        () -> firstPlatoNodeProxy.run(threadPoolExecutor, null, graphRunningInfo),
                        threadPoolExecutor);
        try {
            completableFuture.get(timeOut, timeUnit);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new PlatoException(e, "GraphManager run error");
        }
        return graphRunningInfo;
    }

    public <P, R> GraphRunningInfo run(P p,
            PlatoNodeProxy<P, R> firstPlatoNodeProxy,
            Long timeOut, TimeUnit timeUnit) {
        return run(p, COMMON_POOL, firstPlatoNodeProxy, timeOut, timeUnit);
    }

    public <P> GraphRunningInfo run(P p, String uniqueId, Long timeOut, TimeUnit timeUnit) {
        return run(p, uniqueId, COMMON_POOL, timeOut, timeUnit);
    }

    public <P> GraphRunningInfo run(P p,
            String uniqueId, ThreadPoolExecutor threadPoolExecutor,
            Long timeOut, TimeUnit timeUnit) {
        PlatoNodeProxy firstPlatoNodeProxy = nodeFactory.buildProxy(uniqueId, graphId);
        return run(p, threadPoolExecutor, firstPlatoNodeProxy, timeOut, timeUnit);
    }
}
