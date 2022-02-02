package com.example.plato.test.serial;

import com.example.plato.element.INodeWork;
import com.example.plato.runningData.ResultData;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/24 2:37 下午
 */
@Slf4j
public class NodeB implements INodeWork<List<Integer>, Boolean> {

    @Override
    public Boolean work(List<Integer> integers) {
        int a = 10 / 0;
        log.info("NodeB参数:{}", integers);
        return CollectionUtils.isEmpty(integers);
    }

    @Override
    public void hook(List<Integer> integers, ResultData<Boolean> resultData) {
        log.info("NodeB结果参数:{},{}", integers, resultData);
    }
}