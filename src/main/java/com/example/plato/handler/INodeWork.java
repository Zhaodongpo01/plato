package com.example.plato.handler;

import com.example.plato.runningData.ResultData;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/23 10:48 上午
 */
public interface INodeWork<P, R> extends IWork<P, R> {

    /**
     * 实现hook方法
     */
    void hook(P p, ResultData<R> resultData);

}

