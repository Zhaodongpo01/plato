package com.example.plato.element;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.plato.element.Graph.Entry;
import com.example.plato.handler.AfterHandler;
import com.example.plato.handler.PreHandler;
import com.example.plato.platoEnum.RelationEnum;
import com.example.plato.util.PlatoAssert;

/**
 * @author zhaodongpo
 * create  2022/5/16 11:44 上午
 * @version 1.0
 */
public class NodeProxyBuilder<P, V> extends AbstractNodeProxy<P, V> {

    private Graph<AbstractNodeProxy<?, ?>, RelationEnum> graph;

    private volatile Set<AbstractNodeProxy<?, ?>> nextNodeProxies;

    private volatile Set<AbstractNodeProxy<?, ?>> perNodeProxies;

    public NodeProxyBuilder(String uniqueId, INodeWork<P, V> iNodeWork, String graphId, long timeLimit,
            PreHandler<P> preHandler, AfterHandler afterHandler) {
        super(iNodeWork, uniqueId, graphId, preHandler, afterHandler, timeLimit);
        super.traceId = UUID.randomUUID().toString();
        State.setState(state, State.BUILDING, State.INIT);
    }

    void setParam(P param) {
        super.param = param;
    }

    public void setGraph(Graph<AbstractNodeProxy<?, ?>, RelationEnum> graph) {
        this.graph = graph;
    }

    @Override
    public Set<AbstractNodeProxy<?, ?>> getNextNodeProxies() {
        PlatoAssert.nullException(() -> "graph is null error", graph);
        if (nextNodeProxies == null) {
            synchronized (this) {
                if (nextNodeProxies == null) {
                    Set<? extends Entry<AbstractNodeProxy<?, ?>, RelationEnum>> relationFrom =
                            graph.getRelationFrom(this);
                    if (relationFrom == null) {
                        return null;
                    }
                    nextNodeProxies = relationFrom.stream().map(Graph.Entry::getTo).collect(Collectors.toSet());
                }
            }
        }
        return nextNodeProxies;
    }

    @Override
    public Set<AbstractNodeProxy<?, ?>> getPreNodeProxies() {
        PlatoAssert.nullException(() -> "graph is null error", graph);
        if (perNodeProxies == null) {
            synchronized (this) {
                if (perNodeProxies == null) {
                    Set<? extends Entry<AbstractNodeProxy<?, ?>, RelationEnum>> relationTo = graph.getRelationTo(this);
                    if (relationTo == null) {
                        return null;
                    }
                    perNodeProxies = relationTo.stream().map(Graph.Entry::getFrom).collect(Collectors.toSet());
                }
            }
        }
        return perNodeProxies;
    }
}
