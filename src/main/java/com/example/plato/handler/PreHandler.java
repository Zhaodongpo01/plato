package com.example.plato.handler;

import com.example.plato.runningData.GraphRunningInfo;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/23 11:36 上午
 */

public interface PreHandler<P> {

    PreHandler DEFAULT_PRE_HANDLER = new PreHandler() {
        @Override
        public Object paramHandle(GraphRunningInfo graphRunningInfo) {
            return PreHandler.super.paramHandle(graphRunningInfo);
        }

        @Override
        public boolean suicide(GraphRunningInfo graphRunningInfo) {
            return PreHandler.super.suicide(graphRunningInfo);
        }
    };

    default P paramHandle(GraphRunningInfo graphRunningInfo) {
        return null;
    }

    default boolean suicide(GraphRunningInfo graphRunningInfo) {
        return false;
    }

}
