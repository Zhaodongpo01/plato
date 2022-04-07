package com.example.plato.loader.holder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.plato.loader.loaderConfig.NodeConfig;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/4/6 9:28 下午
 */
public class NodeHolder {

    final static private Map<String, Map<String, NodeConfig>> NODE_MAP = new ConcurrentHashMap<>();

    public static Map<String, NodeConfig> getNodeMap(String graphId) {
        return NODE_MAP.get(graphId);
    }

    public static NodeConfig getNodeConfig(String graphId, String uniqueId) {
        return NODE_MAP.containsKey(graphId) ? NODE_MAP.get(graphId).get(uniqueId) : null;
    }

    public static NodeConfig putNodeConfig(NodeConfig nodeConfig) {
        if (NODE_MAP.containsKey(nodeConfig.getGraphId())) {
            return NODE_MAP.get(nodeConfig.getGraphId()).put(nodeConfig.getUniqueId(), nodeConfig);
        } else {
            Map<String, NodeConfig> nodeConfigMap = new ConcurrentHashMap<>();
            NODE_MAP.put(nodeConfig.getGraphId(), nodeConfigMap);
            return nodeConfigMap.put(nodeConfig.getUniqueId(), nodeConfig);
        }
    }

}
