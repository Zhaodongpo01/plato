package com.example.plato.test.afterHandler;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/7 10:56 上午
 */
@Service
@Slf4j
public class FirstServiceAfterHandler {

    public void afterHandler1() {
        log.info("FirstServiceAfterHandler#afterHandler1");
    }


    public void afterHandler2() {
        log.info("FirstServiceAfterHandler#afterHandler2");
    }
}
