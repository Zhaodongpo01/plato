package com.example.plato.runningData;

import org.apache.commons.lang3.StringUtils;

import com.example.plato.platoEnum.NodeResultStatus;

import lombok.Data;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/23 11:02 上午
 */
@Data
public class ResultData<R> {

    private R data;

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

    public static <R> ResultData<R> getFail(String mes) {
        ResultData<R> objectResultData = new ResultData<>();
        objectResultData.setNodeResultStatus(NodeResultStatus.ERROR);
        objectResultData.setMes(mes);
        objectResultData.setSuccess(false);
        return objectResultData;
    }
}
