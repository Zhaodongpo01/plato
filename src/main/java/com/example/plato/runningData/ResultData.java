package com.example.plato.runningData;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/3/31 11:25 下午
 */
public class ResultData<R> {

    private String uniqueId;
    private R result;
    private ResultState resultState;
    private Exception ex;

    public ResultData(String uniqueId, R result, ResultState resultState) {
        this(uniqueId, result, resultState, null);
    }

    public ResultData(String uniqueId, R result, ResultState resultState, Exception ex) {
        this.uniqueId = uniqueId;
        this.result = result;
        this.resultState = resultState;
        this.ex = ex;
    }

    public boolean checkIsNullResult() {
        return ResultState.DEFAULT == this.getResultState();
    }

    public static <R> ResultData<R> defaultResult(String uniqueId) {
        return new ResultData<>(uniqueId, null, ResultState.DEFAULT);
    }

    public ResultData<R> defaultResult() {
        this.setResultState(ResultState.TIMEOUT);
        this.setResult(null);
        return this;
    }

    public ResultData<R> defaultExResult(Exception ex) {
        this.setResultState(ResultState.EXCEPTION);
        this.setResult(null);
        this.setEx(ex);
        return this;
    }


    @Override
    public String toString() {
        return "ResultData{" +
                "uniqueId='" + uniqueId + '\'' +
                ", result=" + result +
                ", resultState=" + resultState +
                ", ex=" + ex +
                '}';
    }

    public Exception getEx() {
        return ex;
    }

    public void setEx(Exception ex) {
        this.ex = ex;
    }

    public R getResult() {
        return result;
    }

    public void setResult(R result) {
        this.result = result;
    }

    public ResultState getResultState() {
        return resultState;
    }

    public void setResultState(ResultState resultState) {
        this.resultState = resultState;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
}
