package com.example.plato.loader.ymlNode;

import com.example.plato.handler.INodeWork;
import com.example.plato.loader.config.NodeConfig;
import com.example.plato.util.PlatoAssert;
import com.example.plato.util.PlatoJsonUtil;
import com.example.plato.util.SpringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/11 10:30 上午
 */
@Slf4j
public class BeanYmlNode<P, R> extends AbstractYmlNode<P, R> {

    public BeanYmlNode(NodeConfig nodeConfig) {
        super(nodeConfig);
    }

    @Override
    public R work(P p) throws InterruptedException {
        log.info("BeanYmlNode#BeanYmlNode#BeanYmlNode  p:{}", PlatoJsonUtil.toJson(p));
        String invokeElement = getNodeConfig().getInvokeElement();
        INodeWork<P, R> iNodeWork = SpringUtils.getBean(invokeElement);
        PlatoAssert.nullException(() -> "BeanYmlNode iNodeWork is null", iNodeWork);
        return iNodeWork.work(p);
    }
}
