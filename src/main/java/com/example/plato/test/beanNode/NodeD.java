package com.example.plato.test.beanNode;

import org.springframework.stereotype.Service;

import com.example.plato.handler.INodeWork;
import com.example.plato.runningData.ResultData;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/27 11:29 上午
 */
@Slf4j
@Service
public class NodeD implements INodeWork<Void, String> {

    @Override
    public String work(Void unused) {
        return "不需要参数单纯想返回值";
    }

    @Override
    public void hook(Void unused, ResultData<String> resultData) {
        log.info("NodeD#结果:{}", resultData);
    }
}
