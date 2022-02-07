package com.example.plato.platoEnum;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/7 5:25 下午
 */
public enum NodeResultStatus {
    INIT,
    LIMIT_RUN,
    EXECUTING,
    EXECUTED,
    ERROR,
    ;

    public static Set<NodeResultStatus> getAbnormalStatus() {
        return Sets.newHashSet(LIMIT_RUN, ERROR);
    }

}
