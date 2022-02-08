package com.example.plato.element;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;

import com.example.plato.exception.PlatoException;
import com.example.plato.holder.GraphHolder;
import com.example.plato.holder.HandlerHolder;
import com.example.plato.holder.NodeHolder;
import com.example.plato.loader.config.NodeConfig;
import com.example.plato.loader.ymlHandler.YmlAfterHandler;
import com.example.plato.loader.ymlHandler.YmlPreHandler;
import com.example.plato.loader.ymlNode.AbstractYmlNode;
import com.example.plato.platoEnum.MessageEnum;
import com.example.plato.platoEnum.NodeResultStatus;
import com.example.plato.runningData.*;
import com.example.plato.util.PlatoAssert;
import com.example.plato.util.SystemClock;
import com.example.plato.util.TraceUtil;

import com.google.common.base.Splitter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/2 2:19 下午
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class NodeYmlProxy<P, R> extends AbstractNodeProxy {

    private String traceId;
    private String graphTraceId;
    private AbstractYmlNode<P, R> abstractYmlNode;
    private P p;

    public NodeYmlProxy(AbstractYmlNode<P, R> abstractYmlNode, String graphTraceId, P p) {
        this(abstractYmlNode, graphTraceId);
        this.p = p;
    }

    private NodeYmlProxy(AbstractYmlNode<P, R> abstractYmlNode, String graphTraceId) {
        this.abstractYmlNode = abstractYmlNode;
        this.graphTraceId = graphTraceId;
    }

    @Override
    public void run(AbstractNodeProxy comingNode, ExecutorService executorService) {
        traceId = TraceUtil.getRandomTraceId();
        if (run(comingNode)) {
            runNext(executorService);
        }
    }

    @Override
    public boolean run(AbstractNodeProxy comingNode) {
        NodeConfig currentNodeConfig = abstractYmlNode.getNodeConfig();
        if (Optional.ofNullable(comingNode).isPresent()) {
            Pair<NodeConfig, GraphRunningInfo> perData = getPerData(comingNode);
            NodeConfig comingNodeConfig = perData.getLeft();
            GraphRunningInfo graphRunningInfo = perData.getRight();
            if (!checkShouldRun(comingNodeConfig, graphRunningInfo)) {
                return false;
            }
            p = paramHandle((NodeYmlProxy<?, ?>) comingNode, graphRunningInfo);
        }
        changeStatus(NodeResultStatus.INIT, NodeResultStatus.EXECUTING);
        R result = null;
        ResultData resultData = ResultData.getFail(MessageEnum.CLIENT_ERROR.getMes(), NodeResultStatus.ERROR);
        long startTime = SystemClock.now();
        long endTime = SystemClock.now();
        try {
            result = abstractYmlNode.work(p);
            endTime = SystemClock.now();
            changeStatus(NodeResultStatus.EXECUTING, NodeResultStatus.EXECUTED);
            resultData = ResultData.build(result, NodeResultStatus.EXECUTED, "success", endTime - startTime);
        } catch (Exception e) {
            endTime = SystemClock.now();
            log.error(String.format("%s\t{}", MessageEnum.CLIENT_ERROR), abstractYmlNode.getNodeConfig().getUniqueId(),
                    e);
            changeStatus(NodeResultStatus.EXECUTING, NodeResultStatus.ERROR);
            resultData = ResultData.build(result, NodeResultStatus.ERROR, "fail", endTime - startTime);
            log.error("NodeYmlProxy run error", e);
            return false;
        } finally {
            log.info("{}\t执行耗时{}", currentNodeConfig.getUniqueId(), endTime - startTime);
            NodeRunningInfo nodeRunningInfo = new NodeRunningInfo<>(graphTraceId, traceId,
                    currentNodeConfig.getGraphId(), currentNodeConfig.getUniqueId(), resultData);
            nodeRunningInfo.build();
        }
        return true;
    }

    private boolean checkShouldRun(NodeConfig comingNodeConfig, GraphRunningInfo graphRunningInfo) {
        NodeConfig nodeConfig = abstractYmlNode.getNodeConfig();
        String pre = nodeConfig.getPre();
        String limitMes;
        if (StringUtils.isNotBlank(pre)) {
            List<String> preNodes = Splitter.on(",").trimResults().splitToList(pre);
            limitMes = checkPreNodes(graphRunningInfo, preNodes, comingNodeConfig.getUniqueId());
            if (StringUtils.isNotBlank(limitMes)) {
                setLimitResult(limitMes, graphTraceId, traceId, nodeConfig.getGraphId(), nodeConfig.getUniqueId());
                return false;
            }
        }
        if (StringUtils.isBlank(limitMes = checkSuicide(nodeConfig, graphRunningInfo))
                && StringUtils.isBlank(limitMes = checkComingNodeAfter(comingNodeConfig, graphRunningInfo))) {
            return true;
        }
        setLimitResult(limitMes, graphTraceId, traceId, nodeConfig.getGraphId(), nodeConfig.getUniqueId());
        return false;
    }

    public String checkSuicide(NodeConfig nodeConfig, GraphRunningInfo graphRunningInfo) {
        if (StringUtils.isBlank(nodeConfig.getPreHandler())) {
            return StringUtils.EMPTY;
        }
        YmlPreHandler ymlPreHandler = HandlerHolder.getYmlPreHandler(nodeConfig.getGraphId(), nodeConfig.getUniqueId());
        return ymlPreHandler.suicide(graphRunningInfo) ? MessageEnum.SUICIDE.getMes() : StringUtils.EMPTY;
    }

    public String checkComingNodeAfter(NodeConfig comingNodeConfig, GraphRunningInfo graphRunningInfo) {
        if (StringUtils.isBlank(comingNodeConfig.getAfterHandler())) {
            return StringUtils.EMPTY;
        }
        YmlAfterHandler ymlAfterHandler =
                HandlerHolder.getYmlAfterHandler(comingNodeConfig.getGraphId(), comingNodeConfig.getUniqueId());
        PlatoAssert.nullException(() -> "checkComingNodeAfter 有 afterHandler 但是没拿到", ymlAfterHandler);
        Set<String> notShouldRunNodes = ymlAfterHandler.notShouldRunNodes(graphRunningInfo);
        if (CollectionUtils.isNotEmpty(notShouldRunNodes)) {
            return notShouldRunNodes.contains(abstractYmlNode.getNodeConfig().getUniqueId())
                   ? MessageEnum.COMING_NODE_LIMIT_CURRENT_RUN.getMes() : StringUtils.EMPTY;
        }
        return StringUtils.EMPTY;
    }

    private Pair<NodeConfig, GraphRunningInfo> getPerData(AbstractNodeProxy comingNode) {
        NodeConfig nodeConfig = ((NodeYmlProxy<?, ?>) comingNode).getAbstractYmlNode().getNodeConfig();
        PlatoAssert.nullException(() -> "NodeYmlProxy nodeConfig error", nodeConfig);
        GraphRunningInfo graphRunningInfo = GraphHolder.getGraphRunningInfo(nodeConfig.getGraphId(), graphTraceId);
        PlatoAssert.nullException(() -> "checkShouldRun graphRunningInfo error", graphRunningInfo);
        return Pair.of(nodeConfig, graphRunningInfo);
    }

    private P paramHandle(NodeYmlProxy<?, ?> comingNode, GraphRunningInfo graphRunningInfo) {
        AbstractYmlNode<?, ?> comingAbstractYmlNode = comingNode.getAbstractYmlNode();
        PlatoAssert.nullException(() -> "paramHandle comingAbstractYmlNode is null", comingAbstractYmlNode);
        NodeConfig comingNodeConfig = comingAbstractYmlNode.getNodeConfig();
        String comingUniqueId = comingNodeConfig.getUniqueId();
        NodeRunningInfo nodeRunningInfo = graphRunningInfo.getNodeRunningInfo(comingUniqueId);
        ResultData resultData = nodeRunningInfo.getResultData();
        Object data = resultData.getData();
        NodeConfig nodeConfig = abstractYmlNode.getNodeConfig();
        String preHandler = nodeConfig.getPreHandler();
        if (StringUtils.isNotBlank(preHandler)) {
            YmlPreHandler ymlPreHandler =
                    HandlerHolder.getYmlPreHandler(nodeConfig.getGraphId(), nodeConfig.getUniqueId());
            return (P) ymlPreHandler.paramHandle(graphRunningInfo);
        }
        return (P) data;
    }

    @Override
    public void runNext(ExecutorService executorService) {
        NodeConfig nodeConfig = abstractYmlNode.getNodeConfig();
        if (Objects.isNull(nodeConfig) || StringUtils.isBlank(nodeConfig.getNext())) {
            return;
        }
        List<String> nextNodes = Splitter.on(",").trimResults().splitToList(nodeConfig.getNext());
        CompletableFuture[] completableFutures = new CompletableFuture[nextNodes.size()];
        for (int i = 0; i < nextNodes.size(); i++) {
            final int finalI = i;
            AbstractYmlNode nextAbstractYmlNode =
                    NodeHolder.getAbstractYmlNode(nodeConfig.getGraphId(), nextNodes.get(finalI));
            completableFutures[finalI] = CompletableFuture.runAsync(
                    () -> new NodeYmlProxy(nextAbstractYmlNode, graphTraceId).run(this, executorService),
                    executorService);
        }
        try {
            CompletableFuture.allOf(completableFutures).get(DEFAULT_TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            log.error("runNext异常le{}", e.getMessage(), e);
            throw new PlatoException("runNext异常le");
        }
    }
}
