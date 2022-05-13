package com.example.plato.element;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.ObjectUtils;

import com.example.plato.handler.AfterHandler;
import com.example.plato.handler.INodeWork;
import com.example.plato.handler.PreHandler;
import com.example.plato.util.PlatoAssert;

public class PlatoNodeBuilder<T, R> {

    private AfterHandler afterHandler;
    private PreHandler<T> preHandler;
    private String graphId;
    private String uniqueId;
    private INodeWork<T, R> worker;
    private final Set<PlatoNodeBuilder<?, ?>> nextProxies = new HashSet<>();
    private final Set<PlatoNodeBuilder<?, ?>> selfIsMustSet = new HashSet<>();
    private AtomicReference<PlatoNodeProxy<T, R>> proxyAtomicReference = new AtomicReference<>(null);
    private boolean checkNextResult = true;

    public PlatoNodeBuilder<T, R> setAfterHandler(AfterHandler afterHandler) {
        this.afterHandler = afterHandler;
        return this;
    }

    public PlatoNodeBuilder<T, R> setPreHandler(PreHandler<T> preHandler) {
        this.preHandler = preHandler;
        return this;
    }

    public PlatoNodeBuilder<T, R> setINodeWork(INodeWork<T, R> worker) {
        this.worker = worker;
        return this;
    }

    public PlatoNodeBuilder<T, R> setUniqueId(String uniqueId) {
        PlatoAssert.emptyException(() -> "setUniqueId uniqueId error", uniqueId);
        this.uniqueId = uniqueId;
        return this;
    }

    public PlatoNodeBuilder<T, R> setGraphId(String graphId) {
        PlatoAssert.emptyException(() -> "setGraphId graphId error", graphId);
        this.graphId = graphId;
        return this;
    }

    public PlatoNodeBuilder<T, R> checkNextResult(boolean checkNextResult) {
        this.checkNextResult = checkNextResult;
        return this;
    }

    public PlatoNodeBuilder<T, R> next(PlatoNodeBuilder<?, ?> proxy, boolean selfIsMust) {
        nextProxies.add(proxy);
        if (selfIsMust) {
            selfIsMustSet.add(proxy);
        }
        return this;
    }

    PlatoNodeProxy<T, R> build() {
        PlatoNodeProxy<T, R> proxy = new PlatoNodeProxy<>(uniqueId, worker, afterHandler, preHandler);
        if (!proxyAtomicReference.compareAndSet(null, proxy)) {
            return null;
        }
        proxy.setCheckNextResult(checkNextResult);
        convertBuild2Bean(proxy, this.nextProxies);
        return proxy;
    }

    private void convertBuild2Bean(PlatoNodeProxy<T, R> proxy,
            Set<PlatoNodeBuilder<?, ?>> nextProxies) {
        nextProxies.forEach(platoNodeBuilder -> {
            PlatoNodeProxy<?, ?> platoNodeProxy =
                    ObjectUtils.defaultIfNull(platoNodeBuilder.build(), platoNodeBuilder.proxyAtomicReference.get());
            platoNodeProxy.addPreProxy(proxy, selfIsMustSet.contains(platoNodeBuilder));
            proxy.addNextProxy(platoNodeProxy);
        });
    }
}