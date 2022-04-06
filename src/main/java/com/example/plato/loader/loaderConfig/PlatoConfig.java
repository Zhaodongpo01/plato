package com.example.plato.loader.loaderConfig;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/3/19 12:41 下午
 */
public abstract class PlatoConfig {

    abstract void validate();

    public void checkConfig(GraphConfig graphConfig) {
        graphConfig.validate();
        List<NodeConfig> nodes = graphConfig.getNodes();
        nodes.parallelStream().forEach(nodeConfig -> {
            nodeConfig.validate();
            List<SubFlowConfig> subFlows = nodeConfig.getSubFlows();
            if (CollectionUtils.isNotEmpty(subFlows)) {
                subFlows.parallelStream().forEach(SubFlowConfig::validate);
            }
        });
    }
}
