package com.example.plato.test.beanNode;

import org.springframework.stereotype.Service;

import com.example.plato.handler.INodeWork;
import com.example.plato.runningData.ResultData;
import com.example.plato.runningData.WorkResult;
import com.example.plato.test.model.FirstModel;
import com.example.plato.test.model.TestModel;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/24 2:37 下午
 */
@Slf4j
@Service
public class NodeC implements INodeWork<TestModel, FirstModel> {

    @Override
    public FirstModel work(TestModel testModel) throws InterruptedException {
        FirstModel firstModel = new FirstModel();
        firstModel.setName("赵东坡");
        firstModel.setIdf(19000L);
        log.info("NodeC");
        return firstModel;
    }

    @Override
    public void hook(TestModel testModel, WorkResult<FirstModel> resultData) {
        log.info("NodeC结果:{}");
    }
}
