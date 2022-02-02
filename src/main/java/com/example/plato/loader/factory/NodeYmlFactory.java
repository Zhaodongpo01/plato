package com.example.plato.loader.factory;

import java.util.HashMap;
import java.util.Map;

import com.example.plato.loader.ymlNode.BeanNode;
import com.example.plato.loader.ymlNode.ConditionNode;
import com.example.plato.loader.ymlNode.INode;
import com.example.plato.loader.ymlNode.MethodNode;
import com.example.plato.loader.ymlNode.SubflowNode;
import com.example.plato.platoEnum.NodeType;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/2 2:43 下午
 */
public class NodeYmlFactory {

    private static final Map<String, INode> I_COMPONENT_MAP = new HashMap<>();

    static {
        I_COMPONENT_MAP.put(NodeType.BEAN.name(), new BeanNode());
        I_COMPONENT_MAP.put(NodeType.METHOD.name(), new MethodNode());
        I_COMPONENT_MAP.put(NodeType.CONDITION.name(), new ConditionNode());
        I_COMPONENT_MAP.put(NodeType.SUBFLOW.name(), new SubflowNode());
    }

    public static INode getComponent(String component) {
        return I_COMPONENT_MAP.get(component);
    }

}
