package com.example.plato.loader.node;

import java.util.Optional;

import com.example.plato.loader.loaderConfig.NodeConfig;
import com.example.plato.runningData.ResultData;
import com.example.plato.util.ClassUtil;
import com.example.plato.util.PlatoAssert;
import com.example.plato.util.PlatoJsonUtil;
import com.example.plato.util.SpringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/2 2:46 下午
 * 方法 类型Node
 */
@Slf4j
public class MethodNode<P, R> extends AbstractNode<P, R> {

    public MethodNode(NodeConfig nodeConfig) {
        super(nodeConfig);
    }

    @Override
    public R work(P p) throws InterruptedException {
        String invokeElement = getNodeConfig().getInvokeElement();
        String[] split = invokeElement.split(":");
        Object bean = SpringUtils.getBean(split[0]);
        PlatoAssert.nullException(() -> "MethodNode work error", bean);
        String serviceName = bean.getClass().getName();
        Object result = ClassUtil.methodInvoke(serviceName, split[1], p);
        return Optional.ofNullable(result).isPresent() ? (R) result : null;
    }

    @Override
    public void hook(P p, ResultData<R> resultData) {
        log.info("uniqueId:{},MethodNode#hook p参数:{},resultData结果:{}", resultData.getUniqueId(),
                PlatoJsonUtil.toJson(p),
                PlatoJsonUtil.toJson(resultData.getResult()));
    }
}
