package com.example.plato.holder;

import com.example.plato.element.Graph;
import com.example.plato.exception.PlatoException;
import com.example.plato.runningData.GraphRunningInfo;
import com.example.plato.runningData.NodeRunningInfo;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhaodongpo
 * @version 1.0
 * 2022/1/23 10:59 上午
 * 全局定义和运行数据
 */
@Slf4j
public class GraphHolder {

    /**
     * completableFuture所以没有使用ThreadLocal  <graphId : <graphTraceId,GraphRunningInfo>>
     */
    private static final Map<String, Map<String, GraphRunningInfo>> graphRunningInfoHolderMap =
            new ConcurrentHashMap<>();
    private static final Map<String, Graph> graphMap = new ConcurrentHashMap<>();

    public static GraphRunningInfo getGraphRunningInfo(String graphId, String graphTraceId) {
        if (StringUtils.isAnyBlank(graphId, graphTraceId)
                || !graphRunningInfoHolderMap.containsKey(graphId)
                || !graphRunningInfoHolderMap.get(graphId).containsKey(graphTraceId)) {
            throw new PlatoException("getGraphRunningInfo error");
        }
        return graphRunningInfoHolderMap.get(graphId).get(graphTraceId);
    }

    public static GraphRunningInfo removeGraphRunningInfo(String graphId, String graphTraceId) {
        if (StringUtils.isAnyBlank(graphId, graphTraceId)
                || !graphRunningInfoHolderMap.containsKey(graphId)
                || !graphRunningInfoHolderMap.get(graphId).containsKey(graphTraceId)) {
            throw new PlatoException("removeGraphTrace error");
        }
        return graphRunningInfoHolderMap.get(graphId).remove(graphTraceId);
    }

    public static GraphRunningInfo putGraphRunningInfo(String graphId, String graphTraceId,
            GraphRunningInfo graphRunningInfo) {
        if (StringUtils.isAnyBlank(graphId, graphTraceId)
                || !Optional.ofNullable(graphRunningInfo).isPresent()) {
            return null;
        }
        if (graphRunningInfoHolderMap.containsKey(graphId)) {
            return graphRunningInfoHolderMap.get(graphId).put(graphTraceId, graphRunningInfo);
        }
        ConcurrentHashMap<String, GraphRunningInfo> concurrentHashMap = new ConcurrentHashMap<>();
        concurrentHashMap.put(graphTraceId, graphRunningInfo);
        graphRunningInfoHolderMap.put(graphId, concurrentHashMap);
        return graphRunningInfo;
    }

    public static Map<String, NodeRunningInfo> getNodeRunningInfoMap(String graphId, String graphTraceId) {
        Map<String, NodeRunningInfo> nodeRunningInfoMap = new ConcurrentHashMap<>();
        GraphRunningInfo graphRunningInfo;
        if (StringUtils.isAnyBlank(graphId, graphTraceId)
                || !graphRunningInfoHolderMap.containsKey(graphId)
                || (graphRunningInfo = graphRunningInfoHolderMap.get(graphId).get(graphTraceId)) == null) {
            return nodeRunningInfoMap;
        }
        return graphRunningInfo.getNodeRunningInfoMap();
    }

    public static NodeRunningInfo getNodeRunningInfo(String graphId, String graphTraceId, String uniqueId) {
        Map<String, NodeRunningInfo> nodeRunningInfoMap = new ConcurrentHashMap<>();
        if (StringUtils.isBlank(uniqueId)
                || MapUtils.isEmpty((nodeRunningInfoMap = getNodeRunningInfoMap(graphId, graphTraceId)))) {
            return null;
        }
        return nodeRunningInfoMap.get(uniqueId);
    }

    public static <R> NodeRunningInfo<R> putNodeRunningInfo(String graphId,
            String graphTraceId,
            String uniqueId,
            NodeRunningInfo<R> nodeRunningInfo) {
        GraphRunningInfo graphRunningInfo = graphRunningInfoHolderMap.get(graphId).get(graphTraceId);
        Map<String, NodeRunningInfo> nodeRunningInfoMap = graphRunningInfo.getNodeRunningInfoMap();
        nodeRunningInfoMap.put(uniqueId, nodeRunningInfo);
        return nodeRunningInfo;
    }

    public static Graph putGraph(Graph graph) {
        if (graphMap.containsKey(graph.getGraphId())) {
            log.info("graphMap exists graphId{} please do not put again", graph.getGraphId());
            return graphMap.get(graph.getGraphId());
        }
        return graphMap.put(graph.getGraphId(), graph);
    }

    public static Graph getGraph(String graphId) {
        if (StringUtils.isBlank(graphId)) {
            return null;
        }
        return graphMap.get(graphId);
    }
}
