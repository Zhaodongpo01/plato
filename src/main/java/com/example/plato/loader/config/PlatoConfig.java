package com.example.plato.loader.config;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/22 6:16 下午
 */
public abstract class PlatoConfig {

    abstract void validate();

    public void checkConfig(GraphConfig graphConfig) {
        graphConfig.validate();
        List<NodeConfig> nodes = graphConfig.getNodes();
        nodes.parallelStream().forEach(nodeConfig -> {
            nodeConfig.validate();
            List<SubFlow> subFlows = nodeConfig.getSubFlows();
            if (CollectionUtils.isNotEmpty(subFlows)) {
                subFlows.parallelStream().forEach(subFlow -> subFlow.validate());
            }
        });
    }
}
