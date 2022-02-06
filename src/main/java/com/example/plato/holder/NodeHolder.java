package com.example.plato.holder;

import com.example.plato.loader.registry.YmlRegistry;
import com.example.plato.loader.config.GraphConfig;
import com.example.plato.loader.config.NodeConfig;
import com.example.plato.loader.factory.NodeYmlFactory;
import com.example.plato.loader.ymlNode.AbstractYmlNode;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.example.plato.element.NodeLoadByBean;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/23 10:59 上午
 * 存放node运行时结果
 */
@Slf4j
public class NodeHolder {

    /**
     * <graphId:<uniqueId,NodeLoadByBean>>
     */
    private static Map<String, Map<String, NodeLoadByBean>> nodeMap = new ConcurrentHashMap<>();

    private static final Map<String, AbstractYmlNode> START_NODE_MAP = new HashMap<>();

    private static final Map<String, Map<String, AbstractYmlNode>> NODE_YML_MAP = new HashMap<>();

    public static NodeLoadByBean getNode(String graphId, String uniqueId) {
        if (StringUtils.isAnyBlank(graphId, uniqueId) || !nodeMap.containsKey(graphId)) {
            return null;
        }
        return nodeMap.get(uniqueId).get(uniqueId);
    }

    public static NodeLoadByBean putNode(String graphId, String uniqueId, NodeLoadByBean nodeLoadByBean) {
        if (StringUtils.isAnyBlank(graphId, uniqueId) || !Optional.ofNullable(nodeLoadByBean).isPresent()) {
            return null;
        }
        if (nodeMap.containsKey(graphId)) {
            return nodeMap.get(graphId).put(uniqueId, nodeLoadByBean);
        }
        ConcurrentHashMap<String, NodeLoadByBean> concurrentHashMap = new ConcurrentHashMap<>();
        concurrentHashMap.put(uniqueId, nodeLoadByBean);
        nodeMap.put(uniqueId, concurrentHashMap);
        return nodeLoadByBean;
    }

    public static Map<String, AbstractYmlNode> getStartNodeMap() {
        return START_NODE_MAP;
    }

    public static AbstractYmlNode getAbstractYmlNode(String graphIdTemp, String uniqueId) {
        if (StringUtils.isAnyBlank(graphIdTemp, uniqueId) || !NODE_YML_MAP.containsKey(graphIdTemp)) {
            return null;
        }
        return NODE_YML_MAP.get(graphIdTemp).get(uniqueId);
    }

    public static void getYmlNodeMap() {
        Map<String, GraphConfig> registryMap = new YmlRegistry().registry();
        for (String graphIdTemp : registryMap.keySet()) {
            synchronized (NodeHolder.class) {
                GraphConfig graphConfig = registryMap.get(graphIdTemp);
                final Map<String, AbstractYmlNode> abstractYmlNodeMap = NODE_YML_MAP.containsKey(graphIdTemp)
                        ? NODE_YML_MAP.get(graphIdTemp)
                        : new HashMap<>();
                List<NodeConfig> nodeConfigs = graphConfig.getNodes();
                for (NodeConfig nodeConfig : nodeConfigs) {
                    AbstractYmlNode abstractYmlNode = NodeYmlFactory.getIYmlNode(nodeConfig, graphConfig.getScanPackage());
                    abstractYmlNodeMap.put(nodeConfig.getUniqueId(), abstractYmlNode);
                    if (graphConfig.getStartNode().equals(nodeConfig.getUniqueId())) {
                        START_NODE_MAP.putIfAbsent(graphIdTemp, abstractYmlNode);
                    }
                }
            }
        }
    }
}
