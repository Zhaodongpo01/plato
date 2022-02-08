package com.example.plato.runningData;

import com.example.plato.platoEnum.NodeResultStatus;

import lombok.Data;

import org.apache.commons.lang3.StringUtils;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/23 11:02 上午
 */
@Data
public class ResultData<R> {

    private R data = null;

    private boolean success = false;

    private String mes = StringUtils.EMPTY;

    private long costTime = 0L;

    private NodeResultStatus nodeResultStatus = NodeResultStatus.INIT;

    public static <R> ResultData<R> build(R result, NodeResultStatus resultStatus, String mes, long costTime) {
        ResultData<R> resultData = new ResultData<>();
        resultData.setData(result);
        resultData.setNodeResultStatus(resultStatus);
        resultData.setMes(mes);
        resultData.setCostTime(costTime);
        resultData.setSuccess(!NodeResultStatus.ERROR.equals(resultStatus));
        return resultData;
    }

    public static ResultData getFail(String mes, NodeResultStatus nodeResultStatus) {
        ResultData<Object> objectResultData = new ResultData<>();
        objectResultData.setNodeResultStatus(nodeResultStatus);
        objectResultData.setMes(mes);
        objectResultData.setSuccess(false);
        return objectResultData;
    }
}
