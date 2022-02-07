package com.example.plato.element;

import java.util.concurrent.ExecutorService;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/7 11:06 上午
 */
public interface INodeProxy {

    void run(AbstractNodeProxy comingNode, ExecutorService executorService);

    boolean run(AbstractNodeProxy comingNode);

    void runNext(ExecutorService executorService);

}
