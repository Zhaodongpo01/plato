package com.example.plato.loader.ymlNode;

import com.example.plato.loader.config.NodeConfig;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/5 16:03
 */
public class SubFlowYmlNode<P, R> extends AbstractYmlNode<P, R> {

    public SubFlowYmlNode(NodeConfig nodeConfig) {
        super(nodeConfig);
    }

    @Override
    public R work(P p) throws InterruptedException {
        return null;
    }
}
