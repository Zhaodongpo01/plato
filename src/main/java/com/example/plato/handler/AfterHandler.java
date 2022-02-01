package com.example.plato.handler;

import com.example.plato.runningData.GraphRunningInfo;

import java.util.HashSet;
import java.util.Set;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/23 11:39 上午
 */
public interface AfterHandler {

    /**
     * 根据当前节点执行结果可以限制后面节点是否需要执行
     */
    default Set<String> notShouldRunNodes(GraphRunningInfo graphRunningInfo) {
        return new HashSet<>();
    }
}
