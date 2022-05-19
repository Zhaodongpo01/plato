package com.example.plato.element;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.example.plato.runningInfo.GraphRunningInfo;
import com.example.plato.util.SystemClock;

/**
 * @author zhaodongpo
 * create  2022/5/16 10:12 上午
 * @version 1.0
 */
public class GraphManager<P> {

    private final String graphId;

    public GraphManager(String graphId) {
        this.graphId = graphId;
    }

    public GraphRunningInfo run(ExecutorService executorService, P param, NodeProxyBuilder<P, ?> startProxyBuilder,
            long graphLimitTime) {
        GraphRunningInfo graphRunningInfo =
                new GraphRunningInfo(UUID.randomUUID().toString(), graphId, SystemClock.now(), graphLimitTime);
        startProxyBuilder.setParam(param);
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
            startProxyBuilder.run(executorService, null, graphLimitTime, graphRunningInfo);
        });
        try {
            completableFuture.get(graphLimitTime, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return graphRunningInfo;
    }



}
