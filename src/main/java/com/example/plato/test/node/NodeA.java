package com.example.plato.test.node;

import com.example.plato.handler.INodeWork;
import com.example.plato.runningData.ResultData;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/24 2:37 下午
 */
@Slf4j
public class NodeA implements INodeWork<String, Long> {

    @Override
    public Long work(String s) {
        log.info("NodeA参数:{}", s);
        return Long.valueOf(s.length());
    }

    @Override
    public void hook(String s, ResultData<Long> resultData) {
        log.info("NodeA结果参数:{},{}", s, resultData);
    }
}
