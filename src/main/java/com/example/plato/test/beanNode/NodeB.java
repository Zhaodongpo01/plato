package com.example.plato.test.beanNode;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import com.example.plato.element.INodeWork;
import com.example.plato.runningInfo.ResultData;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/24 2:37 下午
 */
@Slf4j
@Service
public class NodeB implements INodeWork<List<Integer>, Boolean> {

    @Override
    public Boolean work(List<Integer> integers) {
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return CollectionUtils.isEmpty(integers);
    }

    @Override
    public void hook(List<Integer> integers, ResultData<Boolean> resultData) {
        log.info("NodeB#结果");
    }
}