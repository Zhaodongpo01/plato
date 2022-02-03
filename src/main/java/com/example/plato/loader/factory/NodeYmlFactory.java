package com.example.plato.loader.factory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.plato.exception.PlatoException;
import com.example.plato.loader.YmlRegistry;
import com.example.plato.loader.config.GraphConfig;
import com.example.plato.loader.config.NodeConfig;
import com.example.plato.element.ymlNode.BeanNode;
import com.example.plato.element.ymlNode.ConditionNode;
import com.example.plato.element.ymlNode.IYmlNode;
import com.example.plato.element.ymlNode.MethodNode;
import com.example.plato.element.ymlNode.SubflowNode;
import com.example.plato.platoEnum.NodeType;
import com.google.common.collect.HashMultimap;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/2 2:43 下午
 */
public class NodeYmlFactory {

    private static final Map<String, HashMultimap<NodeType, IYmlNode>> I_NODE_MAP = new HashMap<>();
    private static final Map<String, IYmlNode> START_NODE_MAP = new HashMap<>();

    public static Map<String, IYmlNode> getStartNodeMap() {
        return START_NODE_MAP;
    }

    public static Map<String, HashMultimap<NodeType, IYmlNode>> getiNodeMap() {
        return I_NODE_MAP;
    }

    public static Map<String, HashMultimap<NodeType, IYmlNode>> getYmlNodeMap() {
        Map<String, GraphConfig> registryMap = new YmlRegistry().registry();
        registryMap.forEach(((graphIdTemp, graphConfig) -> {
            List<NodeConfig> nodeConfigs = graphConfig.getNodes();
            nodeConfigs.parallelStream().forEach(nodeConfig -> {
                NodeType type = nodeConfig.getType();
                IYmlNode iYmlNode = getIYmlNode(type).getInstance(graphConfig.getScanPackage(), nodeConfig);
                if (graphConfig.getStartNode().equals(nodeConfig.getUniqueId())) {
                    START_NODE_MAP.putIfAbsent(graphIdTemp, iYmlNode);
                }
                if (I_NODE_MAP.containsKey(graphIdTemp)) {
                    I_NODE_MAP.get(graphIdTemp).put(type, iYmlNode);
                } else {
                    HashMultimap<NodeType, IYmlNode> multimap = HashMultimap.create();
                    I_NODE_MAP.put(graphIdTemp, multimap);
                    multimap.put(type, iYmlNode);
                }
            });
        }));
        return I_NODE_MAP;
    }

    private static IYmlNode getIYmlNode(NodeType type) {
        if (NodeType.BEAN.equals(type)) {
            return new BeanNode();
        } else if (NodeType.CONDITION.equals(type)) {
            return new ConditionNode();
        } else if (NodeType.METHOD.equals(type)) {
            return new MethodNode();
        } else if (NodeType.SUBFLOW.equals(type)) {
            return new SubflowNode();
        } else {
            throw new PlatoException("NodeType error");
        }
    }
}
