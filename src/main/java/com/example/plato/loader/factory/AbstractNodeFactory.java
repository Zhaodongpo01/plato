package com.example.plato.loader.factory;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.example.plato.element.PlatoNodeProxy.PlatoNodeBuilder;
import com.example.plato.handler.AfterHandlerParser;
import com.example.plato.handler.INodeWork;
import com.example.plato.handler.PreHandlerParser;
import com.example.plato.loader.loaderConfig.NodeConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/3/20 5:49 下午
 */
@Slf4j
public abstract class AbstractNodeFactory<P, R> {

    public abstract INodeWork<P, R> createINodeWork(NodeConfig nodeConfig);

    private static final String SPILIT = ",";

    public PlatoNodeBuilder<P, R> createPlatoNodeBuilder(String graphId, NodeConfig nodeConfig) {
        PlatoNodeBuilder<P, R> nodeBuilder = new PlatoNodeBuilder<>();
        nodeBuilder.setINodeWork(createINodeWork(nodeConfig));
        nodeBuilder.setUniqueId(nodeConfig.getUniqueId());
        nodeBuilder.setGraphId(graphId);
        nodeBuilder.checkNextResult(nodeConfig.isCheckNextResult());
        if (Objects.nonNull(nodeConfig.getAfterHandler())) {
            nodeBuilder.setAfterHandler(new AfterHandlerParser(nodeConfig));
        }
        if (Objects.nonNull(nodeConfig.getPreHandler())) {
            nodeBuilder.setPreHandler(new PreHandlerParser<>(nodeConfig));
        }
        return nodeBuilder;
    }
}
