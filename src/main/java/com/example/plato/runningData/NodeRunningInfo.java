package com.example.plato.runningData;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.StringUtils;

import com.example.plato.platoEnum.CurrentState;
import com.example.plato.util.TraceUtil;

import lombok.Getter;


/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/3/18 3:12 下午
 */
@Getter
public class NodeRunningInfo<P, R> {

    private P p;

    private final String traceId = TraceUtil.getRandomTraceId();

    private final String graphId;

    private final String uniqueId;

    private final AtomicReference<CurrentState> CUR_STATUS = new AtomicReference<>(CurrentState.INIT);

    private ResultData<R> resultData = ResultData.getFail(StringUtils.EMPTY);

    private boolean compareAndSetState(CurrentState expect, CurrentState update) {
        return this.CUR_STATUS.compareAndSet(expect, update);
    }

    ResultData<R> getResultData() {
        return resultData;
    }

    public NodeRunningInfo(String graphId, String uniqueId) {
        this.graphId = graphId;
        this.uniqueId = uniqueId;
    }

}
