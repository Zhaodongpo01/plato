package com.example.plato.loader.ymlNode;

import com.example.plato.handler.INodeWork;
import com.example.plato.loader.config.NodeConfig;
import lombok.Data;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/3 2:50 下午
 */
@Data
public abstract class AbstractYmlNode<P, R> implements INodeWork<P, R> {

    private NodeConfig nodeConfig;

    public AbstractYmlNode(NodeConfig nodeConfig) {
        this.nodeConfig = nodeConfig;
    }

}
