package com.example.plato.test.node;

import com.example.plato.handler.INodeWork;
import com.example.plato.runningData.ResultData;
import com.example.plato.test.model.TestModel;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/24 7:08 下午
 */
@Slf4j
public class NodeB2 implements INodeWork<Integer, TestModel> {

    @Override
    public void hook(Integer integer, ResultData<TestModel> resultData) {
        log.info("NodeB2#hook#参数");
    }

    @Override
    public TestModel work(Integer integer) throws InterruptedException {
        TestModel testModel = new TestModel();
        testModel.setId(Long.valueOf(integer));
        testModel.setUsername("NodeB2名字");
        testModel.setAge(10);
        return testModel;
    }
}
