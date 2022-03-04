package com.example.plato.test.node;

import com.example.plato.handler.INodeWork;
import com.example.plato.runningData.ResultData;
import com.example.plato.test.model.TestModel;
import com.example.plato.util.PlatoJsonUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/24 7:08 下午
 */
@Slf4j
public class NodeC2 implements INodeWork<TestModel, Void> {

    @Override
    public void hook(TestModel testModel, ResultData<Void> resultData) {
        log.info("NodeC2#hook");
    }

    @Override
    public Void work(TestModel testModel) throws InterruptedException {
        log.info("NodeC2#TestModel:{}", PlatoJsonUtil.toJson(testModel));
        return null;
    }
}
