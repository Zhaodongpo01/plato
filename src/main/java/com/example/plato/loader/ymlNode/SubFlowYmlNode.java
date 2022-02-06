package com.example.plato.loader.ymlNode;

import com.example.plato.loader.config.NodeConfig;
import com.example.plato.runningData.ResultData;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/5 16:03
 */
public class SubFlowYmlNode<P, R> extends AbstractYmlNode<P, R> {

    public SubFlowYmlNode(NodeConfig nodeConfig, String scanPackage) {
        super(nodeConfig, scanPackage);
    }

    @Override
    public R work(P p) throws InterruptedException {
        return null;
    }

    @Override
    public void hook(P p, ResultData<R> resultData) {

    }
}
