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
public class NodeWorkBuilder<P, V> extends AbstractNodeWork<P, V> {

    private Graph<AbstractNodeWork<?, ?>, RelationEnum> graph;

    private volatile Set<AbstractNodeWork<?, ?>> nextNodeProxies;

    private volatile Set<AbstractNodeWork<?, ?>> perNodeProxies;

    private volatile Set<AbstractNodeWork<?, ?>> nextEntryNodeProxies;

    private volatile Set<AbstractNodeWork<?, ?>> perEntryNodeProxies;

    public NodeWorkBuilder(String uniqueId, INodeWork<P, V> iNodeWork, String graphId, long timeLimit,
            PreHandler<P> preHandler, AfterHandler afterHandler) {
        super(iNodeWork, uniqueId, graphId, timeLimit);
        super.traceId = UUID.randomUUID().toString();
        super.preHandler = preHandler;
        super.afterHandler = afterHandler;
        State.setState(state, State.BUILDING, State.INIT);
    }

    void setParam(P param) {
        super.param = param;
    }

    public void setGraph(Graph<AbstractNodeWork<?, ?>, RelationEnum> graph) {
        this.graph = graph;
    }

    @Override
    public Set<AbstractNodeWork<?, ?>> getNextEntryNodeProxies(RelationEnum relationEnum) {
        PlatoAssert.nullException(() -> "graph is null error", graph);
        if (nextEntryNodeProxies == null) {
            synchronized (this) {
                if (nextEntryNodeProxies == null) {
                    Set<? extends Entry<AbstractNodeWork<?, ?>, RelationEnum>> relationFrom =
                            graph.getRelationFrom(this);
                    if (relationFrom == null) {
                        nextEntryNodeProxies = null;
                    } else {
                        nextEntryNodeProxies =
                                relationFrom.stream().filter(temp -> relationEnum.equals(temp.getRelation()))
                                        .map(temp -> temp.getTo()).collect(Collectors.toSet());
                    }
                }
            }
        }
        return nextEntryNodeProxies;
    }

    @Override
    public Set<AbstractNodeWork<?, ?>> getNextNodeProxies() {
        PlatoAssert.nullException(() -> "graph is null error", graph);
        if (nextNodeProxies == null) {
            synchronized (this) {
                if (nextNodeProxies == null) {
                    Set<? extends Entry<AbstractNodeWork<?, ?>, RelationEnum>> relationFrom =
                            graph.getRelationFrom(this);
                    if (relationFrom == null) {
                        nextNodeProxies = null;
                    } else {
                        nextNodeProxies = relationFrom.stream().map(Graph.Entry::getTo).collect(Collectors.toSet());
                    }
                }
            }
        }
        return nextNodeProxies;
    }

    @Override
    public Set<AbstractNodeWork<?, ?>> getPreEntryNodeProxies(RelationEnum relationEnum) {
        PlatoAssert.nullException(() -> "graph is null error", graph);
        if (perEntryNodeProxies == null) {
            synchronized (this) {
                if (perEntryNodeProxies == null) {
                    Set<? extends Entry<AbstractNodeWork<?, ?>, RelationEnum>> relationTo = graph.getRelationTo(this);
                    if (relationTo == null) {
                        perEntryNodeProxies = null;
                    } else {
                        perEntryNodeProxies =
                                relationTo.stream().filter(temp -> relationEnum.equals(temp.getRelation()))
                                        .map(temp -> temp.getFrom()).collect(Collectors.toSet());
                    }
                }
            }
        }
        return perEntryNodeProxies;
    }

    @Override
    public Set<AbstractNodeWork<?, ?>> getPreNodeProxies() {
        PlatoAssert.nullException(() -> "graph is null error", graph);
        if (perNodeProxies == null) {
            synchronized (this) {
                if (perNodeProxies == null) {
                    Set<? extends Entry<AbstractNodeWork<?, ?>, RelationEnum>> relationTo = graph.getRelationTo(this);
                    if (relationTo == null) {
                        perNodeProxies = null;
                    } else {
                        perNodeProxies = relationTo.stream().map(Graph.Entry::getFrom).collect(Collectors.toSet());
                    }
                }
            }
        }
        return perNodeProxies;
    }
}
