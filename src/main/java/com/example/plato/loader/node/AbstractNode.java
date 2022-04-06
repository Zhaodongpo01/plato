package com.example.plato.loader.node;

import com.example.plato.handler.INodeWork;
import com.example.plato.loader.loaderConfig.NodeConfig;

import lombok.Data;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/3 2:50 下午
 */
@Data
public abstract class AbstractNode<P, R> implements INodeWork<P, R> {

    private NodeConfig nodeConfig;

    public AbstractNode(NodeConfig nodeConfig) {
        this.nodeConfig = nodeConfig;
    }

}
