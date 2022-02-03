package com.example.plato.loader;

import com.example.plato.exception.PlatoException;
import com.example.plato.util.PlatoJsonUtil;
import com.example.plato.loader.config.GraphConfig;
import com.example.plato.loader.config.NodeConfig;
import com.example.plato.loader.config.SubFlow;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.config.YamlMapFactoryBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.*;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/21 3:24 下午
 */
@Slf4j
public class YmlRegistry implements GraphRegistry {

    private static final String GRAPH_CONFIG_PATH = "*-graph.yml";

    private static final String NODES = "nodes";

    private static final String NODE_CONFIG = "nodeConfig";

    private static final String SUB_FLOWS = "subFlows";

    private static final String SUB_FLOW = "subFlow";

    private static final String GRAPH_ID = "graphId";

    private static final String GRAPH_NAME = "graphName";

    private static final String GRAPH_DESC = "graphDesc";

    private static final String START_NODE = "startNode";

    private static final String SCAN_PACKAGE = "scanPackage";

    /**
     * graphId:GraphConfig
     * */
    @Override
    public Map<String, GraphConfig> registry() {
        Map<String, GraphConfig> resultMap;
        try {
            resultMap = getGraphConfigList();
        } catch (IOException e) {
            throw new PlatoException("registry get graph config error");
        }
        return resultMap;
    }

    private Map<String, GraphConfig> getGraphConfigList() throws IOException {
        Map<String, GraphConfig> graphConfigMap = new HashMap<>();
        Resource[] resources = getResource(GRAPH_CONFIG_PATH);
        Arrays.stream(resources).forEach(resource -> {
            YamlMapFactoryBean yamlMapFactoryBean = new YamlMapFactoryBean();
            yamlMapFactoryBean.setResources(resource);
            yamlMapFactoryBean.afterPropertiesSet();
            Map<String, Object> objectMap = yamlMapFactoryBean.getObject();
            buildGraphConfig(graphConfigMap, objectMap);
        });
        log.info("getGraphConfigList#graphConfigMap:{}", PlatoJsonUtil.toJson(graphConfigMap));
        return graphConfigMap;
    }

    private void buildGraphConfig(Map<String, GraphConfig> graphConfigMap, Map<String, Object> objectMap) {
        if (MapUtils.isEmpty(objectMap) || !objectMap.containsKey(NODES)) {
            return;
        }
        GraphConfig graphConfig = GraphConfig.builder().graphId(String.valueOf(objectMap.get(GRAPH_ID)))
                .graphDesc(String.valueOf(objectMap.get(GRAPH_DESC)))
                .graphName(String.valueOf(objectMap.get(GRAPH_NAME)))
                .startNode(String.valueOf(objectMap.get(START_NODE)))
                .scanPackage(String.valueOf(objectMap.get(SCAN_PACKAGE)))
                .build();
        List nodeObjects = (List) objectMap.get(NODES);
        LinkedList<NodeConfig> nodes = new LinkedList<>();
        nodeObjects.stream().filter(Objects::nonNull).forEach(NodeConfigTemp -> {
            Map<String, Object> nodeConfigMap = PlatoJsonUtil.fromJson(PlatoJsonUtil.toJson(NodeConfigTemp));
            if (MapUtils.isEmpty(nodeConfigMap) || !nodeConfigMap.containsKey(NODE_CONFIG)) {
                return;
            }
            NodeConfig nodeConfig =
                    PlatoJsonUtil.fromJson(PlatoJsonUtil.toJson(nodeConfigMap.get(NODE_CONFIG)), NodeConfig.class);
            nodeConfig.setGraphId(graphConfig.getGraphId());
            nodes.add(nodeConfig);
            Map<String, Object> nodeConfigJsonMap =
                    PlatoJsonUtil.fromJson(PlatoJsonUtil.toJson(nodeConfigMap.get(NODE_CONFIG)), Map.class);
            if (MapUtils.isEmpty(nodeConfigJsonMap) || !nodeConfigJsonMap.containsKey(SUB_FLOWS)) {
                return;
            }
            List subFlows = (List) nodeConfigJsonMap.get(SUB_FLOWS);
            nodeConfig.setSubFlows(new ArrayList<>());
            subFlows.stream().forEach(subFlowTemp -> {
                Map<String, Object> subFlowJsonMap = PlatoJsonUtil.fromJson(PlatoJsonUtil.toJson(subFlowTemp));
                if (MapUtils.isNotEmpty(subFlowJsonMap) && subFlowJsonMap.containsKey(SUB_FLOW)) {
                    SubFlow subFlow = PlatoJsonUtil.fromJson(PlatoJsonUtil.toJson(subFlowJsonMap.get(SUB_FLOW)),
                            SubFlow.class);
                    nodeConfig.getSubFlows().add(subFlow);
                }
            });
        });
        graphConfig.setNodes(nodes);
        new GraphConfig().checkConfig(graphConfig);
        graphConfigMap.put(graphConfig.getGraphId(), graphConfig);
    }

    private Resource[] getResource(String configPath) throws IOException {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(configPath);
        return resources;
    }
}