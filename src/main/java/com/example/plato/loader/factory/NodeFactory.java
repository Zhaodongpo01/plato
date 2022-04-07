package com.example.plato.loader.factory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.example.plato.element.PlatoNodeProxy.PlatoNodeBuilder;
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
 * @date 2022/3/20 12:02 下午
 */
@Slf4j
@Configuration
public class NodeFactory {

    @Autowired
    private BeanNodeFactory beanNodeFactory;

    @Autowired
    private MethodNodeFactory methodNodeFactory;

    private static final String SPLIT = ",";

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
        Set<String> buildNodeSet = new HashSet<>();
        buildProxy(firstPlatoNodeBuilder, firstNodeConfig, nodeMap, buildNodeSet);
        return firstPlatoNodeBuilder;
    }

    private void buildProxy(PlatoNodeBuilder platoNodeBuilder, NodeConfig nodeConfig, Map<String, NodeConfig> nodeMap,
            Set<String> buildNodeSet) {
        if (StringUtils.isBlank(nodeConfig.getNext()) || buildNodeSet.contains(nodeConfig.getUniqueId())) {
            return;
        }
        String[] nextNodes = nodeConfig.getNext().split(SPLIT);
        buildNodeSet.add(nodeConfig.getUniqueId());
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
            PlatoNodeBuilder nextPlatoNodeBuilder = convertConfig2Builder(nextNodeConfig, nextNodeConfig.getGraphId());
            platoNodeBuilder.next(nextPlatoNodeBuilder, preSet.contains(nodeConfig.getUniqueId()));
            buildProxy(nextPlatoNodeBuilder, nextNodeConfig, nodeMap, buildNodeSet);
        });
    }

    private PlatoNodeBuilder convertConfig2Builder(NodeConfig nodeConfig, String graphId) {
        if (NodeType.METHOD.equals(nodeConfig.getType())) {
            return methodNodeFactory.createPlatoNodeBuilder(graphId, nodeConfig);
        } else if (NodeType.BEAN.equals(nodeConfig.getType())) {
            return beanNodeFactory.createPlatoNodeBuilder(graphId, nodeConfig);
        } else {
            throw new PlatoException("NodeType error");
        }
    }

}

