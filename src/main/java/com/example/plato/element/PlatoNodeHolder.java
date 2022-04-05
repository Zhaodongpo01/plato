package com.example.plato.element;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.plato.util.PlatoAssert;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/4/4 3:23 下午
 */
public class PlatoNodeHolder {

    private static final Map<String, Map<String, PlatoNode>> PLATO_NODE_MAP = new ConcurrentHashMap<>();
    private static final Object METUX = new Object();

    static Map<String, PlatoNode> getPlatoNodeMap(String graphId) {
        return PLATO_NODE_MAP.get(graphId);
    }

    static <P, R> PlatoNode<P, R> getPlato(String graphId, String uniqueNodeId) {
        PlatoAssert.emptyException(() -> "getPlato param must not empty", graphId, uniqueNodeId);
        return PLATO_NODE_MAP.containsKey(graphId) ? PLATO_NODE_MAP.get(graphId).get(uniqueNodeId) : null;
    }

    static <P, R> PlatoNode<P, R> putPlato(String graphId, PlatoNode<P, R> platoNode) {
        PlatoAssert.emptyException(() -> "putPlato graphId must not empty", graphId);
        PlatoAssert.nullException(() -> "putPlato platoNode must not null", platoNode);
        synchronized (METUX) {
            ConcurrentHashMap<String, PlatoNode> platoNodeConcurrentHashMap = new ConcurrentHashMap<>();
            if (!PLATO_NODE_MAP.containsKey(graphId)) {
                PLATO_NODE_MAP.put(graphId, platoNodeConcurrentHashMap);
            } else {
                platoNodeConcurrentHashMap = (ConcurrentHashMap<String, PlatoNode>) PLATO_NODE_MAP.get(graphId);
            }
            platoNodeConcurrentHashMap.put(platoNode.getUniqueNodeId(), platoNode);
        }
        return platoNode;
    }

}
