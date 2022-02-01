package com.example.plato.element;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/23 11:04 上午
 */
@Slf4j
public abstract class AbstractNode<P, R> {

    public static final Long DEFAULT_TIME_OUT = 60_000L;

    abstract void run(AbstractNode comingNode, ExecutorService executorService);

    abstract boolean run(AbstractNode comingNode);

    abstract void runNext(ExecutorService executorService);

}
