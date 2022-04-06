package com.example.plato.element;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.example.plato.exception.PlatoException;

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
    private static ExecutorService threadPoolExecutor;

    public GraphManager(String graphId) {
        this.graphId = graphId;
    }

    public <P, R> void run(P p, ThreadPoolExecutor COMMON_POOL, WorkerWrapper<P, R> firstPlatoNodeProxy, Long timeOut,
            TimeUnit timeUnit) {
        GraphManager.threadPoolExecutor = COMMON_POOL;
        firstPlatoNodeProxy.setParam(p);
        Map<String, WorkerWrapper> forParamUseWrappers = new ConcurrentHashMap<>();
        CompletableFuture<Void> completableFuture =
                CompletableFuture.runAsync(
                        () -> firstPlatoNodeProxy.work(threadPoolExecutor, null, forParamUseWrappers),
                        threadPoolExecutor);
        try {
            completableFuture.get(timeOut, timeUnit);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new PlatoException(e, "GraphManager run error");
        }
    }
}
