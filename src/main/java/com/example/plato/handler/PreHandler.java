package com.example.plato.handler;

import com.example.plato.runningInfo.GraphRunningInfo;

/**
 * @author zhaodongpo
 * create  2022/5/16 10:28 上午
 * @version 1.0
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
