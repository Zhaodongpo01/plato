package com.example.plato.element;

import com.example.plato.runningInfo.ResultData;

/**
 * @author zhaodongpo
 * create  2022/5/15 10:14 下午
 * @version 1.0
 */
public interface INodeWork<P, V> {

    /**
     * 回调方法
     */
    V work(P p);

    /**
     * 实现hook方法
     */
    void hook(P p, ResultData<V> resultData);
}
