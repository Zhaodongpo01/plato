package com.example.plato.element;

import com.example.plato.exception.PlatoException;
import com.example.plato.handler.AfterHandler;
import com.example.plato.handler.INodeWork;
import com.example.plato.handler.PreHandler;
import com.example.plato.runningData.GraphRunningInfo;
import com.example.plato.platoEnum.MessageEnum;
import com.example.plato.platoEnum.NodeResultStatus;
import com.example.plato.runningData.NodeRunningInfo;
import com.example.plato.runningData.ResultData;
import com.example.plato.util.PlatoAssert;
import com.example.plato.util.TraceUtil;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/30 16:01
 */
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class NodeBeanProxy<P, R> extends AbstractNodeProxy<P, R> {

    @Getter
    @Setter
    private NodeLoadByBean<P, R> nodeLoadByBean;

    public NodeBeanProxy(NodeLoadByBean<P, R> nodeLoadByBean, String graphTraceId, P p,
            GraphRunningInfo<R> graphRunningInfo) {
        this(nodeLoadByBean, graphTraceId, graphRunningInfo);
        setP(p);
    }

    public NodeBeanProxy(NodeLoadByBean<P, R> nodeLoadByBean, String graphTraceId,
            GraphRunningInfo<R> graphRunningInfo) {
        this.nodeLoadByBean = nodeLoadByBean;
        setGraphTraceId(graphTraceId);
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
        if (Objects.nonNull(comingNode)) {
            NodeLoadByBean<?, ?> comingNodeLoadByBean = ((NodeBeanProxy<?, ?>) comingNode).getNodeLoadByBean();
            if (!checkShouldRun(comingNodeLoadByBean)) {
                return false;
            }
            setP(paramHandle(comingNodeLoadByBean));
        }
        changeStatus(NodeResultStatus.INIT, NodeResultStatus.EXECUTING);
        INodeWork<P, R> iNodeWork = nodeLoadByBean.getINodeWork();
        Pair<Boolean, ResultData<R>> executor = executor(param -> {
            try {
                return iNodeWork.work(getP());
            } catch (InterruptedException e) {
                throw new PlatoException("NodeBeanProxy client run error");
            }
        }, nodeLoadByBean.getUniqueId(), nodeLoadByBean.getGraphId());
        iNodeWork.hook(getP(), executor.getRight());
        return executor.getLeft();
    }

    private boolean checkShouldRun(NodeLoadByBean<?, ?> comingNodeLoadByBean) {
        String limitMes;
        if (StringUtils.isNotBlank(limitMes = checkSuicide())
                || StringUtils.isNotBlank(limitMes = checkComingNodeAfter(comingNodeLoadByBean))
                || StringUtils.isNotBlank(
                limitMes = checkPreNodes(nodeLoadByBean.getPreNodes(), comingNodeLoadByBean.getUniqueId()))
                || StringUtils.isNotBlank(limitMes = checkNextHasResult())) {
            setLimitResult(limitMes, nodeLoadByBean.getGraphId(), nodeLoadByBean.getUniqueId());
            return false;
        }
        return true;
    }

    private String checkNextHasResult() {
        List<NodeLoadByBean<?, ?>> nextNodes = nodeLoadByBean.getNextNodes();
        if (CollectionUtils.isEmpty(nextNodes) && !nodeLoadByBean.isCheckNextResult()) {
            return StringUtils.EMPTY;
        }
        Optional<NodeLoadByBean<?, ?>> first = nextNodes.stream().filter(temp -> {
            boolean contains = temp.getPreNodes().contains(nodeLoadByBean.getUniqueId());
            NodeRunningInfo<R> nodeRunningInfo = getGraphRunningInfo().getNodeRunningInfo(temp.getUniqueId());
            return contains && Objects.nonNull(nodeRunningInfo);
        }).findFirst();
        return first.isPresent() ? MessageEnum.NEXT_NODE_HAS_RESULT.getMes() : StringUtils.EMPTY;
    }

    private String checkSuicide() {
        PreHandler<P> preHandler = nodeLoadByBean.getPreHandler();
        return Objects.isNull(preHandler) || !preHandler.suicide(getGraphRunningInfo())
               ? StringUtils.EMPTY : MessageEnum.SUICIDE.getMes();
    }

    /**
     * if else
     */
    private String checkComingNodeAfter(NodeLoadByBean<?, ?> comingNodeLoadByBean) {
        AfterHandler afterHandler = comingNodeLoadByBean.getAfterHandler();
        if (Optional.ofNullable(afterHandler).isPresent()) {
            Set<String> notShouldRunNodes = afterHandler.notShouldRunNodes(getGraphRunningInfo());
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
        List<CompletableFuture<Void>> completableFutureList =
                nextNodes.stream().map(nodeLoadByBeanTemp -> CompletableFuture.runAsync(
                        () -> new NodeBeanProxy(nodeLoadByBeanTemp, getGraphTraceId(), getGraphRunningInfo()).run(
                                this, executorService), executorService)).collect(Collectors.toList());
        try {
            CompletableFuture.allOf(completableFutureList.toArray(new CompletableFuture[] {}))
                    .get(DEFAULT_TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            log.error("runNext异常le{}", e.getMessage(), e);
            throw new PlatoException("runNext异常le");
        }
    }

    @SuppressWarnings("unchecked")
    private P paramHandle(NodeLoadByBean<?, ?> comingNodeLoadByBean) {
        PreHandler<P> preHandler = nodeLoadByBean.getPreHandler();
        NodeRunningInfo<?> comingNodeRunningInfo =
                getGraphRunningInfo().getNodeRunningInfo(comingNodeLoadByBean.getUniqueId());
        PlatoAssert.nullException(() -> "paramHandle comingNodeRunningInfo error", comingNodeRunningInfo);
        if (Optional.ofNullable(preHandler).isPresent()) {
            return preHandler.paramHandle(getGraphRunningInfo());
        }
        try {
            if (Optional.ofNullable(comingNodeRunningInfo.getResultData().getData()).isPresent()) {
                return (P) comingNodeRunningInfo.getResultData().getData();
            }
        } catch (Exception e) {
            throw new PlatoException("paramHandle error");
        }
        return null;
    }

}
