package com.example.plato.test.node.yml;

import org.springframework.stereotype.Service;

import com.example.plato.handler.IWork;
import com.example.plato.test.model.TestModel;
import com.example.plato.util.PlatoJsonUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/15 11:20 上午
 */
@Service
@Slf4j
public class NodeCBean implements IWork<TestModel, String> {

    @Override
    public String work(TestModel testModel) throws InterruptedException {
        log.info("NodeCBean#work:{}", PlatoJsonUtil.toJson(testModel));
        return PlatoJsonUtil.toJson(testModel);
    }
}
