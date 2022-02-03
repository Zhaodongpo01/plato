package com.example.plato.element;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import com.example.plato.exception.PlatoException;
import com.example.plato.runningData.NodeResultStatus;
import com.example.plato.util.TraceUtil;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/2 2:19 下午
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class NodeYmlProxy<P, R> extends AbstractNode {

    private String traceId;
    private String graphTraceId;
    private NodeLoadByYml<P, R> nodeLoadByYml;
    private AtomicReference<NodeResultStatus> statusAtomicReference = new AtomicReference<>(NodeResultStatus.INIT);

    private void setStatusAtomicReference() {
        throw new PlatoException("private 禁止调用");
    }

    public boolean compareAndSetState(NodeResultStatus expect, NodeResultStatus update) {
        return this.statusAtomicReference.compareAndSet(expect, update);
    }

    @Override
    void run(AbstractNode comingNode, ExecutorService executorService) {
        traceId = TraceUtil.getRandomTraceId();
        if (run(comingNode)) {
            runNext(executorService);
        }
    }

    @Override
    boolean run(AbstractNode comingNode) {
        return false;
    }

    @Override
    void runNext(ExecutorService executorService) {

    }
}
