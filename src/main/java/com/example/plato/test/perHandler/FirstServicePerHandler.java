package com.example.plato.test.perHandler;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/7 10:54 上午
 */
@Slf4j
@Service
public class FirstServicePerHandler {

    public void perhandler1() {
        log.info("FirstServicePerHandler#perhandler1");
    }

    public void perhandler2() {
        log.info("FirstServicePerHandler#perhandler2");
    }
}
