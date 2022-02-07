package com.example.plato.element;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;

import com.example.plato.exception.PlatoException;
import com.example.plato.holder.GraphHolder;
import com.example.plato.holder.NodeHolder;
import com.example.plato.loader.config.NodeConfig;
import com.example.plato.loader.ymlNode.AbstractYmlNode;
import com.example.plato.platoEnum.MessageEnum;
import com.example.plato.platoEnum.NodeResultStatus;
import com.example.plato.runningData.*;
import com.example.plato.util.SystemClock;
import com.example.plato.util.TraceUtil;

import com.google.common.base.Splitter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

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
            NodeConfig nodeConfig = perData.getLeft();
            GraphRunningInfo graphRunningInfo = perData.getRight();
            if (!checkShouldRun(nodeConfig, graphRunningInfo)) {
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
        } catch (InterruptedException e) {
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

    private boolean checkShouldRun(NodeConfig nodeConfig, GraphRunningInfo graphRunningInfo) {
        return true;
    }

    private Pair<NodeConfig, GraphRunningInfo> getPerData(AbstractNodeProxy comingNode) {
        NodeConfig nodeConfig = ((NodeYmlProxy<?, ?>) comingNode).getAbstractYmlNode().getNodeConfig();
        if (Objects.isNull(nodeConfig)) {
            log.error("NodeYmlProxy nodeConfig error");
            return null;
        }
        GraphRunningInfo graphRunningInfo = GraphHolder.getGraphRunningInfo(nodeConfig.getGraphId(), graphTraceId);
        if (Objects.isNull(graphRunningInfo)) {
            throw new PlatoException("checkShouldRun graphRunningInfo error");
        }
        return Pair.of(nodeConfig, graphRunningInfo);
    }

    private P paramHandle(NodeYmlProxy<?, ?> comingNode, GraphRunningInfo graphRunningInfo) {
        AbstractYmlNode<?, ?> comingAbstractYmlNode = comingNode.getAbstractYmlNode();
        if (comingAbstractYmlNode == null) {
            return null;
        }
        NodeConfig comingNodeConfig = comingAbstractYmlNode.getNodeConfig();
        String comingUniqueId = comingNodeConfig.getUniqueId();
        NodeRunningInfo nodeRunningInfo = graphRunningInfo.getNodeRunningInfo(comingUniqueId);
        ResultData resultData = nodeRunningInfo.getResultData();
        Object data = resultData.getData();
        NodeConfig nodeConfig = abstractYmlNode.getNodeConfig();
        String preHandler = nodeConfig.getPreHandler();
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
