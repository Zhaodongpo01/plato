package com.example.plato.element;

import com.example.plato.element.NodeLoadByBean.NodeBeanBuilder;
import com.example.plato.exception.PlatoException;
import com.example.plato.holder.NodeHolder;
import com.example.plato.loader.ymlNode.AbstractYmlNode;
import com.example.plato.platoEnum.MessageEnum;
import com.example.plato.runningData.GraphRunningInfo;
import com.example.plato.util.PlatoAssert;
import com.example.plato.util.TraceUtil;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/30 15:04
 */
@Slf4j
public class GraphManager<P> {

    private static ExecutorService threadPoolExecutor = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() * 2
            , 1000
            , 100_00L
            , TimeUnit.MILLISECONDS
            , new LinkedBlockingQueue<>(1_500)
            , new AbortPolicy());

    private final Map<String, NodeBeanBuilder<?, ?>> firstNodeBeanBuilderMap = new ConcurrentHashMap<>();

    private NodeBeanBuilder<?, ?> getFirstNodeBeanBuilder() {
        PlatoAssert.emptyException(MessageEnum.START_MISS_ERROR::getMes, firstNodeBeanBuilderMap);
        if (firstNodeBeanBuilderMap.values().size() != 1) {
            throw new PlatoException("firstNodeBeanBuilderMap size error");
        }
        return Lists.newArrayList(firstNodeBeanBuilderMap.values()).get(0);
    }

    public static <P> GraphManager<P> getManager() {
        return new GraphManager<>();
    }

    public void buildThreadPoolExecutor(ExecutorService executorService) {
        threadPoolExecutor = Objects.isNull(executorService) ? threadPoolExecutor : executorService;
    }

    /**
     * 代码方式启动run方法
     */
    public GraphRunningInfo run(P p, long timeOut, TimeUnit timeUnit) {
        NodeLoadByBean<?, ?> firstNodeLoadByBean = getFirstNodeBeanBuilder().build();
        PlatoAssert.nullException(() -> "run firstNodeLoadByBean blank", firstNodeLoadByBean);
        PlatoAssert.emptyException(() -> "run graphId blank", firstNodeLoadByBean.getGraphId());
        PlatoAssert.notEmptyException(() -> "firstNodeLoadByBean define error", firstNodeLoadByBean.getPreNodes());
        GraphRunningInfo graphRunningInfo = new GraphRunningInfo(TraceUtil.getRandomTraceId());
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(
                () -> new NodeBeanProxy(firstNodeLoadByBean, p, graphRunningInfo).run(null,
                        threadPoolExecutor));
        try {
            completableFuture.get(timeOut, timeUnit);
            return graphRunningInfo;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("GraphManager run error mes {} ", e.getMessage(), e);
            throw new PlatoException("GraphManager run error");
        }
    }

    /**
     * yml方式启动run方法
     */
    public GraphRunningInfo run(P p, String graphId, long timeOut, TimeUnit timeUnit) {
        Map<String, AbstractYmlNode<?, ?>> startNodeMap = NodeHolder.getStartNodeMap();
        PlatoAssert.emptyException(MessageEnum.START_MISS_ERROR::getMes, startNodeMap);
        AbstractYmlNode<?, ?> abstractYmlNode = startNodeMap.get(graphId);
        GraphRunningInfo graphRunningInfo = new GraphRunningInfo(TraceUtil.getRandomTraceId());
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(
                () -> new NodeYmlProxy(abstractYmlNode, p, graphRunningInfo)
                        .run(null, threadPoolExecutor));
        try {
            completableFuture.get(timeOut, timeUnit);
            return graphRunningInfo;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("GraphManager runByYml error mes {} ", e.getMessage(), e);
            throw new PlatoException("GraphManager runByYml error");
        }
    }

    public GraphManager linkNodes(NodeBeanBuilder nodeBeanBuilder, NodeBeanBuilder nextNodeBeanBuilder) {
        return linkNodes(nodeBeanBuilder, nextNodeBeanBuilder, true);
    }

    public GraphManager linkNodes(NodeBeanBuilder nodeBeanBuilder, NodeBeanBuilder nextNodeBeanBuilder,
            Boolean append) {
        PlatoAssert.nullException(() -> "linkNodes param error", nextNodeBeanBuilder, nodeBeanBuilder);
        nodeBeanBuilder.addNextBuilderNodes(nextNodeBeanBuilder);
        if (Objects.isNull(append) || BooleanUtils.isTrue(append)) {
            nextNodeBeanBuilder.addPreBuilderNodes(nodeBeanBuilder.getUniqueId());
        }
        if (StringUtils.isNotBlank(nodeBeanBuilder.getGraphId())
                && !firstNodeBeanBuilderMap.containsKey(nextNodeBeanBuilder.getUniqueId())) {
            firstNodeBeanBuilderMap.put(nodeBeanBuilder.getUniqueId(), nodeBeanBuilder);
        }
        firstNodeBeanBuilderMap.remove(nextNodeBeanBuilder.getUniqueId());
        return this;
    }
}

