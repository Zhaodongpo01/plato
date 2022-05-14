package com.example.plato.element;

import java.util.concurrent.RecursiveAction;

import com.example.plato.runningData.GraphRunningInfo;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * create 2022/3/14 10:57 上午
 */
@Slf4j
public class ForkJoinNodeAction extends RecursiveAction {

    private final PlatoNodeProxy platoNodeProxy;

    private final PlatoNodeProxy prePlatoNodeProxy;

    private final GraphRunningInfo graphRunningInfo;

    public ForkJoinNodeAction(PlatoNodeProxy platoNodeProxy, PlatoNodeProxy prePlatoNodeProxy,
            GraphRunningInfo graphRunningInfo) {
        this.platoNodeProxy = platoNodeProxy;
        this.prePlatoNodeProxy = prePlatoNodeProxy;
        this.graphRunningInfo = graphRunningInfo;
    }

    @Override
    protected void compute() {
        this.platoNodeProxy.run(prePlatoNodeProxy, graphRunningInfo);
    }
}
