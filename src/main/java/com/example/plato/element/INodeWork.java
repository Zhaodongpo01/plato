package com.example.plato.element;

import com.example.plato.runningData.ResultData;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/23 10:48 上午
 */
public interface INodeWork<P, R> {

    /**
     * 回调方法
     */
    R work(P p) throws InterruptedException;

    /**
     * 实现hook方法
     */
    void hook(P p, ResultData<R> resultData);

}

