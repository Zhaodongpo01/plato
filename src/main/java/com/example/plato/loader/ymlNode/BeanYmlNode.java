package com.example.plato.loader.ymlNode;

import com.example.plato.loader.config.NodeConfig;
import com.example.plato.runningData.ResultData;
import com.example.plato.util.ClassUtil;
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
public class BeanYmlNode<P, R> extends AbstractYmlNode<P, R> {

    public BeanYmlNode(NodeConfig nodeConfig) {
        super(nodeConfig);
    }

    @Override
    public R work(P p) throws InterruptedException {
        String invokeElement = getNodeConfig().getInvokeElement();
        String[] split = invokeElement.split(":");
        Object bean = SpringUtils.getBean(split[0]);
        String serviceName = bean.getClass().getName();
        Object result = ClassUtil.methodInvoke(serviceName, split[1], p);
        return Optional.ofNullable(result).isPresent() ? (R) result : null;
    }

    @Override
    public void hook(P p, ResultData<R> resultData) {

    }
}
