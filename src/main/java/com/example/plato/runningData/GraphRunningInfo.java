package com.example.plato.runningData;

import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.plato.util.PlatoAssert;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/30 14:56
 */
@Getter
public class GraphRunningInfo<R> {

    /**
     * <uniqueId,NodeRunningInfo>
     */
    private Map<String, NodeRunningInfo<R>> nodeRunningInfoMap = new ConcurrentHashMap<>();

    public GraphRunningInfo() {
    }

    public NodeRunningInfo<R> getNodeRunningInfo(String uniqueId) {
        PlatoAssert.emptyException(() -> "getNodeRunningInfo uniqueId is empty", uniqueId);
        return nodeRunningInfoMap.get(uniqueId);
    }

    public <R> NodeRunningInfo<R> putNodeRunningInfo(String uniqueId, NodeRunningInfo nodeRunningInfo) {
        return nodeRunningInfoMap.put(uniqueId, nodeRunningInfo);
    }

}
