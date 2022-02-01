package com.example.plato.element;

import com.example.plato.exception.PlatoException;
import com.example.plato.handler.AfterHandler;
import com.example.plato.handler.PreHandler;
import com.example.plato.holder.NodeHolder;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

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
    private List<NodeLoadByBean<?, ?>> nextNodes = new ArrayList<>();
    private List<String> preNodes = new ArrayList<>();
    //private boolean checkNextDone = false;
    //private SubNodes subNodes;

    /*
    * TODO bean的方式暂时不支持 subFlow的结构
    @Data
    static class SubNodes {
        Node subStartNode;
        Node subEndNode;
        boolean mustNext = false;
        String graphId;

        SubNodes check() {
            if (ObjectUtils.anyNull(this.subStartNode, this.subEndNode)
                    || StringUtils.isBlank(this.graphId)) throw new PlatoException("SubNodes error");
            return this;
        }
    }*/

    @Data
    public static class NodeBeanBuilder<D, M> extends NodeLoadByBean {

        private List<NodeBeanBuilder<?, ?>> nextBuilderNodes = new ArrayList<>();

        NodeLoadByBean<D, M> build() {
            if (NodeHolder.getNodeLoadByBean(this.getGraphId(), this.getUniqueId()) == null) {
                synchronized (NodeLoadByBean.class) {
                    if (NodeHolder.getNodeLoadByBean(this.getGraphId(), this.getUniqueId()) == null) {
                        NodeLoadByBean<D, M> nodeLoadByBean = (NodeLoadByBean<D, M>) check();
                        if (CollectionUtils.isNotEmpty(this.getNextBuilderNodes())) {
                            nodeLoadByBean.getNextNodes().addAll(convertBuild2Bean(this.getNextBuilderNodes(), this.getGraphId()));
                        }
                        return NodeHolder.putNodeLoadByBean(this.getGraphId(), this.getUniqueId(), nodeLoadByBean);
                    }
                }
            }
            return NodeHolder.getNodeLoadByBean(this.getGraphId(), this.getUniqueId());
        }

        private NodeBeanBuilder<D, M> check() {
            if (ObjectUtils.anyNull(this.getINodeWork(), this.getUniqueId(), this.getGraphId())) {
                throw new PlatoException("NodeBeanBuilder#check error");
            }
            return this;
        }

        private Collection<? extends NodeLoadByBean<?, ?>> convertBuild2Bean(List<NodeBeanBuilder<?, ?>> nextBuilderNodes, String graphId) {
            return nextBuilderNodes.stream().map(temp -> {
                temp.setGraphId(graphId);
                return temp.build();
            }).collect(Collectors.toList());
        }

        public static NodeBeanBuilder get() {
            return new NodeBeanBuilder();
        }

        public NodeBeanBuilder<D, M> firstSetNodeBuilder(String graphId, String uniqueId, D d, INodeWork<D, M> iNodeWork) {
            if (!StringUtils.isAnyBlank(graphId, uniqueId) && Optional.ofNullable(iNodeWork).isPresent()) {
                this.setGraphId(graphId).setUniqueId(uniqueId).setINodeWork(iNodeWork).setParam(d);
            }
            return this;
        }

        public NodeBeanBuilder<D, M> setNodeBuilder(String uniqueId, INodeWork<D, M> iNodeWork) {
            if (StringUtils.isNotBlank(uniqueId) && Optional.ofNullable(iNodeWork).isPresent()) {
                this.setUniqueId(uniqueId).setINodeWork(iNodeWork);
            }
            return this;
        }

        public NodeBeanBuilder<D, M> setGraphId(String graphId) {
            if (StringUtils.isNotBlank(graphId)) {
                super.graphId = graphId;
                Graph.getGraphInstance(graphId);
            }
            return this;
        }

        public NodeBeanBuilder<D, M> setUniqueId(String uniqueId) {
            if (StringUtils.isNotBlank(uniqueId)) {
                super.uniqueId = uniqueId;
            }
            return this;
        }

        public NodeBeanBuilder<D, M> setParam(D d) {
            if (Optional.ofNullable(d).isPresent()) {
                super.p = d;
            }
            return this;
        }

        public NodeBeanBuilder<D, M> setName(String name) {
            if (StringUtils.isNotBlank(name)) {
                super.name = name;
            }
            return this;
        }

        public NodeBeanBuilder<D, M> setPreHandler(PreHandler preHandler) {
            if (Optional.ofNullable(preHandler).isPresent()) {
                super.preHandler = preHandler;
            }
            return this;
        }

        public NodeBeanBuilder<D, M> setAfterHandler(AfterHandler afterHandler) {
            if (Optional.ofNullable(afterHandler).isPresent()) {
                super.afterHandler = afterHandler;
            }
            return this;
        }

        public NodeBeanBuilder<D, M> setINodeWork(INodeWork<D, M> iNodeWork) {
            if (Optional.ofNullable(iNodeWork).isPresent()) {
                super.iNodeWork = iNodeWork;
            }
            return this;
        }

        /*public NodeBeanBuilder<D, M> setSubNodes(SubNodes subNodes) {
            if (Optional.ofNullable(subNodes).isPresent()) {
                super.subNodes = subNodes;
            }
            return this;
        }*/

        /*public NodeBeanBuilder<D, M> setCheckNextDone(boolean checkNextDone) {
            super.checkNextDone = checkNextDone;
            return this;
        }*/

        NodeBeanBuilder<D, M> addNextBuilderNodes(NodeBeanBuilder<?, ?>... nextBuilderNodes) {
            List<NodeBeanBuilder<?, ?>> nodeBeanBuilderList = Arrays.stream(nextBuilderNodes).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(nodeBeanBuilderList)) {
                this.getNextBuilderNodes().addAll(nodeBeanBuilderList);
            }
            return this;
        }

        NodeBeanBuilder<D, M> addPreBuilderNodes(String... preNodes) {
            List<String> preNodeList = Arrays.stream(preNodes).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(preNodeList)) {
                super.getPreNodes().addAll(preNodeList);
            }
            return this;
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
                    //&& Objects.equals(this.getSubNodes(), that.getSubNodes())
                    ;
        }

    }
}
