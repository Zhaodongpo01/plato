package com.example.plato.element;

import com.example.plato.exception.PlatoException;
import com.example.plato.handler.AfterHandler;
import com.example.plato.handler.PreHandler;
import com.example.plato.holder.GraphHolder;
import com.example.plato.runningData.GraphRunningInfo;
import com.example.plato.runningData.NodeResultStatus;
import com.example.plato.runningData.NodeRunningInfo;
import com.example.plato.runningData.ResultData;
import com.example.plato.util.TraceUtil;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/30 16:01
 */
@Slf4j
@Data
public class NodeBeanProxy<P, R> extends AbstractNode {

    private String traceId;
    private String graphTraceId;
    private NodeLoadByBean<P, R> nodeLoadByBean;
    private AtomicReference<NodeResultStatus> statusAtomicReference = new AtomicReference<>(NodeResultStatus.INIT);

    private void setStatusAtomicReference() {
        throw new PlatoException("private 禁止调用");
    }

    public boolean compareAndSetState(NodeResultStatus expect, NodeResultStatus update) {
        return this.statusAtomicReference.compareAndSet(expect, update);
    }

    public NodeBeanProxy(NodeLoadByBean<P, R> nodeLoadByBean, String graphTraceId) {
        this.nodeLoadByBean = nodeLoadByBean;
        this.graphTraceId = graphTraceId;
    }

    /**
     * 这个方法是否可以放到,抽象类里面去
     */
    @Override
    public void run(AbstractNode comingNode, ExecutorService executorService) {
        traceId = TraceUtil.getRandomTraceId();
        run(comingNode);
        runNext(executorService);
    }

    @Override
    protected boolean run(AbstractNode comingNode) {
        P p;
        if (Objects.nonNull(comingNode)) {
            NodeLoadByBean<?, ?> comingNodeLoadByBean = ((NodeBeanProxy) comingNode).getNodeLoadByBean();
            GraphRunningInfo graphRunningInfo =
                    GraphHolder.getGraphRunningInfo(nodeLoadByBean.getGraphId(), graphTraceId);
            if (ObjectUtils.anyNull(comingNodeLoadByBean, graphRunningInfo)) {
                throw new PlatoException("checkShouldRun graphRunningInfo error");
            }
            if (!checkShouldRun(graphRunningInfo, comingNodeLoadByBean)) {
                return false;
            }
            p = paramHandle(graphRunningInfo, comingNodeLoadByBean);
        } else {
            p = nodeLoadByBean.getP();
        }
        changeStatus(NodeResultStatus.INIT, NodeResultStatus.EXECUTING);
        INodeWork<P, R> iNodeWork = nodeLoadByBean.getINodeWork();
        R result = iNodeWork.work(p);
        changeStatus(NodeResultStatus.EXECUTING, NodeResultStatus.EXECUTED);
        ResultData<R> resultData = ResultData.build(result, NodeResultStatus.EXECUTED, "success");
        new NodeRunningInfo<R>(graphTraceId, traceId, nodeLoadByBean.getGraphId(), nodeLoadByBean.getUniqueId(),
                resultData).build();
        iNodeWork.hook(p, resultData);
        return true;
    }

    protected boolean checkShouldRun(GraphRunningInfo graphRunningInfo, NodeLoadByBean<?, ?> comingNodeLoadByBean) {
        if (!checkCurrentNodeRunEnable(graphRunningInfo)
                || !checkComingNodeAfter(comingNodeLoadByBean, graphRunningInfo)
                || checkPreNodes(graphRunningInfo)) {
            changeStatus(NodeResultStatus.INIT, NodeResultStatus.LIMIT_RUN);
            ResultData<R> resultData = new ResultData<>();
            resultData.setNodeResultStatus(NodeResultStatus.LIMIT_RUN);
            resultData.setMes("限制执行");
            new NodeRunningInfo<R>(graphTraceId, traceId, nodeLoadByBean.getGraphId(), nodeLoadByBean.getUniqueId(),
                    resultData).build();
            return false;
        }
        return true;
    }

    private boolean checkCurrentNodeRunEnable(GraphRunningInfo graphRunningInfo) {
        PreHandler<P> preHandler = nodeLoadByBean.getPreHandler();
        if (Optional.ofNullable(preHandler).isPresent()) {
            return preHandler.runEnable(graphRunningInfo);
        }
        return true;
    }

    @Override
    protected void runNext(ExecutorService executorService) {
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
            log.error("runNext异常{}", e.getMessage(), e);
            throw new PlatoException("runNext异常");
        }
    }

    /**
     * TODO
     * A和B并行到C。初始编排C强依赖于A和B。当A到C时，C由于还没有B的结果暂时不能执行。
     * 然后B此时执行完之后根据结果是不让C执行。那么此时应考虑继续AC链路继续执行。
     */
    private boolean checkPreNodes(GraphRunningInfo graphRunningInfo) {
        List<String> preNodes = nodeLoadByBean.getPreNodes();
        Optional<String> firstUnique = preNodes.stream().filter(uniqueId -> {
            NodeRunningInfo nodeRunningInfo = graphRunningInfo.getNodeRunningInfo(uniqueId);
            if (Objects.isNull(nodeRunningInfo)) {
                return true;
            }
            NodeResultStatus nodeResultStatus = nodeRunningInfo.getResultData().getNodeResultStatus();
            if (NodeResultStatus.getExceptionStatus().contains(nodeResultStatus)) {
                return true;
            }
            ResultData resultData = nodeRunningInfo.getResultData();
            return resultData == null || !NodeResultStatus.EXECUTED.equals(resultData.getNodeResultStatus());
        }).findFirst();
        return firstUnique.isPresent();
    }

    private boolean checkComingNodeAfter(NodeLoadByBean<?, ?> comingNodeLoadByBean, GraphRunningInfo graphRunningInfo) {
        AfterHandler afterHandler = comingNodeLoadByBean.getAfterHandler();
        if (Optional.ofNullable(afterHandler).isPresent()) {
            Set<String> notShouldRunNodes = afterHandler.notShouldRunNodes(graphRunningInfo);
            if (CollectionUtils.isNotEmpty(notShouldRunNodes) && notShouldRunNodes.contains(
                    nodeLoadByBean.getUniqueId())) {
                return false;
            }
        }
        return true;
    }

    private P paramHandle(GraphRunningInfo graphRunningInfo, NodeLoadByBean<?, ?> comingNodeLoadByBean) {
        PreHandler<P> preHandler = nodeLoadByBean.getPreHandler();
        NodeRunningInfo comingNodeRunningInfo = graphRunningInfo.getNodeRunningInfo(comingNodeLoadByBean.getUniqueId());
        if (Objects.isNull(comingNodeRunningInfo)) {
            throw new PlatoException("paramHandle execute error");
        }
        return Optional.ofNullable(preHandler).isPresent()
               ? (P) preHandler.paramHandle(graphRunningInfo)
               : (P) comingNodeRunningInfo.getResultData().getData();
    }

    private boolean changeStatus(NodeResultStatus fromStatus, NodeResultStatus toStatus) {
        if (!compareAndSetState(fromStatus, toStatus)) {
            log.error("NodeResultStatus change status error");
            throw new PlatoException("NodeResultStatus change status error");
        }
        return true;
    }
}
