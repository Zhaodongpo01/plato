package com.example.plato.test.node.yml;

import java.util.List;

import com.example.plato.test.model.FirstModel;

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

    public String ymlTest1(List<Integer> integerList) {
        log.info("ymlTest1#integerList:{}", integerList);
        return "执行第一个MethodType的Node";
    }
}
