package com.example.plato.element;

import com.example.plato.exception.PlatoException;
import com.example.plato.runningData.NodeResultStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/23 11:04 上午
 */
@Slf4j
public abstract class AbstractNodeProxy {

    public static final Long DEFAULT_TIME_OUT = 60_000L;

    private AtomicReference<NodeResultStatus> statusAtomicReference = new AtomicReference<>(NodeResultStatus.INIT);

    abstract void run(AbstractNodeProxy comingNode, ExecutorService executorService);

    abstract boolean run(AbstractNodeProxy comingNode);

    abstract void runNext(ExecutorService executorService);

    public boolean compareAndSetState(NodeResultStatus expect, NodeResultStatus update) {
        return this.statusAtomicReference.compareAndSet(expect, update);
    }

    protected void changeStatus(NodeResultStatus fromStatus, NodeResultStatus toStatus) {
        if (!compareAndSetState(fromStatus, toStatus)) {
            log.error("NodeResultStatus change status error");
            throw new PlatoException("NodeResultStatus change status error");
        }
    }
}
