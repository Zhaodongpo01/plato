package com.example.plato.runningInfo;

/**
 * @author zhaodongpo
 * create  2022/5/15 10:16 下午
 * @version 1.0
 */
public class ResultData<V> {

    private String uniqueId;

    private V result;

    private ResultState resultState;

    private Exception ex;


    public String getUniqueId() {
        return uniqueId;
    }

    public V getResult() {
        return result;
    }

    public ResultState getResultState() {
        return resultState;
    }

    public Exception getEx() {
        return ex;
    }

    public static ResultData defaultResultData(String uniqueId) {
        return new ResultData(uniqueId, ResultState.DEFAULT, null);
    }

    public ResultData(String uniqueId, ResultState resultState, V result) {
        this(uniqueId, result, resultState, null);
    }

    public ResultData(String uniqueId, V result, ResultState resultState, Exception ex) {
        this.uniqueId = uniqueId;
        this.result = result;
        this.resultState = resultState;
        this.ex = ex;
    }

    public enum ResultState {
        SUCCESS,
        TIMEOUT,
        EXCEPTION,
        DEFAULT  //默认状态
    }

}
