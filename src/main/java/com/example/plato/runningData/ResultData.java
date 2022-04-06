package com.example.plato.runningData;

/**
 * 执行结果
 */
public class ResultData<R> {
    /**
     * 执行的结果
     */
    private R result;
    /**
     * 结果状态
     */
    private ResultState resultState;
    private Exception ex;

    public ResultData(R result, ResultState resultState) {
        this(result, resultState, null);
    }

    public ResultData(R result, ResultState resultState, Exception ex) {
        this.result = result;
        this.resultState = resultState;
        this.ex = ex;
    }

    public static <R> ResultData<R> defaultResult() {
        return new ResultData<>(null, ResultState.DEFAULT);
    }

    @Override
    public String toString() {
        return "WorkResult{" +
                "result=" + result +
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
}
