package com.example.plato.loader.ymlNode;

import java.util.Arrays;
import java.util.LinkedList;

import com.example.plato.loader.config.NodeConfig;
import com.example.plato.platoEnum.ScriptType;
import com.example.plato.util.PlatoAssert;
import com.example.plato.util.PyScriptParserUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/13 4:04 下午
 */
@Slf4j
public class ScriptYmlNode<P, R> extends AbstractYmlNode<P, R> {

    public ScriptYmlNode(NodeConfig nodeConfig) {
        super(nodeConfig);
    }

    @Override
    public R work(P p) throws InterruptedException {
        String invokeElement = getNodeConfig().getInvokeElement();
        PlatoAssert.emptyException(() -> "ScriptYmlNode invokeElement is empty", invokeElement);
        LinkedList<String> elementList = new LinkedList<>();
        elementList.add(ScriptType.PYTHON.getScriptType());
        elementList.add(invokeElement);
        String[] args = (String[]) p;
        elementList.addAll(Arrays.asList(args));
        PyScriptParserUtil.runPyScript((String[]) p);
        return null;
    }
}
