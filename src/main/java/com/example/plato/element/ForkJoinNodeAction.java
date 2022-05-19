package com.example.plato.element;

import java.util.concurrent.RecursiveAction;

import com.example.plato.runningInfo.GraphRunningInfo;

/**
 * @author zhaodongpo
 * @version 1.0
 * create 2022/3/14 10:57 上午
 */
public class ForkJoinNodeAction extends RecursiveAction {

    private final AbstractNodeProxy platoNodeProxy;

    private final AbstractNodeProxy prePlatoNodeProxy;

    private final GraphRunningInfo graphRunningInfo;

    private final long remainTime;

    public ForkJoinNodeAction(AbstractNodeProxy platoNodeProxy, AbstractNodeProxy prePlatoNodeProxy,
            GraphRunningInfo graphRunningInfo, long remainTime) {
        this.platoNodeProxy = platoNodeProxy;
        this.prePlatoNodeProxy = prePlatoNodeProxy;
        this.graphRunningInfo = graphRunningInfo;
        this.remainTime = remainTime;
    }

    @Override
    protected void compute() {
        this.platoNodeProxy.run(null, prePlatoNodeProxy, remainTime, graphRunningInfo);
    }
}