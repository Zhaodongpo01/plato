package com.example.plato.runningData;

import com.example.plato.util.TraceUtil;


/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/3/18 3:12 下午
 */
public class NodeRunningInfo<R> {

    private final String traceId = TraceUtil.getRandomTraceId();

    private final String graphId;

    private final String uniqueId;

    private ResultData<R> resultData;

    public NodeRunningInfo(String graphId, String uniqueId) {
        this.graphId = graphId;
        this.uniqueId = uniqueId;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getGraphId() {
        return graphId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public ResultData<R> getResultData() {
        return resultData;
    }

    public void setResultData(ResultData<R> resultData) {
        this.resultData = resultData;
    }
}
