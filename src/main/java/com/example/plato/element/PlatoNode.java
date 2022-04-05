package com.example.plato.element;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.example.plato.handler.AfterHandler;
import com.example.plato.handler.INodeWork;
import com.example.plato.handler.PreHandler;
import com.example.plato.util.PlatoAssert;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * 2022/3/31 11:12 下午
 */
@Getter
@Slf4j
public class PlatoNode<P, R> {

    private String graphId;
    private String uniqueNodeId;
    private INodeWork<P, R> iNodeWork;
    private AfterHandler afterHandler = AfterHandler.DEFAULT_AFTER_HANDLER;
    private PreHandler<P> preHandler = PreHandler.DEFAULT_PRE_HANDLER;
    private final Map<PlatoNode<?, ?>, Boolean> nextPlatoNodeMap = new ConcurrentHashMap<>();   //后面这个节点强依赖自己
    private final Map<PlatoNode<?, ?>, Boolean> prePlatoNodeMap = new ConcurrentHashMap<>();    //自己强依赖于前面的节点

    private PlatoNode() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PlatoNodeBuilder<?, ?> that = (PlatoNodeBuilder<?, ?>) o;
        return Objects.equals(this.getGraphId(), that.getGraphId())
                && Objects.equals(this.getUniqueNodeId(), that.getUniqueNodeId())
                && Objects.equals(this.getNextPlatoNodeMap(), that.getNextPlatoNodeMap())
                && Objects.equals(this.getPrePlatoNodeMap(), that.getPrePlatoNodeMap())
                && Objects.equals(this.getINodeWork(), that.getINodeWork())
                && Objects.equals(this.getAfterHandler(), that.getAfterHandler())
                && Objects.equals(this.getPreHandler(), that.getPreHandler())
                ;
    }

    public static class PlatoNodeBuilder<P, R> extends PlatoNode<P, R> {

        volatile private Object mutexLock;

        public PlatoNodeBuilder<P, R> setNodeBuilder(String uniqueId, String graphId, INodeWork<P, R> iNodeWork) {
            PlatoAssert.nullException(() -> "setNodeBuilder iNodeWork is null", iNodeWork);
            PlatoAssert.emptyException(() -> "setNodeBuilder uniqueId null ", uniqueId);
            this.setGraphId(graphId).setUniqueId(uniqueId).setINodeWork(iNodeWork);
            return this;
        }

        public PlatoNodeBuilder<P, R> setGraphId(String graphId) {
            PlatoAssert.emptyException(() -> "setGraphId graphId empty ", graphId);
            super.graphId = graphId;
            return this;
        }

        public PlatoNodeBuilder<P, R> setUniqueId(String uniqueId) {
            PlatoAssert.emptyException(() -> "setUniqueId uniqueId empty ", uniqueId);
            super.uniqueNodeId = uniqueId;
            return this;
        }

        public PlatoNodeBuilder<P, R> setPreHandler(PreHandler<P> preHandler) {
            PlatoAssert.nullException(() -> "setPreHandler preHandler is null", preHandler);
            super.preHandler = preHandler;
            return this;
        }

        public PlatoNodeBuilder<P, R> setAfterHandler(AfterHandler afterHandler) {
            PlatoAssert.nullException(() -> "setAfterHandler afterHandler is null", afterHandler);
            super.afterHandler = afterHandler;
            return this;
        }

        public PlatoNodeBuilder<P, R> setINodeWork(INodeWork<P, R> iNodeWork) {
            PlatoAssert.nullException(() -> "setINodeWork iNodeWork is null", iNodeWork);
            super.iNodeWork = iNodeWork;
            return this;
        }

        public PlatoNodeBuilder<P, R> addPreNode(PlatoNode<P, R>... platoNode) {
            this.addPreNode(true, platoNode);
            return this;
        }

        public PlatoNodeBuilder<P, R> addPreNode(boolean appendMust, PlatoNode<P, R>... prePlatoNodes) {
            //如果重复了就按照最后一个加入的状态保存
            PlatoAssert.nullException(() -> "prePlatoNode must not null", (Object) prePlatoNodes);
            Arrays.stream(prePlatoNodes).collect(Collectors.toList())
                    .forEach(prePlatoNodeBuilder -> super.getPrePlatoNodeMap().put(prePlatoNodeBuilder, appendMust));
            return this;
        }

        public PlatoNodeBuilder<P, R> addNextNode(PlatoNode<P, R>... nextPlatoNodes) {
            this.addNextNode(true, nextPlatoNodes);
            return this;
        }

        public PlatoNodeBuilder<P, R> addNextNode(boolean selfIsMust, PlatoNode<P, R>... nextPlatoNodes) {
            //如果重复了就按照最后一个加入的状态保存
            PlatoAssert.nullException(() -> "prePlatoNode must not null", (Object) nextPlatoNodes);
            Arrays.stream(nextPlatoNodes).collect(Collectors.toList())
                    .forEach(nextPlatoNodeBuilder -> super.getNextPlatoNodeMap().put(nextPlatoNodeBuilder, selfIsMust));
            return this;
        }

        PlatoNode<P, R> build() {
            if (PlatoNodeHolder.getPlato(getGraphId(), getUniqueNodeId()) == null) {
                synchronized (mutex()) {
                    if (PlatoNodeHolder.getPlato(getGraphId(), getUniqueNodeId()) == null) {
                        PlatoNodeHolder.putPlato(getGraphId(), this);
                        return this;
                    }
                    return PlatoNodeHolder.getPlato(getGraphId(), getUniqueNodeId());
                }
            }
            return this;
        }

        private Object mutex() {
            Object mutex = mutexLock;
            if (mutex == null) {
                synchronized (this) {
                    mutex = mutexLock;
                    if (mutex == null) {
                        mutexLock = mutex = new Object();
                    }
                }
            }
            return mutex;
        }
    }
}
