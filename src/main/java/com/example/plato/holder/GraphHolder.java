package com.example.plato.holder;

import com.example.plato.exception.PlatoException;
import com.example.plato.runningData.GraphRunningInfo;
import com.example.plato.runningData.NodeRunningInfo;
import com.example.plato.util.PlatoAssert;

import lombok.extern.slf4j.Slf4j;

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
     * 没有使用ThreadLocal  <graphId : <graphTraceId,GraphRunningInfo>>
     */
    /*private static final Map<String, Map<String, GraphRunningInfo>> GRAPH_RUNNING_INFO_MAP = new ConcurrentHashMap<>();

    public static GraphRunningInfo getGraphRunningInfo(String graphId, String graphTraceId) {
        if (StringUtils.isAnyBlank(graphId, graphTraceId)
                || !GRAPH_RUNNING_INFO_MAP.containsKey(graphId)
                || !GRAPH_RUNNING_INFO_MAP.get(graphId).containsKey(graphTraceId)) {
            throw new PlatoException("getGraphRunningInfo error");
        }
        return GRAPH_RUNNING_INFO_MAP.get(graphId).get(graphTraceId);
    }

    public static GraphRunningInfo removeGraphRunningInfo(String graphId, String graphTraceId) {
        if (StringUtils.isAnyBlank(graphId, graphTraceId)
                || !GRAPH_RUNNING_INFO_MAP.containsKey(graphId)
                || !GRAPH_RUNNING_INFO_MAP.get(graphId).containsKey(graphTraceId)) {
            throw new PlatoException("removeGraphTrace error");
        }
        return GRAPH_RUNNING_INFO_MAP.get(graphId).remove(graphTraceId);
    }

    public static GraphRunningInfo putGraphRunningInfo(String graphId, String graphTraceId,
            GraphRunningInfo graphRunningInfo) {
        PlatoAssert.emptyException(() -> "putGraphRunningInfo param error", graphId, graphTraceId);
        PlatoAssert.nullException(() -> "putGraphRunningInfo graphRunningInfo error", graphRunningInfo);
        if (GRAPH_RUNNING_INFO_MAP.containsKey(graphId)) {
            return GRAPH_RUNNING_INFO_MAP.get(graphId).put(graphTraceId, graphRunningInfo);
        }
        ConcurrentHashMap<String, GraphRunningInfo> concurrentHashMap = new ConcurrentHashMap<>();
        concurrentHashMap.put(graphTraceId, graphRunningInfo);
        GRAPH_RUNNING_INFO_MAP.put(graphId, concurrentHashMap);
        return graphRunningInfo;
    }

    public static Map<String, NodeRunningInfo> getNodeRunningInfoMap(String graphId, String graphTraceId) {
        Map<String, NodeRunningInfo> nodeRunningInfoMap = new ConcurrentHashMap<>();
        GraphRunningInfo graphRunningInfo;
        PlatoAssert.emptyException(() -> "getNodeRunningInfoMap param error", graphId, graphTraceId);
        if (!GRAPH_RUNNING_INFO_MAP.containsKey(graphId)
                || (graphRunningInfo = GRAPH_RUNNING_INFO_MAP.get(graphId).get(graphTraceId)) == null) {
            return nodeRunningInfoMap;
        }
        return graphRunningInfo.getNodeRunningInfoMap();
    }

    public static NodeRunningInfo getNodeRunningInfo(String graphId, String graphTraceId, String uniqueId) {
        Map<String, NodeRunningInfo> nodeRunningInfoMap;
        PlatoAssert.emptyException(() -> "getNodeRunningInfo uniqueId error", uniqueId);
        PlatoAssert.emptyException(() -> "getNodeRunningInfo get result error",
                (nodeRunningInfoMap = getNodeRunningInfoMap(graphId, graphTraceId)));
        return nodeRunningInfoMap.get(uniqueId);
    }

    public static <R> NodeRunningInfo<R> putNodeRunningInfo(String graphId,
            String graphTraceId,
            String uniqueId,
            NodeRunningInfo<R> nodeRunningInfo) {
        GraphRunningInfo graphRunningInfo = GRAPH_RUNNING_INFO_MAP.get(graphId).get(graphTraceId);
        Map<String, NodeRunningInfo> nodeRunningInfoMap = graphRunningInfo.getNodeRunningInfoMap();
        nodeRunningInfoMap.put(uniqueId, nodeRunningInfo);
        return nodeRunningInfo;
    }*/
}
