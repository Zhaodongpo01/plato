package com.example.plato.test.serial.second;

import java.util.List;

import com.example.plato.handler.INodeWork;
import com.example.plato.runningData.ResultData;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/8 4:45 下午
 */
@Slf4j
public class Na implements INodeWork<String, List<String>> {

    @Override
    public List<String> work(String s) throws InterruptedException {
        log.info("Na#work:{}", s);
        return Lists.newArrayList("1", "23", "3244");
    }

    @Override
    public void hook(String s, ResultData<List<String>> resultData) {
        log.info("Na#hook:{}", s, resultData.getData());
    }
}
