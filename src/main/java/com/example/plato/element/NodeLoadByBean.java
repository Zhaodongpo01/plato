package com.example.plato.element;

import com.example.plato.handler.AfterHandler;
import com.example.plato.handler.INodeWork;
import com.example.plato.handler.PreHandler;
import com.example.plato.holder.NodeHolder;
import com.example.plato.util.PlatoAssert;

import lombok.Data;
import lombok.Getter;

import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/30 14:56
 */
@Getter
public class NodeLoadByBean<P, R> {

    private NodeLoadByBean() {
    }

    private P p;
    private String uniqueId;
    private String graphId;
    private String name;
    private PreHandler<P> preHandler;
    private AfterHandler afterHandler;
    private INodeWork<P, R> iNodeWork;
    private boolean checkNextHasResult = false;
    private final List<NodeLoadByBean<?, ?>> nextNodes = new ArrayList<>();
    private final List<String> preNodes = new ArrayList<>();

    @Data
    public static class NodeBeanBuilder<P, R> extends NodeLoadByBean {
        private List<NodeBeanBuilder<?, ?>> nextBuilderNodes = new ArrayList<>();

        NodeLoadByBean<P, R> build() {
            if (NodeHolder.getNode(this.getGraphId(), this.getUniqueId()) == null) {
                synchronized (NodeLoadByBean.class) {
                    if (NodeHolder.getNode(this.getGraphId(), this.getUniqueId()) == null) {
                        NodeLoadByBean<P, R> nodeLoadByBean = (NodeLoadByBean<P, R>) check();
                        if (CollectionUtils.isNotEmpty(this.getNextBuilderNodes())) {
                            nodeLoadByBean.getNextNodes()
                                    .addAll(convertBuild2Bean(this.getNextBuilderNodes(), this.getGraphId()));
                        }
                        NodeHolder.putNode(this.getGraphId(), this.getUniqueId(), nodeLoadByBean);
                        return this;
                    }
                }
            }
            return NodeHolder.getNode(this.getGraphId(), this.getUniqueId());
        }

        private NodeBeanBuilder<P, R> check() {
            PlatoAssert.nullException(() -> "NodeBeanBuilder check INodeWork error", this.getINodeWork());
            PlatoAssert.emptyException(() -> "NodeBeanBuilder#check error", this.getUniqueId(), this.getGraphId());
            return this;
        }

        private Collection<? extends NodeLoadByBean<?, ?>> convertBuild2Bean(
                List<NodeBeanBuilder<?, ?>> nextBuilderNodes, final String graphId) {
            return nextBuilderNodes.stream().map(temp -> temp.setGraphId(graphId).build()).collect(Collectors.toList());
        }

        public static NodeBeanBuilder get() {
            return new NodeBeanBuilder();
        }

        public NodeBeanBuilder<P, R> firstSetNodeBuilder(String graphId, String uniqueId, P p,
                INodeWork<P, R> iNodeWork) {
            PlatoAssert.nullException(() -> "firstSetNodeBuilder iNodeWork is null", iNodeWork);
            PlatoAssert.emptyException(() -> "firstSetNodeBuilder param error ", graphId, uniqueId);
            this.setGraphId(graphId).setUniqueId(uniqueId).setINodeWork(iNodeWork).setParam(p);
            return this;
        }

        public NodeBeanBuilder<P, R> setNodeBuilder(String uniqueId, INodeWork<P, R> iNodeWork) {
            PlatoAssert.nullException(() -> "setNodeBuilder iNodeWork is null", iNodeWork);
            PlatoAssert.emptyException(() -> "setNodeBuilder uniqueId null ", uniqueId);
            this.setUniqueId(uniqueId).setINodeWork(iNodeWork);
            return this;
        }

        public NodeBeanBuilder<P, R> setGraphId(String graphId) {
            PlatoAssert.emptyException(() -> "setGraphId graphId empty ", graphId);
            super.graphId = graphId;
            return this;
        }

        public NodeBeanBuilder<P, R> setUniqueId(String uniqueId) {
            PlatoAssert.emptyException(() -> "setUniqueId uniqueId empty ", uniqueId);
            super.uniqueId = uniqueId;
            return this;
        }

        public NodeBeanBuilder<P, R> setParam(P p) {
            PlatoAssert.nullException(() -> "setParam p is null", p);
            super.p = p;
            return this;
        }

        public NodeBeanBuilder<P, R> setName(String name) {
            PlatoAssert.emptyException(() -> "setName name empty ", name);
            super.name = name;
            return this;
        }

        public NodeBeanBuilder<P, R> setPreHandler(PreHandler preHandler) {
            PlatoAssert.nullException(() -> "setPreHandler preHandler is null", preHandler);
            super.preHandler = preHandler;
            return this;
        }

        public NodeBeanBuilder<P, R> setAfterHandler(AfterHandler afterHandler) {
            PlatoAssert.nullException(() -> "setAfterHandler afterHandler is null", afterHandler);
            super.afterHandler = afterHandler;
            return this;
        }

        public NodeBeanBuilder<P, R> setINodeWork(INodeWork<P, R> iNodeWork) {
            PlatoAssert.nullException(() -> "setINodeWork iNodeWork is null", iNodeWork);
            super.iNodeWork = iNodeWork;
            return this;
        }

        public NodeBeanBuilder<P, R> setCheckNextHasResult(boolean checkNextHasResult) {
            super.checkNextHasResult = checkNextHasResult;
            return this;
        }

        void addNextBuilderNodes(NodeBeanBuilder<?, ?>... nextBuilderNodes) {
            List<NodeBeanBuilder<?, ?>> nodeBeanBuilderList =
                    Arrays.stream(nextBuilderNodes).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(nodeBeanBuilderList)) {
                this.getNextBuilderNodes().addAll(nodeBeanBuilderList);
            }
        }

        void addPreBuilderNodes(String... preNodes) {
            List<String> preNodeList = Arrays.stream(preNodes).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(preNodeList)) {
                super.getPreNodes().addAll(preNodeList);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            NodeBeanBuilder<?, ?> that = (NodeBeanBuilder<?, ?>) o;
            return Objects.equals(this.getP(), that.getP())
                    && Objects.equals(this.getGraphId(), that.getGraphId())
                    && Objects.equals(this.getUniqueId(), that.getUniqueId())
                    && Objects.equals(this.getNextNodes(), that.getNextNodes())
                    && Objects.equals(this.getPreNodes(), that.getPreNodes())
                    && Objects.equals(this.getINodeWork(), that.getINodeWork())
                    && Objects.equals(this.getName(), that.getName())
                    && Objects.equals(this.getAfterHandler(), that.getAfterHandler())
                    && Objects.equals(this.getPreHandler(), that.getPreHandler())
                    ;
        }

    }
}
