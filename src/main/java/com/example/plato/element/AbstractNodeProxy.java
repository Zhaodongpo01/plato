package com.example.plato.element;

import com.example.plato.exception.PlatoException;
import com.example.plato.platoEnum.MessageEnum;
import com.example.plato.platoEnum.NodeResultStatus;
import com.example.plato.runningData.GraphRunningInfo;
import com.example.plato.runningData.NodeRunningInfo;
import com.example.plato.runningData.ResultData;
import com.example.plato.util.SystemClock;
import com.google.common.collect.Sets;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/23 11:04 上午
 */
@Slf4j
@Data
public abstract class AbstractNodeProxy<P, R> implements INodeProxy {

    private P p;
    private String traceId;
    private String graphTraceId;
    private GraphRunningInfo<R> graphRunningInfo;

    public static final Long DEFAULT_TIME_OUT = 60_000L;

    private final AtomicReference<NodeResultStatus> statusAtomicReference =
            new AtomicReference<>(NodeResultStatus.INIT);

    private void setStatusAtomicReference() {
        throw new PlatoException("禁止通过set方法设置状态");
    }

    protected boolean compareAndSetState(NodeResultStatus expect, NodeResultStatus update) {
        return this.statusAtomicReference.compareAndSet(expect, update);
    }

    protected void changeStatus(NodeResultStatus fromStatus, NodeResultStatus toStatus) {
        if (!compareAndSetState(fromStatus, toStatus)) {
            log.error("AbstractNodeProxy NodeResultStatus change status error");
            throw new PlatoException(MessageEnum.UPDATE_NODE_STATUS_ERROR.getMes());
        }
    }

    /**
     * A->B->D
     * A->C->D
     * D强依赖于B，但是不强依赖于C。
     * 线程1：C执行完应该执行D，但是D应该等待线程B执行完。
     * 线程2：第一步线程B执行完。第二步执行D
     * 线程1：在线程2的第一步骤执行完之后，线程1D开始执行，并且在执行前已经检测强依赖的B已经执行完成
     * 线程2：线程2的第二步骤又把D执行了一遍。
     * 存在线程1和线程2在切换时，D被执行了两遍。
     * <p>
     * 当当前节点的preNodes不为空时，只有preNode才能触发当前节点执行。
     * 也就是当前Node的非强依赖节点不能触发当前节点执行。
     * <p>
     * 但是如果D节点前面的B和C节点都不是强依赖节点，那么D节点将执行两次。
     */
    protected String checkPreNodes(List<String> preNodes, String comingNodeUniqueId) {
        if (CollectionUtils.isNotEmpty(preNodes) && !Sets.newHashSet(preNodes).contains(comingNodeUniqueId)) {
            return MessageEnum.COMING_NODE_IS_NOT_PRE_NODE.getMes();
        }
        Optional<String> firstUnique = preNodes.parallelStream().filter(uniqueId -> {
            NodeRunningInfo<?> nodeRunningInfo = graphRunningInfo.getNodeRunningInfo(uniqueId);
            if (Objects.isNull(nodeRunningInfo)) {
                return true;
            }
            NodeResultStatus nodeResultStatus = nodeRunningInfo.getResultData().getNodeResultStatus();
            if (NodeResultStatus.getAbnormalStatus().contains(nodeResultStatus)) {
                return true;
            }
            ResultData<?> resultData = nodeRunningInfo.getResultData();
            return resultData == null || !NodeResultStatus.EXECUTED.equals(resultData.getNodeResultStatus());
        }).findFirst();
        return firstUnique.isPresent() ? MessageEnum.PRE_NOT_HAS_RESULT.getMes() : StringUtils.EMPTY;
    }

    protected void setLimitResult(String limitMes, String graphId, String uniqueId) {
        changeStatus(NodeResultStatus.INIT, NodeResultStatus.LIMIT_RUN);
        ResultData<R> resultData = new ResultData<>();
        resultData.setNodeResultStatus(NodeResultStatus.LIMIT_RUN);
        resultData.setMes(limitMes);
        NodeRunningInfo<R> nodeRunningInfo =
                new NodeRunningInfo<>(graphTraceId, traceId, graphId, uniqueId, resultData);
        graphRunningInfo.putNodeRunningInfo(uniqueId, nodeRunningInfo);
    }

    protected Pair<Boolean, ResultData<R>> executor(Function<P, R> workFunction, String uniqueId, String graphId) {
        R result = null;
        ResultData<R> resultData = ResultData.getFail(MessageEnum.CLIENT_ERROR.getMes(), NodeResultStatus.ERROR);
        long startTime = SystemClock.now();
        long endTime = SystemClock.now();
        try {
            result = workFunction.apply(p);
            endTime = SystemClock.now();
            changeStatus(NodeResultStatus.EXECUTING, NodeResultStatus.EXECUTED);
            resultData = ResultData.build(result, NodeResultStatus.EXECUTED, "success", endTime - startTime);
        } catch (Exception e) {
            endTime = SystemClock.now();
            log.error(String.format("%s\t{}", MessageEnum.CLIENT_ERROR), uniqueId, e);
            changeStatus(NodeResultStatus.EXECUTING, NodeResultStatus.ERROR);
            resultData = ResultData.build(result, NodeResultStatus.ERROR, "fail", endTime - startTime);
            return Pair.of(false, resultData);
        } finally {
            log.info("{}\t执行耗时{}", uniqueId, endTime - startTime);
            NodeRunningInfo nodeRunningInfo = new NodeRunningInfo(getGraphTraceId(), getTraceId(),
                    graphId, uniqueId, resultData);
            getGraphRunningInfo().putNodeRunningInfo(uniqueId, nodeRunningInfo);
        }
        return Pair.of(true, resultData);
    }

}
