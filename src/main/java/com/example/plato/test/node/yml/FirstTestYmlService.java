package com.example.plato.test.node.yml;

import com.example.plato.test.model.FirstModel;
import com.example.plato.test.model.TestModel;
import com.example.plato.util.PlatoJsonUtil;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/6 18:52
 */
@Service
@Slf4j
public class FirstTestYmlService {

    public String uniqAMethod(Integer[] args) {
        log.info("ymlTest1#integerList:{}", args);
        return "执行第一个MethodType的Node";
    }

    public TestModel uniqBMethod(FirstModel firstModel) {
        log.info("FirstTestYmlService#uniqBMethod:{}", PlatoJsonUtil.toJson(firstModel));
        TestModel testModel = new TestModel();
        testModel.setAge(10);
        testModel.setUsername(firstModel.getName());
        testModel.setId(firstModel.getIdf());
        return testModel;
    }
}
