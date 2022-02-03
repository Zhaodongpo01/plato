package com.example.plato.loader.factory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.example.plato.exception.PlatoException;
import com.example.plato.loader.YmlRegistry;
import com.example.plato.loader.config.GraphConfig;
import com.example.plato.loader.config.NodeConfig;
import com.example.plato.loader.ymlNode.BeanYmlNode;
import com.example.plato.loader.ymlNode.ConditionYmlNode;
import com.example.plato.loader.ymlNode.IYmlNode;
import com.example.plato.loader.ymlNode.MethodYmlNode;
import com.example.plato.loader.ymlNode.SubflowYmlNode;
import com.example.plato.platoEnum.NodeType;
import com.google.common.collect.HashMultimap;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/2 2:43 下午
 */
public class NodeYmlFactory {

    private static final Map<String, HashMultimap<NodeType, IYmlNode>> I_NODE_MAP = new HashMap<>();

    public Map<String, HashMultimap<NodeType, IYmlNode>> getYmlNodeMap() {
        Map<String, GraphConfig> registryMap = new YmlRegistry().registry();
        registryMap.forEach(((graphIdTemp, graphConfig) -> {
            LinkedList<NodeConfig> nodeConfigs = graphConfig.getNodes();
            nodeConfigs.parallelStream().forEach(nodeConfig -> {
                NodeType type = nodeConfig.getType();
                if (I_NODE_MAP.containsKey(graphIdTemp)) {
                    I_NODE_MAP.get(graphIdTemp).put(type, getIYmlNode(type).getInstance(graphConfig.getScanPackage()));
                } else {
                    HashMultimap<NodeType, IYmlNode> multimap = HashMultimap.create();
                    I_NODE_MAP.put(graphIdTemp, multimap);
                    multimap.put(type, getIYmlNode(type).getInstance(graphConfig.getScanPackage()));
                }
            });
        }));
        return I_NODE_MAP;
    }

    private IYmlNode getIYmlNode(NodeType type) {
        if (NodeType.BEAN.equals(type)) {
            return new BeanYmlNode();
        } else if (NodeType.CONDITION.equals(type)) {
            return new ConditionYmlNode();
        } else if (NodeType.METHOD.equals(type)) {
            return new MethodYmlNode();
        } else if (NodeType.SUBFLOW.equals(type)) {
            return new SubflowYmlNode();
        } else {
            throw new PlatoException("NodeType error");
        }
    }
}
