package com.example.plato.element;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/3/31 11:17 下午
 */
public class PrePlatoNodeProxy {
    private WorkerWrapper<?, ?> workerWrapper;
    private boolean must;

    public PrePlatoNodeProxy(WorkerWrapper<?, ?> workerWrapper, boolean must) {
        this.workerWrapper = workerWrapper;
        this.must = must;
    }

    public WorkerWrapper<?, ?> getWorkerWrapper() {
        return workerWrapper;
    }

    public void setWorkerWrapper(WorkerWrapper<?, ?> workerWrapper) {
        this.workerWrapper = workerWrapper;
    }

    public boolean isMust() {
        return must;
    }

    public void setMust(boolean must) {
        this.must = must;
    }
}
