package com.example.plato.loader.registry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.config.YamlMapFactoryBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.example.plato.loader.loaderConfig.GraphConfig;
import com.example.plato.loader.loaderConfig.NodeConfig;
import com.example.plato.loader.loaderConfig.SubFlowConfig;
import com.example.plato.util.PlatoJsonUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/21 3:24 下午
 */
@Slf4j
public class YmlRegistry implements GraphRegistry {

    private static final String GRAPH_CONFIG_PATH = "graph/*-graph.yml";

    private static final String NODES = "nodes";

    private static final String NODE_CONFIG = "nodeConfig";

    private static final String SUB_FLOWS = "subFlows";

    private static final String SUB_FLOW = "subFlow";

    private static final String GRAPH_ID = "graphId";

    private static final String GRAPH_NAME = "graphName";

    private static final String GRAPH_DESC = "graphDesc";

    private static final String START_NODE = "startNode";

    private static final String CHECK_NEXT_HAS_RESULT = "checkNextResult";

    private static final Map<String, GraphConfig> resultMap = new HashMap<>();

    /**
     * graphId:GraphConfig
     */
    @Override
    public Map<String, GraphConfig> registry() {
        try {
            getGraphConfigs();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultMap;
    }

    private void getGraphConfigs() throws IOException {
        Resource[] resources = getResource();
        Arrays.stream(resources).forEach(resource -> {
            YamlMapFactoryBean yamlMapFactoryBean = new YamlMapFactoryBean();
            yamlMapFactoryBean.setResources(resource);
            yamlMapFactoryBean.afterPropertiesSet();
            Map<String, Object> objectMap = yamlMapFactoryBean.getObject();
            buildGraphConfig(resultMap, objectMap);
        });
    }

    private void buildGraphConfig(Map<String, GraphConfig> graphConfigMap, Map<String, Object> objectMap) {
        if (MapUtils.isEmpty(objectMap) || !objectMap.containsKey(NODES)) {
            return;
        }
        String graphId = String.valueOf(objectMap.get(GRAPH_ID));
        if (resultMap.containsKey(graphId)) {
            return;
        }
        GraphConfig graphConfig = GraphConfig.builder().graphId(graphId)
                .graphDesc(String.valueOf(objectMap.get(GRAPH_DESC)))
                .graphName(String.valueOf(objectMap.get(GRAPH_NAME)))
                .startNode(String.valueOf(objectMap.get(START_NODE)))
                .build();
        List nodeObjects = (List) objectMap.get(NODES);
        List<NodeConfig> nodes = new ArrayList<>();
        nodeObjects.stream().filter(Objects::nonNull).forEach(NodeConfigTemp -> {
            Map<String, Object> nodeConfigMap = PlatoJsonUtil.fromJson(PlatoJsonUtil.toJson(NodeConfigTemp));
            if (MapUtils.isEmpty(nodeConfigMap) || !nodeConfigMap.containsKey(NODE_CONFIG)) {
                return;
            }
            NodeConfig nodeConfig =
                    PlatoJsonUtil.fromJson(PlatoJsonUtil.toJson(nodeConfigMap.get(NODE_CONFIG)), NodeConfig.class);
            nodeConfig.setGraphId(graphConfig.getGraphId());
            nodes.add(nodeConfig);
            Map nodeConfigJsonMap =
                    PlatoJsonUtil.fromJson(PlatoJsonUtil.toJson(nodeConfigMap.get(NODE_CONFIG)), Map.class);
            if (MapUtils.isEmpty(nodeConfigJsonMap) || !nodeConfigJsonMap.containsKey(SUB_FLOWS)) {
                return;
            }
            List subFlows = (List) nodeConfigJsonMap.get(SUB_FLOWS);
            nodeConfig.setSubFlows(new ArrayList<>());
            subFlows.forEach(subFlowTemp -> {
                Map<String, Object> subFlowJsonMap = PlatoJsonUtil.fromJson(PlatoJsonUtil.toJson(subFlowTemp));
                if (MapUtils.isNotEmpty(subFlowJsonMap) && subFlowJsonMap.containsKey(SUB_FLOW)) {
                    SubFlowConfig subFlowConfig = PlatoJsonUtil.fromJson(PlatoJsonUtil.toJson(subFlowJsonMap.get(SUB_FLOW)),
                            SubFlowConfig.class);
                    nodeConfig.getSubFlows().add(subFlowConfig);
                }
            });
        });
        graphConfig.setNodes(nodes);
        graphConfig.checkConfig(graphConfig);
        graphConfigMap.put(graphConfig.getGraphId(), graphConfig);
    }

    private Resource[] getResource() throws IOException {
        ResourcePatternResolver resourcePatResolver = new PathMatchingResourcePatternResolver();
        return resourcePatResolver.getResources(YmlRegistry.GRAPH_CONFIG_PATH);
    }
}
