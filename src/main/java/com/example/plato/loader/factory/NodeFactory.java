package com.example.plato.loader.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.collections4.MapUtils;

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
public class NodeFactory {


    private static final String SPLIT = ",";

    private Map<String, AtomicBoolean> reBuilder = new ConcurrentHashMap<>();

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
        registryMap.forEach((graphId, graphInfo) -> graphInfo.getNodes()
                .forEach(nodeConfig -> NodeHolder.putNodeConfig(nodeConfig)));
        log.info("properties parser done...............");
    }

    public <R, P> PlatoNodeBuilder<P, R> buildProxy(String startNode, String graphId) {
        Map<String, NodeConfig> nodeMap = NodeHolder.getNodeMap(graphId);
        NodeConfig firstNodeConfig = nodeMap.get(startNode);
        PlatoNodeBuilder firstPlatoNodeBuilder = convertConfig2Builder(firstNodeConfig, graphId);
        buildProxy(firstPlatoNodeBuilder, firstNodeConfig, nodeMap);
        return firstPlatoNodeBuilder;
    }

    private void buildProxy(PlatoNodeBuilder platoNodeBuilder, NodeConfig nodeConfig,
            Map<String, NodeConfig> nodeMap) {

    }

    private PlatoNodeBuilder convertConfig2Builder(NodeConfig nodeConfig, String graphId) {
        if (NodeType.METHOD.equals(nodeConfig.getType())) {
            return new MethodNodeFactory<>().createPlatoNodeBuilder(graphId, nodeConfig);
        } else if (NodeType.BEAN.equals(nodeConfig.getType())) {
            return new BeanNodeFactory<>().createPlatoNodeBuilder(graphId, nodeConfig);
        } else {
            throw new PlatoException("NodeType error");
        }
    }

}

