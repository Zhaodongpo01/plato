package com.example.plato.loader.factory;

import org.springframework.stereotype.Component;

import com.example.plato.handler.INodeWork;
import com.example.plato.loader.loaderConfig.NodeConfig;
import com.example.plato.loader.node.MethodNode;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/3/20 5:46 下午
 */
@Component
public class MethodNodeFactory<P, R> extends AbstractNodeFactory<P, R> {

    @Override
    public INodeWork<P, R> createINodeWork(NodeConfig nodeConfig) {
        return new MethodNode<>(nodeConfig);
    }
}
