package com.example.plato.test.handler;

import org.springframework.stereotype.Service;

import com.example.plato.handler.PreHandler;
import com.example.plato.runningData.GraphRunningInfo;
import com.example.plato.runningData.ResultData;
import com.example.plato.test.model.TestModel;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/3/30 10:27 上午
 */
@Service
public class PreHandlerC implements PreHandler<TestModel> {

    @Override
    public TestModel paramHandle(GraphRunningInfo graphRunningInfo) {

        ResultData nodeAResult = graphRunningInfo.getResultData("nodeA");
        TestModel testModel = new TestModel();
        testModel.setId((Long) nodeAResult.getResult());
        testModel.setUsername("PrehandlerC");
        testModel.setAge(10);
        return testModel;
    }

    @Override
    public boolean suicide(GraphRunningInfo graphRunningInfo) {
        return PreHandler.super.suicide(graphRunningInfo);
    }
}
