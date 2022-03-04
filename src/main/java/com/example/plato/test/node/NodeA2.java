package com.example.plato.test.node;

import com.example.plato.handler.INodeWork;
import com.example.plato.runningData.ResultData;
import com.example.plato.test.model.FirstModel;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/24 7:10 下午
 */
@Slf4j
public class NodeA2 implements INodeWork<String, FirstModel> {

    @Override
    public void hook(String s, ResultData<FirstModel> resultData) {
        log.info("NodeA2#hook");
    }

    @Override
    public FirstModel work(String s) throws InterruptedException {
        log.info("NodeA2请求参数#s:{}", s);
        FirstModel firstModel = new FirstModel();
        firstModel.setName(s);
        firstModel.setIdf(100L);
        return firstModel;
    }
}
