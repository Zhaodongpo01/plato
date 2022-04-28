package com.example.plato.test.beanNode;

import org.springframework.stereotype.Service;

import com.example.plato.handler.INodeWork;
import com.example.plato.runningData.ResultData;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/14 8:00 下午
 */
@Slf4j
@Service
public class NodeE implements INodeWork<Integer, Void> {

    @Override
    public Void work(Integer integer) throws InterruptedException {
        Thread.sleep(100L);
        return null;
    }

    @Override
    public void hook(Integer integer, ResultData resultData) {
        log.info("NodeE结果:{}", resultData.getResult());
    }

}
