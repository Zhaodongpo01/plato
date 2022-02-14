package com.example.plato.loader.ymlNode;

import com.example.plato.loader.config.NodeConfig;
import com.example.plato.util.ClassUtil;
import com.example.plato.util.PlatoAssert;
import com.example.plato.util.SpringUtils;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/2 2:46 下午
 * 类 类型Node
 */
@Slf4j
public class MethodYmlNode<P, R> extends AbstractYmlNode<P, R> {

    public MethodYmlNode(NodeConfig nodeConfig) {
        super(nodeConfig);
    }

    @Override
    public R work(P p) throws InterruptedException {
        String invokeElement = getNodeConfig().getInvokeElement();
        String[] split = invokeElement.split(":");
        Object bean = SpringUtils.getBean(split[0]);
        PlatoAssert.nullException(() -> "MethodYmlNode work error", bean);
        String serviceName = bean.getClass().getName();
        Object result = ClassUtil.methodInvoke(serviceName, split[1], p);
        return Optional.ofNullable(result).isPresent() ? (R) result : null;
    }
}
