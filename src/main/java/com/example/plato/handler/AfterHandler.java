package com.example.plato.handler;

import java.util.HashSet;
import java.util.Set;

import com.example.plato.runningData.GraphRunningInfo;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/4/1 12:23 上午
 */
public interface AfterHandler extends IHandler {
    default <R> Set<String> notShouldRunNodes(GraphRunningInfo<R> graphRunningInfo) {
        return new HashSet<>();
    }
}
