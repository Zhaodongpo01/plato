package com.example.plato.element;

import java.util.Set;

import com.example.plato.holder.Holder;

/**
 * @author zhaodongpo
 * create  2022/5/16 11:39 上午
 * @version 1.0
 */
public class PlatoNode<P, V> {

    private String uniqueId;

    private String graphId;

    private INodeWork<P, V> iNodeWork;

    private Set<String> perPlatoNodes;

    private Set<String> nextPlatoNodes;

    private long timeLimit;

    public String getUniqueId() {
        return uniqueId;
    }

    public String getGraphId() {
        return graphId;
    }

    public INodeWork<P, V> getiNodeWork() {
        return iNodeWork;
    }

    private static volatile PlatoNode platoNode = null;

    private PlatoNode(String uniqueId, String graphId, INodeWork<P, V> iNodeWork, long timeLimit) {
        this.uniqueId = uniqueId;
        this.graphId = graphId;
        this.iNodeWork = iNodeWork;
        this.timeLimit = timeLimit;
    }

    public static <P, V> PlatoNode<P, V> getInstance(String uniqueId, String graphId, INodeWork<P, V> iNodeWork,
            long timeLimit) {
        if (Holder.getPlatoNode(graphId, uniqueId) == null) {
            synchronized (PlatoNode.class) {
                if (Holder.getPlatoNode(graphId, uniqueId) == null) {
                    platoNode = new PlatoNode(uniqueId, graphId, iNodeWork, timeLimit);
                    Holder.putPlatoNode(platoNode);
                }
            }
        }
        return Holder.getPlatoNode(graphId, uniqueId);
    }

}
