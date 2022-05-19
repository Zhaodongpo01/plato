package com.example.plato.element;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Sets;

/**
 * @author zhaodongpo
 * create  2022/5/16 10:19 上午
 * @version 1.0
 */
public class DefaultGraph<N, R> implements Graph<N, R> {

    private final Map<N, Set<DefaultEntry<N, R>>> FROM_MAP = new ConcurrentHashMap<>();
    private final Map<N, Set<DefaultEntry<N, R>>> TO_MAP = new ConcurrentHashMap<>();

    @Override
    public void putRelation(N fromNode, R relation, N toNode) {
        if (FROM_MAP.containsKey(fromNode)) {
            FROM_MAP.get(fromNode).add(new DefaultEntry<>(fromNode, toNode, relation));
        } else {
            FROM_MAP.put(fromNode, Sets.newHashSet(new DefaultEntry<>(fromNode, toNode, relation)));
        }
        if (TO_MAP.containsKey(toNode)) {
            TO_MAP.get(toNode).add(new DefaultEntry<>(fromNode, toNode, relation));
        } else {
            TO_MAP.put(toNode, Sets.newHashSet(new DefaultEntry<>(fromNode, toNode, relation)));
        }
    }

    @Override
    public Set<? extends Entry<N, R>> getRelationFrom(N from) {
        return FROM_MAP.get(from);
    }

    @Override
    public Set<? extends Entry<N, R>> getRelationTo(N to) {
        return TO_MAP.get(to);
    }

    public static class DefaultEntry<N, R> implements Entry<N, R> {

        private final N from;
        private final N to;
        private final R relation;

        public DefaultEntry(N from, N to, R relation) {
            this.from = from;
            this.to = to;
            this.relation = relation;
        }

        @Override
        public N getFrom() {
            return from;
        }

        @Override
        public N getTo() {
            return to;
        }

        @Override
        public R getRelation() {
            return relation;
        }
    }

}
