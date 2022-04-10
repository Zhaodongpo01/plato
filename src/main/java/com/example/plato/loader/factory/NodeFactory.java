package com.example.plato.loader.factory;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.example.plato.element.PlatoNodeBuilder;
import com.example.plato.exception.PlatoException;
import com.example.plato.loader.holder.NodeHolder;
import com.example.plato.loader.loaderConfig.GraphConfig;
import com.example.plato.loader.loaderConfig.NodeConfig;
import com.example.plato.loader.registry.YmlRegistry;
import com.example.plato.platoEnum.NodeType;
import com.example.plato.util.PlatoJsonUtil;
import com.google.common.collect.Sets;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * create 2022/3/20 12:02 下午
 */
@Slf4j
public class NodeFactory {


    private static final String SPLIT = ",";

    private final Map<String, PlatoNodeBuilder<?, ?>> platoNodeBuilderMap = new ConcurrentHashMap<>();

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
                .forEach(NodeHolder::putNodeConfig));
        log.info("properties parser done...............");
    }

    public PlatoNodeBuilder buildProxy(String startNode, String graphId) {
        Map<String, NodeConfig> nodeMap = NodeHolder.getNodeMap(graphId);
        NodeConfig firstNodeConfig = nodeMap.get(startNode);
        PlatoNodeBuilder firstPlatoNodeBuilder = convertConfig2Builder(firstNodeConfig, graphId);
        buildProxy(firstPlatoNodeBuilder, firstNodeConfig, nodeMap);
        return firstPlatoNodeBuilder;
    }

    private void buildProxy(PlatoNodeBuilder platoNodeBuilder, NodeConfig nodeConfig,
            Map<String, NodeConfig> nodeMap) {
        if (StringUtils.isBlank(nodeConfig.getNext())) {
            return;
        }
        String[] nextNodes = nodeConfig.getNext().split(SPLIT);
        Arrays.stream(nextNodes).forEach(nextNode -> {
            if (!nodeMap.containsKey(nextNode)) {
                throw new PlatoException("buildProxy nextNode not exists");
            }
            NodeConfig nextNodeConfig = nodeMap.get(nextNode);
            String pre = nextNodeConfig.getPre();
            Set<String> preSet = Sets.newConcurrentHashSet();
            if (StringUtils.isNotBlank(pre)) {
                String[] split = pre.split(SPLIT);
                preSet.addAll(Stream.of(split).collect(Collectors.toSet()));
            }
            if (!platoNodeBuilderMap.containsKey(nextNode)) {
                PlatoNodeBuilder nextPlatoNodeBuilder =
                        convertConfig2Builder(nextNodeConfig, nextNodeConfig.getGraphId());
                platoNodeBuilderMap.put(nextNode, nextPlatoNodeBuilder);
                platoNodeBuilder.next(nextPlatoNodeBuilder, preSet.contains(nodeConfig.getUniqueId()));
                buildProxy(nextPlatoNodeBuilder, nextNodeConfig, nodeMap);
            } else {
                platoNodeBuilder.next(platoNodeBuilderMap.get(nextNode), preSet.contains(nodeConfig.getUniqueId()));
            }
        });
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