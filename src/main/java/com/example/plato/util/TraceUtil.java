package com.example.plato.util;

import org.springframework.util.SimpleIdGenerator;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/31 18:01
 */
public class TraceUtil {

    public static String getRandomTraceId() {
        return new SimpleIdGenerator().generateId().toString();
    }

}
