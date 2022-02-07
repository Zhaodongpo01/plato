package com.example.plato.handler;

import com.example.plato.runningData.GraphRunningInfo;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/23 11:36 上午
 */

@FunctionalInterface
public interface PreHandler<R> {

    PreHandler VOID_PRE_HANDLER = graphRunningInfo -> null;

    R paramHandle(GraphRunningInfo graphRunningInfo);

    default boolean suicide(GraphRunningInfo graphRunningInfo) {
        return false;
    }

}
