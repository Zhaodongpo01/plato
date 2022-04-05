package com.example.plato.element;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.example.plato.exception.PlatoException;
import com.example.plato.runningData.NodeRunningInfo;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/3/31 11:25 下午
 */
public class GraphManager {

    private final String graphId;
    private static final Long KEEP_ALIVE_TIME = 100L;
    private static final Integer MAXIMUM_POOL_SIZE = 1000;
    private static final Integer CAPACITY = 1_500;
    private static final ExecutorService threadPoolExecutor;

    public GraphManager(String graphId) {
        this.graphId = graphId;
    }

    static {
        threadPoolExecutor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2,
                MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(CAPACITY), new AbortPolicy());
    }

    public <P, R> Map<String, NodeRunningInfo> run(P p, PlatoNode<P, R> firstPlatoNode, Long timeOut,
            TimeUnit timeUnit) {
        PlatoNodeProxy<P, R> platoNodeProxy = buildPlatoNodeProxy(firstPlatoNode);
        Map<String, NodeRunningInfo> nodeRunningInfoMap = new ConcurrentHashMap<>();
        CompletableFuture<Void> completableFuture =
                CompletableFuture.runAsync(() -> platoNodeProxy.run(p, null, threadPoolExecutor, nodeRunningInfoMap),
                        threadPoolExecutor);
        try {
            completableFuture.get(timeOut, timeUnit);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new PlatoException(e, "GraphManager run error");
        } finally {
            return nodeRunningInfoMap;
        }
    }

    private <R, P> PlatoNodeProxy<P, R> buildPlatoNodeProxy(PlatoNode<P, R> firstPlatoNode) {
        Map<String, PlatoNode> platoNodeMap = PlatoNodeHolder.getPlatoNodeMap(graphId);

        return null;
    }

}
