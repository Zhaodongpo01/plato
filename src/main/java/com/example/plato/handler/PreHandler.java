package com.example.plato.handler;

import com.example.plato.runningData.GraphRunningInfo;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/23 11:36 上午
 */

@FunctionalInterface
public interface PreHandler<P> {

    PreHandler VOID_PRE_HANDLER = graphRunningInfo -> null;

    P paramHandle(GraphRunningInfo graphRunningInfo);

    /**
     * 通过前面的节点结果。判断当前节点是否要执行
     */
    default boolean runEnable(GraphRunningInfo graphRunningInfo) {
        return true;
    }

}
