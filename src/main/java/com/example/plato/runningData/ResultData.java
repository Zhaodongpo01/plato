package com.example.plato.runningData;

import org.apache.commons.lang3.StringUtils;

import com.example.plato.platoEnum.NodeResultStatus;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/23 11:02 上午
 */
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

    public static <R> ResultData<R> defaultResult() {
        return new ResultData<>();
    }

    public R getData() {
        return data;
    }

    public void setData(R data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMes() {
        return mes;
    }

    public void setMes(String mes) {
        this.mes = mes;
    }

    public long getCostTime() {
        return costTime;
    }

    public void setCostTime(long costTime) {
        this.costTime = costTime;
    }

    public NodeResultStatus getNodeResultStatus() {
        return nodeResultStatus;
    }

    public void setNodeResultStatus(NodeResultStatus nodeResultStatus) {
        this.nodeResultStatus = nodeResultStatus;
    }
}
