package com.example.plato.element;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

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
public class NodeYmlProxy<P, R> extends AbstractNodeProxy<P, R> {

    @Getter
    @Setter
    private AbstractYmlNode<P, R> abstractYmlNode;

    public NodeYmlProxy(AbstractYmlNode<P, R> abstractYmlNode, P p,
            GraphRunningInfo<R> graphRunningInfo) {
        this(abstractYmlNode, graphRunningInfo);
        setP(p);
    }

    public NodeYmlProxy(AbstractYmlNode<P, R> abstractYmlNode, GraphRunningInfo<R> graphRunningInfo) {
        this.abstractYmlNode = abstractYmlNode;
        setGraphRunningInfo(graphRunningInfo);
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
                return abstractYmlNode.work(getP());
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
            List<String> preNodes = Arrays.asList(pre.split(","));
            limitMes = checkPreNodes(preNodes, comingNodeConfig.getUniqueId());
            if (StringUtils.isNotBlank(limitMes)) {
                setLimitResult(limitMes, nodeConfig.getGraphId(), nodeConfig.getUniqueId());
                return false;
            }
        }
        if (StringUtils.isBlank(limitMes = checkSuicide(nodeConfig))
                && StringUtils.isBlank(
                limitMes = checkComingNodeAfter(comingNodeConfig))) {
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

    public String checkComingNodeAfter(NodeConfig comingNodeConfig) {
        if (StringUtils.isBlank(comingNodeConfig.getAfterHandler())) {
            return StringUtils.EMPTY;
        }
        YmlAfterHandler ymlAfterHandler =
                HandlerHolder.getYmlAfterHandler(comingNodeConfig.getGraphId(), comingNodeConfig.getUniqueId());
        PlatoAssert.nullException(() -> "checkComingNodeAfter 有 afterHandler 但是没拿到", ymlAfterHandler);
        Set<String> notShouldRunNodes = ymlAfterHandler.notShouldRunNodes(getGraphRunningInfo());
        if (CollectionUtils.isNotEmpty(notShouldRunNodes)) {
            return notShouldRunNodes.contains(abstractYmlNode.getNodeConfig().getUniqueId())
                   ? MessageEnum.COMING_NODE_LIMIT_CURRENT_RUN.getMes() : StringUtils.EMPTY;
        }
        return StringUtils.EMPTY;
    }

    @SuppressWarnings("unchecked")
    private P paramHandle(NodeYmlProxy<?, ?> comingNode) {
        AbstractYmlNode<?, ?> comingAbstractYmlNode = comingNode.getAbstractYmlNode();
        PlatoAssert.nullException(() -> "paramHandle comingAbstractYmlNode is null", comingAbstractYmlNode);
        NodeConfig comingNodeConfig = comingAbstractYmlNode.getNodeConfig();
        String comingUniqueId = comingNodeConfig.getUniqueId();
        NodeRunningInfo<R> nodeRunningInfo = getGraphRunningInfo().getNodeRunningInfo(comingUniqueId);
        ResultData<R> resultData = nodeRunningInfo.getResultData();
        Object data = resultData.getData();
        NodeConfig nodeConfig = abstractYmlNode.getNodeConfig();
        String preHandler = nodeConfig.getPreHandler();

        if (StringUtils.isNotBlank(preHandler)) {
            YmlPreHandler ymlPreHandler =
                    HandlerHolder.getYmlPreHandler(nodeConfig.getGraphId(), nodeConfig.getUniqueId());
            PlatoAssert.nullException(() -> "paramHandle ymlPreHandler error", ymlPreHandler);
            Object objParam = ymlPreHandler.paramHandle(getGraphRunningInfo());
            if (Objects.nonNull(objParam)) {
                return (P) objParam;
            }
        }
        return (P) data;
    }

    @Override
    public void runNext(ExecutorService executorService) {
        NodeConfig nodeConfig = abstractYmlNode.getNodeConfig();
        if (Objects.isNull(nodeConfig) || StringUtils.isBlank(nodeConfig.getNext())) {
            return;
        }
        List<String> nextNodes = Arrays.asList(nodeConfig.getNext().split(","));
        List<CompletableFuture<Void>> completableFutureList =
                nextNodes.stream().map(nextNodeTemp -> CompletableFuture.runAsync(
                        () -> new NodeYmlProxy<>(NodeHolder.getAbstractYmlNode(nodeConfig.getGraphId(), nextNodeTemp), getGraphRunningInfo()).run(this,
                                executorService), executorService)).collect(Collectors.toList());
        try {
            CompletableFuture.allOf(completableFutureList.toArray(new CompletableFuture[] {}))
                    .get(DEFAULT_TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            log.error("runNext异常le{}", e.getMessage(), e);
            throw new PlatoException("runNext异常le");
        }
    }
}
