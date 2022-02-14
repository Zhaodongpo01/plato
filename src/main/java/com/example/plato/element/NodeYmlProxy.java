package com.example.plato.element;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;

import com.example.plato.exception.PlatoException;
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
import com.example.plato.util.TraceUtil;

import com.google.common.base.Splitter;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
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
@EqualsAndHashCode(callSuper = true)
public class NodeYmlProxy<P, R> extends AbstractNodeProxy {

    @Getter
    @Setter
    private AbstractYmlNode<P, R> abstractYmlNode;

    public NodeYmlProxy(AbstractYmlNode<P, R> abstractYmlNode, String graphTraceId, P p,
            GraphRunningInfo<R> graphRunningInfo) {
        this(abstractYmlNode, graphTraceId, graphRunningInfo);
        setP(p);
    }

    public NodeYmlProxy(AbstractYmlNode<P, R> abstractYmlNode, String graphTraceId,
            GraphRunningInfo<R> graphRunningInfo) {
        this.abstractYmlNode = abstractYmlNode;
        setGraphRunningInfo(graphRunningInfo);
        setGraphTraceId(graphTraceId);
    }

    @Override
    public void run(AbstractNodeProxy comingNode, ExecutorService executorService) {
        setTraceId(TraceUtil.getRandomTraceId());
        if (run(comingNode)) {
            runNext(executorService);
        }
    }

    @Override
    public boolean run(AbstractNodeProxy comingNode) {
        NodeConfig currentNodeConfig = abstractYmlNode.getNodeConfig();
        if (Optional.ofNullable(comingNode).isPresent()) {
            NodeConfig comingNodeConfig = ((NodeYmlProxy<?, ?>) comingNode).getAbstractYmlNode().getNodeConfig();
            if (!checkShouldRun(comingNodeConfig)) {
                return false;
            }
            setP(paramHandle((NodeYmlProxy<?, ?>) comingNode));
        }
        changeStatus(NodeResultStatus.INIT, NodeResultStatus.EXECUTING);
        Pair<Boolean, ResultData<R>> executor = executor(param -> {
            try {
                return abstractYmlNode.work((P) getP());
            } catch (InterruptedException e) {
                throw new PlatoException("NodeYmlProxy client run error");
            }
        }, currentNodeConfig.getUniqueId(), currentNodeConfig.getGraphId());
        return executor.getLeft();
    }

    private boolean checkShouldRun(NodeConfig comingNodeConfig) {
        NodeConfig nodeConfig = abstractYmlNode.getNodeConfig();
        String pre = nodeConfig.getPre();
        String limitMes;
        if (StringUtils.isNotBlank(pre)) {
            List<String> preNodes = Splitter.on(",").trimResults().splitToList(pre);
            limitMes = checkPreNodes(preNodes, comingNodeConfig.getUniqueId());
            if (StringUtils.isNotBlank(limitMes)) {
                setLimitResult(limitMes, nodeConfig.getGraphId(), nodeConfig.getUniqueId());
                return false;
            }
        }
        if (StringUtils.isBlank(limitMes = checkSuicide(nodeConfig))
                && StringUtils.isBlank(
                limitMes = checkComingNodeAfter(comingNodeConfig, getGraphRunningInfo()))) {
            return true;
        }
        setLimitResult(limitMes, nodeConfig.getGraphId(), nodeConfig.getUniqueId());
        return false;
    }

    public String checkSuicide(NodeConfig nodeConfig) {
        if (StringUtils.isBlank(nodeConfig.getPreHandler())) {
            return StringUtils.EMPTY;
        }
        YmlPreHandler ymlPreHandler = HandlerHolder.getYmlPreHandler(nodeConfig.getGraphId(), nodeConfig.getUniqueId());
        PlatoAssert.nullException(() -> "checkSuicide ymlPreHandler is null", ymlPreHandler);
        return ymlPreHandler.suicide(getGraphRunningInfo()) ? MessageEnum.SUICIDE.getMes() : StringUtils.EMPTY;
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

    private P paramHandle(NodeYmlProxy<?, ?> comingNode) {
        AbstractYmlNode<?, ?> comingAbstractYmlNode = comingNode.getAbstractYmlNode();
        PlatoAssert.nullException(() -> "paramHandle comingAbstractYmlNode is null", comingAbstractYmlNode);
        NodeConfig comingNodeConfig = comingAbstractYmlNode.getNodeConfig();
        String comingUniqueId = comingNodeConfig.getUniqueId();
        NodeRunningInfo nodeRunningInfo = getGraphRunningInfo().getNodeRunningInfo(comingUniqueId);
        ResultData resultData = nodeRunningInfo.getResultData();
        Object data = resultData.getData();
        NodeConfig nodeConfig = abstractYmlNode.getNodeConfig();
        String preHandler = nodeConfig.getPreHandler();
        if (StringUtils.isNotBlank(preHandler)) {
            YmlPreHandler ymlPreHandler =
                    HandlerHolder.getYmlPreHandler(nodeConfig.getGraphId(), nodeConfig.getUniqueId());
            return (P) ymlPreHandler.paramHandle(getGraphRunningInfo());
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
                    () -> new NodeYmlProxy<>(nextAbstractYmlNode, getGraphTraceId(), getGraphRunningInfo()).run(this,
                            executorService), executorService);
        }
        try {
            CompletableFuture.allOf(completableFutures).get(DEFAULT_TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            log.error("runNext异常le{}", e.getMessage(), e);
            throw new PlatoException("runNext异常le");
        }
    }
}
