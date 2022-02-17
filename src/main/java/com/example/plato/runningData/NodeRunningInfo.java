package com.example.plato.runningData;

import lombok.Getter;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/30 15:08
 */
@Getter
public class NodeRunningInfo<R> {

    private final String traceId;

    private final String graphId;

    private final String uniqueId;

    private final ResultData<R> resultData;

    public NodeRunningInfo(String traceId, String graphId, String uniqueId,
            ResultData<R> resultData) {
        this.traceId = traceId;
        this.graphId = graphId;
        this.uniqueId = uniqueId;
        this.resultData = resultData;
    }
}
