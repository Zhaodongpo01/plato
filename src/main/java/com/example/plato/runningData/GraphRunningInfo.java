package com.example.plato.runningData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.plato.util.PlatoAssert;
import com.example.plato.util.TraceUtil;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/4/6 7:00 下午
 */
public class GraphRunningInfo<R> {

    private final String graphTraceId = TraceUtil.getRandomTraceId();

    private final Map<String, ResultData<R>> resultDataMap = new ConcurrentHashMap<>();

    public ResultData<R> putUniqueResultData(String uniqueId, ResultData<R> resultData) {
        return resultDataMap.put(uniqueId, resultData);
    }

    private final ConcurrentHashMap<String, NodeRunningInfo<R>> nodeRunningInfoMap = new ConcurrentHashMap<>();

    public ConcurrentHashMap<String, NodeRunningInfo<R>> getNodeRunningInfoMap() {
        return this.nodeRunningInfoMap;
    }

    public NodeRunningInfo<R> getNodeRunningInfo(String uniqueNodeId) {
        PlatoAssert.emptyException(() -> "getNodeRunningInfo uniqueNodeId is empty", uniqueNodeId);
        return nodeRunningInfoMap.get(uniqueNodeId);
    }

    public void putNodeRunningInfo(String uniqueId, NodeRunningInfo<R> nodeRunningInfo) {
        nodeRunningInfoMap.put(uniqueId, nodeRunningInfo);
    }
}
