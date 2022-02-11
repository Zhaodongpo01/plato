package com.example.plato.test.serial.second;

import com.example.plato.handler.INodeWork;
import com.example.plato.runningData.ResultData;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/8 4:45 下午
 */
@Slf4j
public class Nb implements INodeWork<Integer, Boolean> {

    @Override
    public Boolean work(Integer integer) throws InterruptedException {
        log.info("Nb#work:{}", integer);
        return integer > 10;
    }

    @Override
    public void hook(Integer integer, ResultData<Boolean> resultData) {
        log.info("Nb#hook:{},{}", integer, resultData.getData());
    }
}
