package com.example.plato.element;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.example.plato.handler.INodeWork;
import com.example.plato.platoEnum.CurrentState;
import com.example.plato.platoEnum.NodeResultStatus;
import com.example.plato.runningData.NodeRunningInfo;
import com.example.plato.runningData.ResultData;
import com.example.plato.util.PlatoAssert;

import lombok.Data;

/**
 * @author zhaodongpo
 * @version 1.0
 * 2022/4/4 3:19 下午
 */
@Data
public class PlatoNodeProxy<P, R> {

    private P p;
    private String graphId;
    private String uniqueNodeId;
    private List<PlatoNodeProxy<?, ?>> nextPlatoNodeProxyList = new ArrayList<>();
    private List<PrePlatoNodeProxy> prePlatoNodeProxyList = new ArrayList<>();
    private final Map<String, ResultData<R>> resultDataMap = new ConcurrentHashMap<>();
    private final AtomicReference<CurrentState> CUR_STATUS = new AtomicReference<>(CurrentState.INIT);
    private final ResultData<R> resultData = ResultData.getFail(StringUtils.EMPTY);

    private boolean compareAndSetState(CurrentState expect, CurrentState update) {
        return this.CUR_STATUS.compareAndSet(expect, update);
    }

    ResultData<R> getResultData() {
        return resultData;
    }

    void run(P p, PlatoNodeProxy<?, ?> comingNode,
            ExecutorService threadPoolExecutor, Map<String, NodeRunningInfo> nodeRunningInfoMap) {
        PlatoNode<P, R> platoNode = PlatoNodeHolder.getPlato(graphId, uniqueNodeId);
        PlatoAssert.nullException(() -> "run platoNode must not null", platoNode);
        if (CurrentState.EXECUTED.equals(CUR_STATUS.get()) || CurrentState.ERROR.equals(CUR_STATUS.get())) {
            return;
        }
        if (MapUtils.isEmpty(platoNode.getPrePlatoNodeMap())) {
            this.p = p;
            executor(platoNode, nodeRunningInfoMap);
            if (resultData.isSuccess() && MapUtils.isNotEmpty(platoNode.getNextPlatoNodeMap())) {
                runNext(threadPoolExecutor, nodeRunningInfoMap);
            }
            return;
        }
        prePlatoNodesRun(comingNode, threadPoolExecutor, nodeRunningInfoMap);
    }

    private synchronized void prePlatoNodesRun(PlatoNodeProxy<?, ?> comingNode, ExecutorService threadPoolExecutor,
            Map<String, NodeRunningInfo> nodeRunningInfoMap) {
        AtomicBoolean current = new AtomicBoolean(false);
        Set<PrePlatoNodeProxy> mustAppends = new HashSet<>();
        prePlatoNodeProxyList.forEach(prePlatoNodeProxy -> {
            if (prePlatoNodeProxy.isMust()) {
                mustAppends.add(prePlatoNodeProxy);
            }
            if (prePlatoNodeProxy.getPlatoNodeProxy().equals(comingNode)) {
                current.set(prePlatoNodeProxy.isMust());
            }
        });
        //如果全部是不必须的条件，那么只要到了这里，就执行自己。
        PlatoNode<P, R> platoNode = PlatoNodeHolder.getPlato(graphId, uniqueNodeId);
        if (CollectionUtils.isEmpty(mustAppends)) {
            executor(platoNode, nodeRunningInfoMap);
            runNext(threadPoolExecutor, nodeRunningInfoMap);
            return;
        }
        //如果存在需要必须完成的，且fromWrapper不是必须的，就什么也不干
        if (!current.get()) {
            return;
        }
        boolean existsNotFinish = false;
        boolean hasError = false;
        for (PrePlatoNodeProxy prePlatoNodeProxy : mustAppends) {
            PlatoNodeProxy<?, ?> platoNodeProxy = prePlatoNodeProxy.getPlatoNodeProxy();
            ResultData<?> resultData = platoNodeProxy.getResultData(); //依赖节点的执行结果
            if (CUR_STATUS.get() == CurrentState.INIT || CUR_STATUS.get() == CurrentState.EXECUTING) {
                existsNotFinish = true;
                break;
            }
            if (NodeResultStatus.ERROR.equals(resultData.getNodeResultStatus())) {
                hasError = true;
                break;
            }
        }
        if (!existsNotFinish) {
            executor(platoNode, nodeRunningInfoMap);
            runNext(threadPoolExecutor, nodeRunningInfoMap);
        }
    }

    private void runNext(ExecutorService executorService, Map<String, NodeRunningInfo> nodeRunningInfoMap) {
        if (nextPlatoNodeProxyList.size() == 1) {
            PlatoNodeProxy<?, ?> platoNodeProxy = nextPlatoNodeProxyList.get(0);
            platoNodeProxy.run(getHandlerParam(), this, executorService, nodeRunningInfoMap);
        } else {
            CompletableFuture[] futures = new CompletableFuture[nextPlatoNodeProxyList.size()];
            for (int i = 0; i < nextPlatoNodeProxyList.size(); i++) {
                int finalI = i;
                futures[i] = CompletableFuture.runAsync(() -> {
                    PlatoNodeProxy<?, ?> platoNodeProxy = nextPlatoNodeProxyList.get(finalI);
                    platoNodeProxy.run(getHandlerParam(), this, executorService, nodeRunningInfoMap);
                }, executorService);
            }
            try {
                CompletableFuture.allOf(futures).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private <T> T getHandlerParam() {
        return null;
    }

    private void executor(PlatoNode<P, R> platoNode, Map<String, NodeRunningInfo> nodeRunningInfoMap) {
        if (!checkIsNullResult()) {
            return;
        }
        try {
            if (!compareAndSetState(CurrentState.INIT, CurrentState.EXECUTING)) {
                return;
            }
            NodeRunningInfo<R> nodeRunningInfo = new NodeRunningInfo<>(graphId, uniqueNodeId);
            INodeWork<P, R> iNodeWork = platoNode.getINodeWork();
            R r = iNodeWork.work(p);
            if (!compareAndSetState(CurrentState.EXECUTING, CurrentState.EXECUTED)) {
                return;
            }
            resultData.setData(r);
            resultData.setSuccess(true);
            nodeRunningInfo.setResultData(resultData);
            nodeRunningInfoMap.put(uniqueNodeId, nodeRunningInfo);
            iNodeWork.hook(p, resultData);
        } catch (Exception e) {
            if (!checkIsNullResult()) {
                return;
            }
            fastFail(CurrentState.EXECUTING, e);
        }
    }

    private boolean fastFail(CurrentState expect, Exception e) {
        if (!compareAndSetState(expect, CurrentState.ERROR)) {
            return false;
        }
        if (checkIsNullResult()) {
            resultData.setSuccess(false);
            resultData.setMes(e == null ? "执行失败" : e.getMessage());
        }
        PlatoNode<P, R> platoNode = PlatoNodeHolder.getPlato(graphId, uniqueNodeId);
        PlatoAssert.nullException(() -> "fastFail platoNode must not null", platoNode);
        platoNode.getINodeWork().hook(p, resultData);
        return true;
    }

    private boolean checkIsNullResult() {
        return NodeResultStatus.INIT == resultData.getNodeResultStatus();
    }

}
