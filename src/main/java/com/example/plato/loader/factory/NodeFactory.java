package com.example.plato.loader.factory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.example.plato.element.PlatoNodeProxy;
import com.example.plato.element.PlatoNodeProxy.PlatoNodeBuilder;
import com.example.plato.exception.PlatoException;
import com.example.plato.loader.holder.NodeHolder;
import com.example.plato.loader.loaderConfig.GraphConfig;
import com.example.plato.loader.loaderConfig.NodeConfig;
import com.example.plato.loader.registry.YmlRegistry;
import com.example.plato.platoEnum.NodeType;
import com.example.plato.util.PlatoJsonUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/3/20 12:02 下午
 */
@Slf4j
@Configuration
public class NodeFactory {

    @Autowired
    private BeanNodeFactory beanNodeFactory;

    @Autowired
    private MethodNodeFactory methodNodeFactory;

    public void createNode() {
        Map<String, GraphConfig> registryMap = new YmlRegistry().registry();
        if (MapUtils.isEmpty(registryMap)) {
            log.warn("createNode registryMap is empty");
            return;
        }
        log.info("createNode#registry:{}", PlatoJsonUtil.toJson(registryMap));
        parser(registryMap);
    }

    private void parser(Map<String, GraphConfig> registryMap) {
        registryMap.forEach((graphId, graphInfo) -> {
            List<NodeConfig> nodes = graphInfo.getNodes();
            nodes.forEach(nodeConfig -> NodeHolder.putNodeConfig(nodeConfig));
        });
        log.info("properties parser done...............");
    }

    public PlatoNodeProxy buildProxy(String startNode, String graphId) {
        Map<String, NodeConfig> nodeMap = NodeHolder.getNodeMap(graphId);
        if (MapUtils.isEmpty(nodeMap)) {
            throw new PlatoException("yml properties loading ......");
        }
        NodeConfig firstNodeConfig = nodeMap.get(startNode);
        PlatoNodeBuilder firstPlatoNodeBuilder = getPlatoBuilder(graphId, firstNodeConfig);
        return firstPlatoNodeBuilder.build();
    }

    private PlatoNodeBuilder getPlatoBuilder(String graphId, NodeConfig nodeConfig) {
        if (NodeType.METHOD.equals(nodeConfig.getType())) {
            return methodNodeFactory.createPlatoNodeBuilder(graphId, nodeConfig);
        } else if (NodeType.BEAN.equals(nodeConfig.getType())) {
            return beanNodeFactory.createPlatoNodeBuilder(graphId, nodeConfig);
        } else {
            throw new PlatoException("NodeType error");
        }
    }
}

