package com.example.plato.test.afterHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/3 3:06 下午
 */
@Slf4j
public class DemoAfterHandler {

    public void afterHandler() {
        log.info("hello afterHandler");
    }

}
