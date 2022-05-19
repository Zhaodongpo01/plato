package com.example.plato.runningInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * @author zhaodongpo
 * create  2022/5/15 10:32 下午
 * @version 1.0
 */
public class GraphRunningInfo<V> {

    private final long groupStartTime;

    private volatile long graphEndTime = -1L;

    private final long graphTimeLimit;

    private final String graphTraceId;

    private final String graphId;

    private final CountDownLatch endCDL = new CountDownLatch(1);

    /**
     * uniqueId:resultData<R>
     */
    private Map<String, ResultData<V>> resultDataMap = new ConcurrentHashMap<>();

    public GraphRunningInfo(String graphTraceId, String graphId, long groupStartTime, long graphTimeLimit) {
        this.groupStartTime = groupStartTime;
        this.graphTimeLimit = graphTimeLimit;
        this.graphId = graphId;
        this.graphTraceId = graphTraceId;
    }

    public ResultData putResult(ResultData<V> resultData) {
        resultDataMap.put(resultData.getUniqueId(), resultData);
        return resultData;
    }

    public CountDownLatch getEndCDL() {
        return endCDL;
    }

    public Map<String, ResultData<V>> getResultDataMap() {
        return resultDataMap;
    }

    public ResultData<V> getResultData(String uniqueId) {
        return resultDataMap.get(uniqueId);
    }
}
