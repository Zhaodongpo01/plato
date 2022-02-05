package com.example.plato.loader.factory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.plato.exception.PlatoException;
import com.example.plato.loader.YmlRegistry;
import com.example.plato.loader.config.GraphConfig;
import com.example.plato.loader.config.NodeConfig;
import com.example.plato.loader.ymlNode.AbstractYmlNode;
import com.example.plato.loader.ymlNode.BeanYmlNode;
import com.example.plato.loader.ymlNode.MethodYmlNode;
import com.example.plato.platoEnum.NodeType;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/2 2:43 下午
 */
public class NodeYmlFactory {

    private static final Map<String, AbstractYmlNode> START_NODE_MAP = new HashMap<>();

    public static Map<String, AbstractYmlNode> getStartNodeMap() {
        return START_NODE_MAP;
    }

    public static void getYmlNodeMap() {
        Map<String, GraphConfig> registryMap = new YmlRegistry().registry();
        registryMap.forEach(((graphIdTemp, graphConfig) -> {
            List<NodeConfig> nodeConfigs = graphConfig.getNodes();
            nodeConfigs.parallelStream().forEach(nodeConfig -> {
                AbstractYmlNode abstractYmlNode = getIYmlNode(nodeConfig, graphConfig.getScanPackage());
                if (graphConfig.getStartNode().equals(nodeConfig.getUniqueId())) {
                    START_NODE_MAP.putIfAbsent(graphIdTemp, abstractYmlNode);
                }
            });
        }));
    }

    private static AbstractYmlNode getIYmlNode(NodeConfig nodeConfig, String scanPackage) {
        if (NodeType.BEAN.equals(nodeConfig.getType())) {
            return new BeanYmlNode(nodeConfig, scanPackage);
        } else if (NodeType.METHOD.equals(nodeConfig.getType())) {
            return new MethodYmlNode(nodeConfig, scanPackage);
        } else {
            throw new PlatoException("NodeType error");
        }
    }
}
