package com.example.plato.element;

import com.example.plato.exception.PlatoException;
import com.example.plato.handler.AfterHandler;
import com.example.plato.handler.INodeWork;
import com.example.plato.handler.PreHandler;
import com.example.plato.holder.GraphHolder;
import com.example.plato.runningData.GraphRunningInfo;
import com.example.plato.platoEnum.MessageEnum;
import com.example.plato.platoEnum.NodeResultStatus;
import com.example.plato.runningData.NodeRunningInfo;
import com.example.plato.runningData.ResultData;
import com.example.plato.util.PlatoAssert;
import com.example.plato.util.SystemClock;
import com.example.plato.util.TraceUtil;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/30 16:01
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class NodeBeanProxy<P, R> extends AbstractNodeProxy {

    private String traceId;
    private String graphTraceId;
    private NodeLoadByBean<P, R> nodeLoadByBean;
    private P p;

    public NodeBeanProxy(NodeLoadByBean<P, R> nodeLoadByBean, String graphTraceId, P p) {
        this.nodeLoadByBean = nodeLoadByBean;
        this.graphTraceId = graphTraceId;
        this.p = p;
    }

    public NodeBeanProxy(NodeLoadByBean<P, R> nodeLoadByBean, String graphTraceId) {
        this.nodeLoadByBean = nodeLoadByBean;
        this.graphTraceId = graphTraceId;
    }

    @Override
    public void run(AbstractNodeProxy comingNode, ExecutorService executorService) {
        traceId = TraceUtil.getRandomTraceId();
        if (run(comingNode)) {
            runNext(executorService);
        }
    }

    private Pair<NodeLoadByBean<?, ?>, GraphRunningInfo> getPerData(AbstractNodeProxy comingNode) {
        NodeLoadByBean<?, ?> comingNodeLoadByBean = ((NodeBeanProxy<?, ?>) comingNode).getNodeLoadByBean();
        GraphRunningInfo graphRunningInfo =
                GraphHolder.getGraphRunningInfo(comingNodeLoadByBean.getGraphId(), graphTraceId);
        PlatoAssert.nullException(() -> "getPerData comingNodeLoadByBean & graphRunningInfo error",
                comingNodeLoadByBean, graphRunningInfo);
        return Pair.of(comingNodeLoadByBean, graphRunningInfo);
    }

    @Override
    public boolean run(AbstractNodeProxy comingNode) {
        if (Objects.nonNull(comingNode)) {
            Pair<NodeLoadByBean<?, ?>, GraphRunningInfo> perData = getPerData(comingNode);
            GraphRunningInfo graphRunningInfo = perData.getRight();
            NodeLoadByBean<?, ?> comingNodeLoadByBean = perData.getLeft();
            if (!checkShouldRun(graphRunningInfo, comingNodeLoadByBean)) {
                return false;
            }
            p = paramHandle(graphRunningInfo, comingNodeLoadByBean);
        }
        changeStatus(NodeResultStatus.INIT, NodeResultStatus.EXECUTING);
        INodeWork<P, R> iNodeWork = nodeLoadByBean.getINodeWork();
        R result = null;
        ResultData resultData = ResultData.getFail(MessageEnum.CLIENT_ERROR.getMes(), NodeResultStatus.ERROR);
        long startTime = SystemClock.now();
        long endTime = SystemClock.now();
        try {
            result = iNodeWork.work(p);
            endTime = SystemClock.now();
            changeStatus(NodeResultStatus.EXECUTING, NodeResultStatus.EXECUTED);
            resultData = ResultData.build(result, NodeResultStatus.EXECUTED, "success", endTime - startTime);
        } catch (Exception e) {
            endTime = SystemClock.now();
            log.error(String.format("%s\t{}", MessageEnum.CLIENT_ERROR), nodeLoadByBean.getUniqueId(), e);
            changeStatus(NodeResultStatus.EXECUTING, NodeResultStatus.ERROR);
            resultData = ResultData.build(result, NodeResultStatus.ERROR, "fail", endTime - startTime);
            return false;
        } finally {
            log.info("{}\t执行耗时{}", nodeLoadByBean.getUniqueId(), endTime - startTime);
            NodeRunningInfo nodeRunningInfo = new NodeRunningInfo<>(graphTraceId, traceId,
                    nodeLoadByBean.getGraphId(), nodeLoadByBean.getUniqueId(), resultData);
            nodeRunningInfo.build();
            iNodeWork.hook(p, resultData);
        }
        return true;
    }

    private boolean checkShouldRun(GraphRunningInfo graphRunningInfo, NodeLoadByBean<?, ?> comingNodeLoadByBean) {
        String limitMes;
        if (StringUtils.isNotBlank(limitMes = checkSuicide(graphRunningInfo))
                || StringUtils.isNotBlank(limitMes = checkComingNodeAfter(comingNodeLoadByBean, graphRunningInfo))
                || StringUtils.isNotBlank(limitMes = checkPreNodes(graphRunningInfo,
                nodeLoadByBean.getPreNodes(), comingNodeLoadByBean.getUniqueId()))
                || StringUtils.isNotBlank(limitMes = checkNextHasResult())) {
            setLimitResult(limitMes, graphTraceId, traceId, nodeLoadByBean.getGraphId(), nodeLoadByBean.getUniqueId());
            return false;
        }
        return true;
    }

    private String checkNextHasResult() {
        List<NodeLoadByBean<?, ?>> nextNodes = nodeLoadByBean.getNextNodes();
        if (CollectionUtils.isEmpty(nextNodes) && !nodeLoadByBean.isCheckNextHasResult()) {
            return StringUtils.EMPTY;
        }
        Optional<NodeLoadByBean<?, ?>> first = nextNodes.stream().filter(temp -> {
            boolean contains = temp.getPreNodes().contains(nodeLoadByBean.getUniqueId());
            NodeRunningInfo nodeRunningInfo =
                    GraphHolder.getNodeRunningInfo(nodeLoadByBean.getGraphId(), graphTraceId, temp.getUniqueId());
            return contains && Objects.nonNull(nodeRunningInfo);
        }).findFirst();
        return first.isPresent() ? MessageEnum.NEXT_NODE_HAS_RESULT.getMes() : StringUtils.EMPTY;
    }

    private String checkSuicide(GraphRunningInfo graphRunningInfo) {
        PreHandler<P> preHandler = nodeLoadByBean.getPreHandler();
        return Objects.isNull(preHandler) || !preHandler.suicide(graphRunningInfo)
               ? StringUtils.EMPTY : MessageEnum.SUICIDE.getMes();
    }

    /**
     * if else
     */
    private String checkComingNodeAfter(NodeLoadByBean<?, ?> comingNodeLoadByBean, GraphRunningInfo graphRunningInfo) {
        AfterHandler afterHandler = comingNodeLoadByBean.getAfterHandler();
        if (Optional.ofNullable(afterHandler).isPresent()) {
            Set<String> notShouldRunNodes = afterHandler.notShouldRunNodes(graphRunningInfo);
            return (CollectionUtils.isEmpty(notShouldRunNodes)
                    || !notShouldRunNodes.contains(nodeLoadByBean.getUniqueId()))
                   ? StringUtils.EMPTY : MessageEnum.COMING_NODE_LIMIT_CURRENT_RUN.getMes();
        }
        return StringUtils.EMPTY;
    }

    @Override
    public void runNext(ExecutorService executorService) {
        List<NodeLoadByBean<?, ?>> nextNodes = nodeLoadByBean.getNextNodes();
        if (CollectionUtils.isEmpty(nextNodes)) {
            return;
        }
        CompletableFuture[] completableFutures = new CompletableFuture[nextNodes.size()];
        for (int i = 0; i < nextNodes.size(); i++) {
            final int finalI = i;
            completableFutures[finalI] = CompletableFuture.runAsync(
                    () -> new NodeBeanProxy(nextNodes.get(finalI), graphTraceId).run(this, executorService),
                    executorService);
        }
        try {
            CompletableFuture.allOf(completableFutures).get(DEFAULT_TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            log.error("runNext异常le{}", e.getMessage(), e);
            throw new PlatoException("runNext异常le");
        }
    }


    private P paramHandle(GraphRunningInfo graphRunningInfo, NodeLoadByBean<?, ?> comingNodeLoadByBean) {
        PreHandler<P> preHandler = nodeLoadByBean.getPreHandler();
        NodeRunningInfo<?> comingNodeRunningInfo =
                graphRunningInfo.getNodeRunningInfo(comingNodeLoadByBean.getUniqueId());
        PlatoAssert.nullException(() -> "paramHandle comingNodeRunningInfo error", comingNodeRunningInfo);
        if (Optional.ofNullable(preHandler).isPresent()) {
            return preHandler.paramHandle(graphRunningInfo);
        }
        if (Optional.ofNullable(comingNodeRunningInfo.getResultData().getData()).isPresent()) {
            return (P) comingNodeRunningInfo.getResultData().getData();
        }
        throw new PlatoException("paramHandle error");
    }

}
