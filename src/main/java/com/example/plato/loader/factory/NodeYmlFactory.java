package com.example.plato.loader.factory;

import com.example.plato.exception.PlatoException;
import com.example.plato.loader.config.NodeConfig;
import com.example.plato.loader.ymlNode.AbstractYmlNode;
import com.example.plato.loader.ymlNode.BeanYmlNode;
import com.example.plato.loader.ymlNode.ScriptYmlNode;
import com.example.plato.loader.ymlNode.MethodYmlNode;
import com.example.plato.loader.ymlNode.SubFlowYmlNode;
import com.example.plato.platoEnum.NodeType;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/2 2:43 下午
 */
public class NodeYmlFactory {

    public static AbstractYmlNode getIYmlNode(NodeConfig nodeConfig) {
        if (NodeType.METHOD.equals(nodeConfig.getType())) {
            return new MethodYmlNode(nodeConfig);
        } else if (NodeType.SUBFLOW.equals(nodeConfig.getType())) {
            return new SubFlowYmlNode(nodeConfig);
        } else if (NodeType.BEAN.equals(nodeConfig.getType())) {
            return new BeanYmlNode(nodeConfig);
        } else if (NodeType.SCRIPT.equals(nodeConfig.getType())) {
            return new ScriptYmlNode(nodeConfig);
        } else {
            throw new PlatoException("NodeType error");
        }
    }
}
