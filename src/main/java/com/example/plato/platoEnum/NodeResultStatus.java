package com.example.plato.platoEnum;

import java.util.Set;

import com.google.common.collect.Sets;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/7 5:25 下午
 */
public enum NodeResultStatus {
    DEFAULT,
    INIT,
    LIMIT_RUN,
    EXECUTING,
    EXECUTED,
    TIMEOUT,
    ERROR,
    ;

    public static Set<NodeResultStatus> getAbnormalStatus() {
        return Sets.newHashSet(LIMIT_RUN, ERROR);
    }

}
