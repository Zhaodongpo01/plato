package com.example.plato.element;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.BooleanUtils;

import com.example.plato.exception.PlatoException;
import com.example.plato.loader.factory.NodeFactory;
import com.example.plato.runningData.GraphRunningInfo;
import com.example.plato.util.PlatoAssert;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * 2022/3/31 11:25 下午
 */
@Slf4j
public class GraphManager {

    private final String graphId;

    public GraphManager(String graphId) {
        this.graphId = graphId;
    }

    public <P, R> GraphRunningInfo run(P p,
            ThreadPoolExecutor graphThreadPoolExecutor,
            PlatoNodeBuilder<P, R> platoNodeBuilder,
            Long timeOut, TimeUnit timeUnit) {
        PlatoNodeProxy<P, R> firstPlatoNodeProxy = platoNodeBuilder.build();
        firstPlatoNodeProxy.setP(p);
        GraphRunningInfo graphRunningInfo = new GraphRunningInfo();
        CompletableFuture<Void> completableFuture =
                CompletableFuture.runAsync(() -> firstPlatoNodeProxy.run(null, graphRunningInfo),
                        graphThreadPoolExecutor);
        try {
            completableFuture.get(timeOut, timeUnit);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new PlatoException(e, "GraphManager run error");
        }
        return graphRunningInfo;
    }

    public <P, R> GraphRunningInfo run(P p, PlatoNodeBuilder<P, R> platoNodeBuilder,
            Long timeOut, TimeUnit timeUnit) {
        return run(p, platoNodeBuilder, timeOut, timeUnit);
    }

    public <P> GraphRunningInfo run(P p, String startNode, Long timeOut, TimeUnit timeUnit) {
        return run(p, startNode, timeOut, timeUnit);
    }

    public <P, R> GraphRunningInfo run(P p, String startNode,
            ThreadPoolExecutor graphThreadPoolExecutor, Long timeOut,
            TimeUnit timeUnit) {
        PlatoNodeBuilder<P, R> firstPlatoNodeBuilder = new NodeFactory().buildProxy(startNode, graphId);
        return run(p, graphThreadPoolExecutor, firstPlatoNodeBuilder, timeOut, timeUnit);
    }

    public GraphManager linkNodes(PlatoNodeBuilder platoNodeBuilder, PlatoNodeBuilder nextNodeBeanBuilder) {
        return linkNodes(platoNodeBuilder, nextNodeBeanBuilder, true);
    }

    public GraphManager linkNodes(PlatoNodeBuilder platoNodeBuilder, PlatoNodeBuilder nextNodeBeanBuilder,
            Boolean append) {
        PlatoAssert.nullException(() -> "linkNodes param error", nextNodeBeanBuilder, platoNodeBuilder);
        platoNodeBuilder.setGraphId(graphId);
        platoNodeBuilder.next(nextNodeBeanBuilder, (Objects.isNull(append) || BooleanUtils.isTrue(append)));
        return this;
    }
}
