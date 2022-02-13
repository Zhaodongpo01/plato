package com.example.plato.handler;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/13 3:34 下午
 */
public interface IWork<P, R> {

    /**
     * 回调方法
     */
    R work(P p) throws InterruptedException;

}
