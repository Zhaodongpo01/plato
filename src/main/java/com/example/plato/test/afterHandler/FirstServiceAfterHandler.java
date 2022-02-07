package com.example.plato.test.afterHandler;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.plato.handler.AfterHandler;
import com.example.plato.runningData.GraphRunningInfo;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/7 10:56 上午
 */
@Service
@Slf4j
public class FirstServiceAfterHandler {

    public AfterHandler afterHandler1() {
        log.info("FirstServiceAfterHandler#afterHandler1");
        return new AfterHandler() {
            @Override
            public Set<String> notShouldRunNodes(GraphRunningInfo graphRunningInfo) {
                return AfterHandler.super.notShouldRunNodes(graphRunningInfo);
            }
        };
    }
}
