package com.example.plato.element;

import com.example.plato.exception.PlatoException;
import com.example.plato.handler.AfterHandler;
import com.example.plato.handler.INodeWork;
import com.example.plato.handler.PreHandler;
import com.example.plato.holder.GraphHolder;
import com.example.plato.runningData.GraphRunningInfo;
import com.example.plato.runningData.MessageEnum;
import com.example.plato.runningData.NodeResultStatus;
import com.example.plato.runningData.NodeRunningInfo;
import com.example.plato.runningData.ResultData;
import com.example.plato.util.SystemClock;
import com.example.plato.util.TraceUtil;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

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
@EqualsAndHashCode(callSuper = true)
public class NodeBeanProxy<P, R> extends AbstractNodeProxy {

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
    public void run(AbstractNodeProxy comingNode, ExecutorService executorService) {
        traceId = TraceUtil.getRandomTraceId();
        if (run(comingNode)) {
            runNext(executorService);
        }
    }

    @Override
    boolean run(AbstractNodeProxy comingNode) {
        P p;
        if (Objects.nonNull(comingNode)) {
            NodeLoadByBean<?, ?> comingNodeLoadByBean = ((NodeBeanProxy<?, ?>) comingNode).getNodeLoadByBean();
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
        R result = null;
        ResultData<R> resultData = ResultData.getFail(MessageEnum.CLIENT_ERROR.getMes(), NodeResultStatus.ERROR);
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
            NodeRunningInfo<R> nodeRunningInfo = new NodeRunningInfo<>(graphTraceId, traceId,
                    nodeLoadByBean.getGraphId(), nodeLoadByBean.getUniqueId(), resultData);
            nodeRunningInfo.build();
            iNodeWork.hook(p, resultData);
        }
        return true;
    }

    boolean checkShouldRun(GraphRunningInfo graphRunningInfo, NodeLoadByBean<?, ?> comingNodeLoadByBean) {
        String limitMes;
        if (StringUtils.isNotBlank(limitMes = checkSuicide(graphRunningInfo))
                || StringUtils.isNotBlank(limitMes = checkComingNodeAfter(comingNodeLoadByBean, graphRunningInfo))
                || StringUtils.isNotBlank(limitMes = checkPreNodes(graphRunningInfo))
                || StringUtils.isNotBlank(limitMes = checkNextHasResult())) {
            changeStatus(NodeResultStatus.INIT, NodeResultStatus.LIMIT_RUN);
            ResultData<R> resultData = new ResultData<>();
            resultData.setNodeResultStatus(NodeResultStatus.LIMIT_RUN);
            resultData.setMes(limitMes);
            new NodeRunningInfo(graphTraceId, traceId, nodeLoadByBean.getGraphId(), nodeLoadByBean.getUniqueId(),
                    resultData).build();
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

    private String checkPreNodes(GraphRunningInfo graphRunningInfo) {
        List<String> preNodes = nodeLoadByBean.getPreNodes();
        Optional<String> firstUnique = preNodes.stream().filter(uniqueId -> {
            NodeRunningInfo<?> nodeRunningInfo = graphRunningInfo.getNodeRunningInfo(uniqueId);
            if (Objects.isNull(nodeRunningInfo)) {
                return true;
            }
            NodeResultStatus nodeResultStatus = nodeRunningInfo.getResultData().getNodeResultStatus();
            if (NodeResultStatus.getExceptionStatus().contains(nodeResultStatus)) {
                return true;
            }
            ResultData<?> resultData = nodeRunningInfo.getResultData();
            return resultData == null || !NodeResultStatus.EXECUTED.equals(resultData.getNodeResultStatus());
        }).findFirst();
        return firstUnique.isPresent() ? MessageEnum.PRE_NOT_HAS_RESULT.getMes() : StringUtils.EMPTY;
    }

    private String checkComingNodeAfter(NodeLoadByBean<?, ?> comingNodeLoadByBean, GraphRunningInfo graphRunningInfo) {
        AfterHandler afterHandler = comingNodeLoadByBean.getAfterHandler();
        if (Optional.ofNullable(afterHandler).isPresent()) {
            Set<String> notShouldRunNodes = afterHandler.notShouldRunNodes(graphRunningInfo);
            return !CollectionUtils.isNotEmpty(notShouldRunNodes)
                           || !notShouldRunNodes.contains(nodeLoadByBean.getUniqueId())
                   ? StringUtils.EMPTY : MessageEnum.COMING_NODE_LIMIT_CURRENT_RUN.getMes();
        }
        return StringUtils.EMPTY;
    }

    @Override
    void runNext(ExecutorService executorService) {
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
        if (Objects.isNull(comingNodeRunningInfo)) {
            throw new PlatoException("paramHandle execute error");
        }
        if (Optional.ofNullable(preHandler).isPresent()) {
            return preHandler.paramHandle(graphRunningInfo);
        } else if (Optional.ofNullable(comingNodeRunningInfo.getResultData().getData()).isPresent()) {
            return (P) comingNodeRunningInfo.getResultData().getData();
        }
        throw new PlatoException("paramHandle error");
    }

    private void changeStatus(NodeResultStatus fromStatus, NodeResultStatus toStatus) {
        if (!compareAndSetState(fromStatus, toStatus)) {
            log.error("NodeResultStatus change status error");
            throw new PlatoException("NodeResultStatus change status error");
        }
    }
}
