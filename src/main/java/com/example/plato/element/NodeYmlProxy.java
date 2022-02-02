package com.example.plato.element;

import java.util.concurrent.ExecutorService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/2 2:19 下午
 */
@Slf4j
public class NodeYmlProxy extends AbstractNode {

    @Override
    void run(AbstractNode comingNode, ExecutorService executorService) {

    }

    @Override
    boolean run(AbstractNode comingNode) {
        return false;
    }

    @Override
    void runNext(ExecutorService executorService) {

    }
}
