package com.example.plato.loader.node;

import com.example.plato.handler.INodeWork;
import com.example.plato.loader.loaderConfig.NodeConfig;
import com.example.plato.runningData.ResultData;
import com.example.plato.util.PlatoAssert;
import com.example.plato.util.PlatoJsonUtil;
import com.example.plato.util.SpringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/11 10:30 上午
 * bean  类型Node
 */
@Slf4j
public class BeanNode<P, R> extends AbstractNode<P, R> {

    public BeanNode(NodeConfig nodeConfig) {
        super(nodeConfig);
    }

    @Override
    public R work(P p) throws InterruptedException {
        String invokeElement = getNodeConfig().getInvokeElement();
        Object bean = SpringUtils.getBean(invokeElement);
        PlatoAssert.nullException(() -> "BeanNode iNodeWork is null", bean);
        INodeWork iNodeWork = (INodeWork) bean;
        return (R) iNodeWork.work(p);
    }

    @Override
    public void hook(P p, ResultData<R> resultData) {
        log.info("BeanNode#hook p参数:{},resultData结果:{}", PlatoJsonUtil.toJson(p),
                PlatoJsonUtil.toJson(resultData.getResult()));
    }
}
