package com.example.plato.element;

import java.util.Set;

/**
 * @author zhaodongpo
 * create  2022/5/15 10:31 下午
 * @version 1.0
 */
public interface Graph<N, R> {

    void putRelation(N fromNode, R relation, N toNode);

    Set<? extends Entry<N, R>> getRelationFrom(N from);

    Set<? extends Entry<N, R>> getRelationTo(N to);

    interface Entry<N, R> {
        N getFrom();

        N getTo();

        R getRelation();
    }
}
