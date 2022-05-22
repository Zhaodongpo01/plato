package com.example.plato.test.beanNode;

import org.springframework.stereotype.Service;

import com.example.plato.element.INodeWork;
import com.example.plato.runningInfo.ResultData;
import com.example.plato.test.model.FirstModel;
import com.example.plato.test.model.TestModel;
import com.example.plato.util.PlatoJsonUtil;

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
    public FirstModel work(TestModel testModel) {
        FirstModel firstModel = new FirstModel();
        firstModel.setName("赵东坡");
        firstModel.setIdf(19000L);
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return firstModel;
    }

    @Override
    public void hook(TestModel testModel, ResultData<FirstModel> resultData) {
        log.info("NodeC#结果");
    }
}
