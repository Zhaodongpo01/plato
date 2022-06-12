package com.example.plato.runningInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhaodongpo
 * create  2022/5/15 10:32 下午
 * @version 1.0
 */
public class GraphRunningInfo<V> {

    private final long groupStartTime;

    private volatile long graphEndTime = -1L;

    private final String graphTraceId;

    private final String graphId;

    /**
     * uniqueId:resultData<R>
     */
    private Map<String, ResultData<V>> resultDataMap = new ConcurrentHashMap<>();

    public GraphRunningInfo(String graphTraceId, String graphId, long groupStartTime) {
        this.groupStartTime = groupStartTime;
        this.graphId = graphId;
        this.graphTraceId = graphTraceId;
    }

    public ResultData putResult(ResultData<V> resultData) {
        resultDataMap.put(resultData.getUniqueId(), resultData);
        return resultData;
    }

    public Map<String, ResultData<V>> getResultDataMap() {
        return resultDataMap;
    }

    public ResultData<V> getResultData(String uniqueId) {
        return resultDataMap.get(uniqueId);
    }
}
