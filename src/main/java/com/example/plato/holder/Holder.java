package com.example.plato.holder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.plato.element.PlatoNode;
import com.example.plato.util.PlatoAssert;

/**
 * @author zhaodongpo
 * create  2022/5/16 10:26 上午
 * @version 1.0
 */
public class Holder {

    private static final Map<String, Map<String, PlatoNode>> PLATO_NODE_MAP = new ConcurrentHashMap<>();

    public static PlatoNode getPlatoNode(String graphId, String uniqueId) {
        PlatoAssert.emptyException(() -> "getPlatoNode param error", graphId, uniqueId);
        return PLATO_NODE_MAP.containsKey(graphId) ? PLATO_NODE_MAP.get(graphId).get(uniqueId) : null;
    }

    public static void putPlatoNode(PlatoNode platoNode) {
        String graphId = platoNode.getGraphId();
        String uniqueId = platoNode.getUniqueId();
        if (PLATO_NODE_MAP.containsKey(graphId)) {
            PLATO_NODE_MAP.get(graphId).put(uniqueId, platoNode);
        } else {
            ConcurrentHashMap<String, PlatoNode> concurrentHashMap = new ConcurrentHashMap<>();
            concurrentHashMap.put(uniqueId, platoNode);
            PLATO_NODE_MAP.put(platoNode.getGraphId(), concurrentHashMap);
        }
    }
}
