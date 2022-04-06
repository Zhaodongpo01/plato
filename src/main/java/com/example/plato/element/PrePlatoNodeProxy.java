package com.example.plato.element;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/3/31 11:17 下午
 */
public class PrePlatoNodeProxy {
    private PlatoNodeProxy<?, ?> platoNodeProxy;
    private boolean must;

    public PrePlatoNodeProxy(PlatoNodeProxy<?, ?> platoNodeProxy, boolean must) {
        this.platoNodeProxy = platoNodeProxy;
        this.must = must;
    }

    public PlatoNodeProxy<?, ?> getWorkerProxy() {
        return platoNodeProxy;
    }

    public void setWorkerProxy(PlatoNodeProxy<?, ?> platoNodeProxy) {
        this.platoNodeProxy = platoNodeProxy;
    }

    public boolean isMust() {
        return must;
    }

    public void setMust(boolean must) {
        this.must = must;
    }
}
