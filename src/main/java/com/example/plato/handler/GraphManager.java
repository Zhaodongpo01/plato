package com.example.plato.handler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.example.plato.element.PlatoNode;
import com.example.plato.runningData.GraphRunningInfo;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/3/31 11:25 下午
 */
public class GraphManager {
    private String graphId;
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

    public <P, R> void run(P p, PlatoNode<P, R> platoNode) {
        threadPoolExecutor.execute(() -> platoNode.run(p, null, threadPoolExecutor));
    }
}
