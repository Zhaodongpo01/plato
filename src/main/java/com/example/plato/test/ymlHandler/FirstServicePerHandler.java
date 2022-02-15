package com.example.plato.test.ymlHandler;

import org.springframework.stereotype.Service;

import com.example.plato.handler.PreHandler;
import com.example.plato.runningData.GraphRunningInfo;
import com.example.plato.runningData.NodeRunningInfo;
import com.example.plato.test.model.FirstModel;
import com.example.plato.test.model.TestModel;
import com.example.plato.util.PlatoJsonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FirstServicePerHandler {

    public PreHandler<FirstModel> perhandlerB() {
        return new PreHandler<FirstModel>() {
            @Override
            public FirstModel paramHandle(GraphRunningInfo graphRunningInfo) {
                NodeRunningInfo uniqAResult = graphRunningInfo.getNodeRunningInfo("uniqA");
                String uniqueAResult = String.valueOf(uniqAResult.getResultData().getData());
                FirstModel firstModel = new FirstModel();
                firstModel.setIdf(1000088L);
                firstModel.setName(uniqueAResult);
                return firstModel;
            }
        };
    }

    public PreHandler<TestModel> perhandlerC() {
        return new PreHandler<TestModel>() {
            @Override
            public TestModel paramHandle(GraphRunningInfo graphRunningInfo) {
                return null;
            }

            @Override
            public boolean suicide(GraphRunningInfo graphRunningInfo) {
                NodeRunningInfo uniqB = graphRunningInfo.getNodeRunningInfo("uniqB");
                TestModel testModel = (TestModel) uniqB.getResultData().getData();
                log.info("perhandlerC#uniqB的Result:{}", PlatoJsonUtil.toJson(testModel));
                return testModel.getAge() != 10;   //不等于10自杀
            }
        };
    }
}