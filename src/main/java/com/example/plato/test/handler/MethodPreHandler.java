package com.example.plato.test.handler;

import org.springframework.stereotype.Service;

import com.example.plato.handler.PreHandler;
import com.example.plato.runningData.GraphRunningInfo;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/3/21 10:27 上午
 */
@Service
public class MethodPreHandler {

    public PreHandler commonPreHandler() {

        return new PreHandler() {
            @Override
            public Object paramHandle(GraphRunningInfo graphRunningInfo) {
                return PreHandler.super.paramHandle(graphRunningInfo);
            }

            @Override
            public boolean suicide(GraphRunningInfo graphRunningInfo) {
                return PreHandler.super.suicide(graphRunningInfo);
            }
        };
    }

}
