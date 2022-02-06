package com.example.plato.loader.ymlNode;

import com.example.plato.loader.config.NodeConfig;
import com.example.plato.runningData.ResultData;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/2 2:46 下午
 * 方法类型Node
 */
public class MethodYmlNode<P, R> extends AbstractYmlNode<P, R> {

    public MethodYmlNode(NodeConfig nodeConfig, String scanPackage) {
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
