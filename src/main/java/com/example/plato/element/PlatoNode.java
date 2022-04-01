package com.example.plato.element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.collections4.CollectionUtils;

import com.example.plato.handler.AfterHandler;
import com.example.plato.handler.INodeWork;
import com.example.plato.handler.PreHandler;
import com.example.plato.platoEnum.CurrentState;
import com.example.plato.platoEnum.NodeResultStatus;
import com.example.plato.runningData.ResultData;
import com.example.plato.util.PlatoAssert;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/3/31 11:12 下午
 */
public class PlatoNode<P, R> {

    private P p;
    private String uniqueNodeId;
    private INodeWork<P, R> iNodeWork;
    private AfterHandler afterHandler;
    private PreHandler<P> preHandler = PreHandler.DEFAULT_PRE_HANDLER;
    private List<PlatoNode<?, ?>> nextPlatoNodes = new ArrayList<>();
    private List<PrePlatoNode> prePlatoNodes = new ArrayList<>();
    private final AtomicReference<CurrentState> CUR_STATUS = new AtomicReference<>(CurrentState.INIT);
    private volatile ResultData<R> resultData = ResultData.getFail("");

    private PlatoNode(String uniqueNodeId, INodeWork<P, R> iNodeWork) {
        this.iNodeWork = iNodeWork;
        this.uniqueNodeId = uniqueNodeId;
    }

    private boolean compareAndSetState(CurrentState expect, CurrentState update) {
        return this.CUR_STATUS.compareAndSet(expect, update);
    }

    public ResultData<R> getResultData() {
        return resultData;
    }

    private void addPreNode(PlatoNode<?, ?> platoNode, boolean must) {
        addPreNode(new PrePlatoNode(platoNode, must));
    }

    private void addPreNode(PrePlatoNode prePlatoNode) {
        //如果依赖的是重复的同一个，就不重复添加了
        for (PrePlatoNode prePlatoNodeTemp : prePlatoNodes) {
            if (prePlatoNodeTemp.equals(prePlatoNode)) {
                return;
            }
        }
        prePlatoNodes.add(prePlatoNode);
    }

    private void addNext(PlatoNode<?, ?> platoNode) {
        //避免添加重复
        for (PlatoNode platoNodeTemp : nextPlatoNodes) {
            if (platoNodeTemp.equals(platoNode)) {
                return;
            }
        }
        nextPlatoNodes.add(platoNode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PlatoNode<?, ?> that = (PlatoNode<?, ?>) o;
        return Objects.equals(p, that.p) &&
                Objects.equals(iNodeWork, that.iNodeWork) &&
                Objects.equals(nextPlatoNodes, that.nextPlatoNodes) &&
                Objects.equals(prePlatoNodes, that.prePlatoNodes) &&
                Objects.equals(CUR_STATUS, that.CUR_STATUS) &&
                Objects.equals(resultData, that.resultData);
    }

    public void run(P p, PlatoNode<?, ?> comingNode, ExecutorService threadPoolExecutor) {
        if (CurrentState.EXECUTED.equals(CUR_STATUS.get())
                || CurrentState.ERROR.equals(CUR_STATUS.get())) {
            return;
        }
        if (CollectionUtils.isEmpty(prePlatoNodes)) {
            this.p = p;
            executor();
            if (resultData.isSuccess() && CollectionUtils.isNotEmpty(nextPlatoNodes)) {
                runNext(threadPoolExecutor);
            }
            return;
        }
        prePlatoNodesRun(comingNode, threadPoolExecutor);
    }

    private synchronized void prePlatoNodesRun(PlatoNode<?, ?> comingNode, ExecutorService threadPoolExecutor) {
        AtomicBoolean current = new AtomicBoolean(false);
        Set<PrePlatoNode> mustAppends = new HashSet<>();
        prePlatoNodes.forEach(prePlatoNode -> {
            if (prePlatoNode.isMust()) {
                mustAppends.add(prePlatoNode);
            }
            if (prePlatoNode.getPlatoNode().equals(comingNode)) {
                current.set(prePlatoNode.isMust());
            }
        });
        //如果全部是不必须的条件，那么只要到了这里，就执行自己。
        if (CollectionUtils.isEmpty(mustAppends)) {
            executor();
            runNext(threadPoolExecutor);
            return;
        }
        //如果存在需要必须完成的，且fromWrapper不是必须的，就什么也不干
        if (!current.get()) {
            return;
        }
        boolean existsNotFinish = false;
        boolean hasError = false;
        for (PrePlatoNode prePlatoNode : mustAppends) {
            PlatoNode<?, ?> platoNode = prePlatoNode.getPlatoNode();
            ResultData<?> resultData = platoNode.getResultData(); //依赖节点的执行结果
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
            executor();
            runNext(threadPoolExecutor);
            return;
        }
    }

    private void runNext(ExecutorService executorService) {
        if (nextPlatoNodes.size() == 1) {
            PlatoNode<?, ?> platoNode = nextPlatoNodes.get(0);
            platoNode.run(getHandlerParam(), this, executorService);
        } else {
            CompletableFuture[] futures = new CompletableFuture[nextPlatoNodes.size()];
            for (int i = 0; i < nextPlatoNodes.size(); i++) {
                int finalI = i;
                futures[i] = CompletableFuture.runAsync(() -> {
                    PlatoNode<?, ?> platoNode = nextPlatoNodes.get(finalI);
                    platoNode.run(getHandlerParam(), this, executorService);
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
        PlatoAssert.nullException(() -> "getHandlerParam preHandler is null", preHandler);
        return null;
    }

    private void executor() {
        if (!checkIsNullResult()) {
            return;
        }
        try {
            if (!compareAndSetState(CurrentState.INIT, CurrentState.EXECUTING)) {
                return;
            }
            R r = iNodeWork.work(p);
            if (!compareAndSetState(CurrentState.EXECUTING, CurrentState.EXECUTED)) {
                return;
            }
            resultData.setData(r);
            resultData.setSuccess(true);
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
        iNodeWork.hook(p, resultData);
        return true;
    }

    private boolean checkIsNullResult() {
        return NodeResultStatus.INIT == resultData.getNodeResultStatus();
    }


    public class PlatoNodeBuilder<T, V> {

        private String uniqueNodeId;
        private INodeWork<T, V> iNodeWork;
        private final List<PlatoNode<?, ?>> nextPlatoNodes = new ArrayList<>();
        private List<PrePlatoNode> prePlatoNodes = new ArrayList<>();
        private Set<PlatoNode<?, ?>> selfIsMustSet;

        public PlatoNodeBuilder<T, V> setUniqueNodeId(String uniqueNodeId) {
            PlatoAssert.emptyException(() -> "setUniqueNodeId uniqueNodeId empty", uniqueNodeId);
            this.uniqueNodeId = uniqueNodeId;
            return this;
        }

        public PlatoNodeBuilder<T, V> setINodeWork(INodeWork<T, V> iNodeWork) {
            PlatoAssert.nullException(() -> "setINodeWork iNodeWork nul", iNodeWork);
            this.iNodeWork = iNodeWork;
            return this;
        }

        public PlatoNodeBuilder<T, V> addPreNodes(PlatoNode<?, ?>... platoNodes) {
            PlatoAssert.nullException(() -> "addPreNodes param null", platoNodes);
            Arrays.stream(platoNodes).forEach(this::addPreNode);
            return this;
        }

        public PlatoNodeBuilder<T, V> addPreNode(PlatoNode<?, ?> platoNode) {
            PlatoAssert.nullException(() -> "addPreNode param null", platoNode);
            return this;
        }

        public PlatoNodeBuilder<T, V> addPreNode(PlatoNode<?, ?> platoNode, boolean isMust) {
            PlatoAssert.nullException(() -> "addPreNode param null", platoNode);
            PrePlatoNode prePlatoNode = new PrePlatoNode(platoNode, isMust);
            this.prePlatoNodes.add(prePlatoNode);
            return this;
        }

        public PlatoNodeBuilder<T, V> addNext(PlatoNode<?, ?> platoNode) {
            return addNext(platoNode, true);
        }

        public PlatoNodeBuilder<T, V> next(PlatoNode<?, ?>... platoNode) {
            if (platoNode == null) {
                return this;
            }
            for (PlatoNode<?, ?> platoNodeTemp : platoNode) {
                next(platoNodeTemp);
            }
            return this;
        }

        private PlatoNodeBuilder<T, V> addNext(PlatoNode<?, ?> platoNode, boolean selfIsMust) {
            this.nextPlatoNodes.add(platoNode);
            if (selfIsMust) {
                if (selfIsMustSet == null) {
                    selfIsMustSet = new HashSet<>();
                }
                selfIsMustSet.add(platoNode);
            }
            return this;
        }

        public PlatoNode<T, V> build() {
            PlatoNode<T, V> platoNode = new PlatoNode<>(uniqueNodeId, iNodeWork);
            if (CollectionUtils.isNotEmpty(prePlatoNodes)) {
                prePlatoNodes.forEach(prePlatoNode -> {
                    prePlatoNode.getPlatoNode().addNext(platoNode);
                    platoNode.addPreNode(prePlatoNode);
                });
            }
            if (CollectionUtils.isNotEmpty(nextPlatoNodes)) {
                nextPlatoNodes.forEach(platoNodeTemp -> {
                    boolean must = false;
                    if (selfIsMustSet != null && selfIsMustSet.contains(platoNodeTemp)) {
                        must = true;
                    }
                    platoNodeTemp.addPreNode(platoNode, must);
                    platoNode.addNext(platoNodeTemp);
                });
            }
            return platoNode;
        }

    }
}
