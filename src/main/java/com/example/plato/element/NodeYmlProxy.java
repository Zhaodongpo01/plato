package com.example.plato.element;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import com.example.plato.exception.PlatoException;
import com.example.plato.loader.ymlNode.AbstractYmlNode;
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
public class NodeYmlProxy<P, R> extends AbstractNodeProxy {

    private String traceId;
    private String graphTraceId;
    private AbstractYmlNode<P, R> abstractYmlNode;
    private P p;
    private AtomicReference<NodeResultStatus> statusAtomicReference = new AtomicReference<>(NodeResultStatus.INIT);

    private void setStatusAtomicReference() {
        throw new PlatoException("private 禁止调用");
    }

    public boolean compareAndSetState(NodeResultStatus expect, NodeResultStatus update) {
        return this.statusAtomicReference.compareAndSet(expect, update);
    }

    public NodeYmlProxy(AbstractYmlNode<P, R> abstractYmlNode, String graphTraceId, P p) {
        this.abstractYmlNode = abstractYmlNode;
        this.graphTraceId = graphTraceId;
        this.p = p;
    }

    @Override
    void run(AbstractNodeProxy comingNode, ExecutorService executorService) {
        traceId = TraceUtil.getRandomTraceId();
        if (run(comingNode)) {
            runNext(executorService);
        }
    }

    @Override
    boolean run(AbstractNodeProxy comingNode) {
        p = Objects.isNull(comingNode) ? p : paramHandle((NodeYmlProxy<?, ?>) comingNode);
        try {
            R work = abstractYmlNode.work(p);
        } catch (InterruptedException e) {
            log.error("NodeYmlProxy run error", e);
            return false;
        }
        return true;
    }

    private P paramHandle(NodeYmlProxy<?, ?> comingNode) {
        AbstractYmlNode<?, ?> abstractYmlNode = comingNode.getAbstractYmlNode();

        return null;
    }

    @Override
    void runNext(ExecutorService executorService) {

    }

}
