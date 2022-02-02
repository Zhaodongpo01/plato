package com.example.plato.element;

import com.example.plato.element.NodeLoadByBean.NodeBeanBuilder;
import com.example.plato.exception.PlatoException;
import com.example.plato.holder.GraphHolder;
import com.example.plato.runningData.GraphRunningInfo;
import com.example.plato.util.TraceUtil;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/30 15:04
 */
@Slf4j
public class NodeManager {

    private static ExecutorService threadPoolExecutor = ForkJoinPool.commonPool();

    private final Map<String, NodeBeanBuilder> firstNodeBeanBuilderMap = new ConcurrentHashMap<>();

    private NodeBeanBuilder getFirstNodeBeanBuilder() {
        if (MapUtils.isEmpty(firstNodeBeanBuilderMap) || firstNodeBeanBuilderMap.values().size() != 1) {
            throw new PlatoException("NodeManager getFirstNodeBeanBuilder error");
        }
        return Lists.newArrayList(firstNodeBeanBuilderMap.values()).get(0);
    }

    public static NodeManager getManager() {
        return new NodeManager();
    }

    public void setThreadPoolExecutor(ExecutorService executorService) {
        if (executorService != null) {
            threadPoolExecutor = executorService;
        }
    }

    public GraphRunningInfo run(long timeOut, TimeUnit timeUnit) {
        NodeLoadByBean firstNodeLoadByBean = getFirstNodeBeanBuilder().build();
        if (Objects.isNull(firstNodeLoadByBean)
                || StringUtils.isBlank(firstNodeLoadByBean.getGraphId())
                || CollectionUtils.isNotEmpty(firstNodeLoadByBean.getPreNodes())) {
            throw new PlatoException("firstNodeLoadByBean define error");
        }
        String graphTraceId = TraceUtil.getRandomTraceId();
        GraphHolder.putGraphRunningInfo(firstNodeLoadByBean.getGraphId(), graphTraceId, new GraphRunningInfo());
        CompletableFuture<Void> completableFuture =
                CompletableFuture.runAsync(
                        () -> new NodeBeanProxy(firstNodeLoadByBean, graphTraceId).run(null, threadPoolExecutor));
        try {
            completableFuture.get(timeOut, timeUnit);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("NodeManager run error {} ", e.getMessage(), e);
            throw new PlatoException("NodeManager run error");
        }
        return GraphHolder.removeGraphRunningInfo(firstNodeLoadByBean.getGraphId(), graphTraceId);
    }

    /**
     * 连接过程不给客户端来实现。代避免码太繁琐。
     */
    public NodeManager linkNodes(NodeBeanBuilder<?, ?> nodeBeanBuilder, NodeBeanBuilder<?, ?> nextNodeBeanBuilder,
            Boolean... notAppends) {
        List<Boolean> appendList = Arrays.stream(notAppends).collect(Collectors.toList());
        if (ObjectUtils.anyNull(nodeBeanBuilder, nextNodeBeanBuilder) || appendList.size() > 1) {
            throw new PlatoException("linkNodes param error");
        }
        nodeBeanBuilder.addNextBuilderNodes(nextNodeBeanBuilder);
        if (CollectionUtils.isEmpty(appendList) || BooleanUtils.isTrue(appendList.get(0))) {
            nextNodeBeanBuilder.addPreBuilderNodes(nodeBeanBuilder.getUniqueId());
        }
        if (StringUtils.isNotBlank(nodeBeanBuilder.getGraphId())
                && !firstNodeBeanBuilderMap.containsKey(nextNodeBeanBuilder.getUniqueId())) {
            firstNodeBeanBuilderMap.put(nodeBeanBuilder.getUniqueId(), nodeBeanBuilder);
        }
        firstNodeBeanBuilderMap.remove(nextNodeBeanBuilder.getUniqueId());
        return this;
    }


    /*private void runSubFlow(NodeBeanProxy<?, ?> comingNode) {
        if (comingNode.getNodeLoadByBean().getSubNodes() == null) {
            return;
        }
        NodeLoadByBean.SubNodes subNodes = comingNode.getSubNodes().check();
        subNodes.setGraphId(String.format("%s_%s_%s", this.getGraphId(), subNodes.getGraphId(), UUID.randomUUID()));
        if (Optional.ofNullable(GraphHolder.getGraph(subNodes.getGraphId())).isPresent()) {
            throw new PlatoException("runSubFlow subNodes graphId error");
        }
        GraphHolder.putGraph(Graph.getGraphInstance(subNodes.getGraphId()));
        this.getNextNodes().add((NodeLoadByBean<?, ?>) subNodes.getSubStartNode());

        if (CollectionUtils.isNotEmpty(this.preNodes)) {
            this.preNodes.add((NodeLoadByBean<?, ?>) subNodes.getSubEndNode());
        } else {
            this.preNodes = Lists.newArrayList((NodeLoadByBean<?, ?>) subNodes.getSubEndNode());
        }
        subNodes.subStartNode.run(comingNode, ForkJoinPool.commonPool());
    }*/
}

