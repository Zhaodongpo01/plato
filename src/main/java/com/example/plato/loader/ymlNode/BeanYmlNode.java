package com.example.plato.loader.ymlNode;

import com.example.plato.loader.config.NodeConfig;
import com.example.plato.runningData.ResultData;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/11 10:30 上午
 */
public class BeanYmlNode<P, R> extends AbstractYmlNode<P, R>{

    public BeanYmlNode(NodeConfig nodeConfig) {
        super(nodeConfig);
    }

    @Override
    public R work(P p) throws InterruptedException {
        return null;
    }

    @Override
    public void hook(P p, ResultData<R> resultData) {

    }
}
