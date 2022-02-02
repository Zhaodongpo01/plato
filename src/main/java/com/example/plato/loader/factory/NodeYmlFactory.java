package com.example.plato.loader.factory;

import java.util.HashMap;
import java.util.Map;

import com.example.plato.loader.ymlNode.BeanYmlNode;
import com.example.plato.loader.ymlNode.ConditionYmlNode;
import com.example.plato.loader.ymlNode.IYmlNode;
import com.example.plato.loader.ymlNode.MethodYmlNode;
import com.example.plato.loader.ymlNode.SubflowYmlNode;
import com.example.plato.platoEnum.NodeType;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/2 2:43 下午
 */
public class NodeYmlFactory {

    private static final Map<String, IYmlNode> I_COMPONENT_MAP = new HashMap<>();

    static {
        I_COMPONENT_MAP.put(NodeType.BEAN.name(), new BeanYmlNode());
        I_COMPONENT_MAP.put(NodeType.METHOD.name(), new MethodYmlNode());
        I_COMPONENT_MAP.put(NodeType.CONDITION.name(), new ConditionYmlNode());
        I_COMPONENT_MAP.put(NodeType.SUBFLOW.name(), new SubflowYmlNode());
    }

    public static IYmlNode getComponent(String component) {
        return I_COMPONENT_MAP.get(component);
    }

}
