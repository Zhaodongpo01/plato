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
    //当并发时，后面的节点不强依赖此节点，是否要求此节点在运行之前进行校验，后面的节点有结果了就不执行
    private boolean checkNextHasResult = false;
    private final List<NodeLoadByBean<?, ?>> nextNodes = new ArrayList<>();
    private final List<String> preNodes = new ArrayList<>();

    @Data
    public static class NodeBeanBuilder<P, R> extends NodeLoadByBean {
        private List<NodeBeanBuilder<?, ?>> nextBuilderNodes = new ArrayList<>();

        NodeLoadByBean<P, R> build() {
            if (NodeHolder.getNodeLoadByBean(this.getGraphId(), this.getUniqueId()) == null) {
                synchronized (NodeLoadByBean.class) {
                    if (NodeHolder.getNodeLoadByBean(this.getGraphId(), this.getUniqueId()) == null) {
                        NodeLoadByBean<P, R> nodeLoadByBean = (NodeLoadByBean<P, R>) check();
                        if (CollectionUtils.isNotEmpty(this.getNextBuilderNodes())) {
                            nodeLoadByBean.getNextNodes()
                                    .addAll(convertBuild2Bean(this.getNextBuilderNodes(), this.getGraphId()));
                        }
                        return NodeHolder.putNodeLoadByBean(this.getGraphId(), this.getUniqueId(), nodeLoadByBean);
                    }
                }
            }
            return NodeHolder.getNodeLoadByBean(this.getGraphId(), this.getUniqueId());
        }

        private NodeBeanBuilder<P, R> check() {
            if (ObjectUtils.anyNull(this.getINodeWork(), this.getUniqueId(), this.getGraphId())) {
                throw new PlatoException("NodeBeanBuilder#check error");
            }
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
            if (!StringUtils.isAnyBlank(graphId, uniqueId) && Optional.ofNullable(iNodeWork).isPresent()) {
                this.setGraphId(graphId).setUniqueId(uniqueId).setINodeWork(iNodeWork).setParam(p);
            }
            return this;
        }

        public NodeBeanBuilder<P, R> setNodeBuilder(String uniqueId, INodeWork<P, R> iNodeWork) {
            if (StringUtils.isNotBlank(uniqueId) && Optional.ofNullable(iNodeWork).isPresent()) {
                this.setUniqueId(uniqueId).setINodeWork(iNodeWork);
            }
            return this;
        }

        public NodeBeanBuilder<P, R> setGraphId(String graphId) {
            if (StringUtils.isNotBlank(graphId)) {
                super.graphId = graphId;
                Graph.getGraphInstance(graphId);
            }
            return this;
        }

        public NodeBeanBuilder<P, R> setUniqueId(String uniqueId) {
            if (StringUtils.isNotBlank(uniqueId)) {
                super.uniqueId = uniqueId;
            }
            return this;
        }

        public NodeBeanBuilder<P, R> setParam(P p) {
            if (Optional.ofNullable(p).isPresent()) {
                super.p = p;
            }
            return this;
        }

        public NodeBeanBuilder<P, R> setName(String name) {
            if (StringUtils.isNotBlank(name)) {
                super.name = name;
            }
            return this;
        }

        public NodeBeanBuilder<P, R> setPreHandler(PreHandler preHandler) {
            if (Optional.ofNullable(preHandler).isPresent()) {
                super.preHandler = preHandler;
            }
            return this;
        }

        public NodeBeanBuilder<P, R> setAfterHandler(AfterHandler afterHandler) {
            if (Optional.ofNullable(afterHandler).isPresent()) {
                super.afterHandler = afterHandler;
            }
            return this;
        }

        public NodeBeanBuilder<P, R> setINodeWork(INodeWork<P, R> iNodeWork) {
            if (Optional.ofNullable(iNodeWork).isPresent()) {
                super.iNodeWork = iNodeWork;
            }
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
