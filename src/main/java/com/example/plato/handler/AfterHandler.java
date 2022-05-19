package com.example.plato.handler;

import java.util.HashSet;
import java.util.Set;

import com.example.plato.runningInfo.GraphRunningInfo;

/**
 * @author zhaodongpo
 * create  2022/5/16 10:28 上午
 * @version 1.0
 */
public interface AfterHandler {

    default <V> Set<String> notShouldRunNodes(GraphRunningInfo<V> graphRunningInfo) {
        return new HashSet<>();
    }

}
