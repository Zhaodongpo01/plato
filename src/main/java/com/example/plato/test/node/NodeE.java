package com.example.plato.test.node;

import com.example.plato.handler.INodeWork;
import com.example.plato.runningData.ResultData;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/14 8:00 下午
 */
@Slf4j
public class NodeE implements INodeWork<Integer, Void> {

    @Override
    public Void work(Integer integer) throws InterruptedException {
        return null;
    }

    @Override
    public void hook(Integer integer, ResultData<Void> resultData) {

    }

}
