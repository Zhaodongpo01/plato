package com.example.plato.element;

import static com.example.plato.element.AbstractNodeWork.State.BUILDING;
import static com.example.plato.element.AbstractNodeWork.State.INIT;
import static com.example.plato.element.AbstractNodeWork.State.SUCCESS;
import static com.example.plato.element.AbstractNodeWork.State.WORKING;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;

import com.example.plato.exception.PlatoException;
import com.example.plato.handler.AfterHandler;
import com.example.plato.handler.PreHandler;
import com.example.plato.platoEnum.RelationEnum;
import com.example.plato.runningInfo.GraphRunningInfo;
import com.example.plato.runningInfo.ResultData;
import com.example.plato.runningInfo.ResultData.ResultState;

/**
 * @author zhaodongpo
 * create  2022/5/19 8:12 下午
 * @version 1.0
 */
public abstract class AbstractNodeWork<P, V> {

    private final PlatoNode<P, V> platoNode;
    protected String traceId;
    protected volatile P param;
    protected PreHandler<P> preHandler;
    protected AfterHandler afterHandler;
    protected final AtomicInteger state = new AtomicInteger(BUILDING.id);
    private final AtomicReference<Thread> doWorkingThread = new AtomicReference<>();
    private final AtomicReference<ResultData<V>> resultDataReference = new AtomicReference(null);

    AbstractNodeWork(INodeWork<P, V> iNodeWork, String uniqueId, String graphId, long timeLimit) {
        platoNode = PlatoNode.getInstance(uniqueId, graphId, iNodeWork, timeLimit);
        //resultDataReference.compareAndSet(null, ResultData.defaultResultData(platoNode.getUniqueId()));
    }

    private void normalRun(ExecutorService executorService, GraphRunningInfo<V> graphRunningInfo,
            AbstractNodeWork<?, ?> comingNode) {
        if (executor(comingNode, graphRunningInfo)) {
            graphRunningInfo.putResult(resultDataReference.get());
            runNext(executorService, graphRunningInfo);
        }
    }

    public void run(ExecutorService executorService, AbstractNodeWork<?, ?> comingNode,
            GraphRunningInfo<V> graphRunningInfo) {
        if (State.inStates(state, State.states_of_finish)) {
            runNext(executorService, graphRunningInfo);
            return;
        }
        Set<AbstractNodeWork<?, ?>> preNodeProxies = getPreNodeProxies();
        if (CollectionUtils.isNotEmpty(preNodeProxies)) {
            if (preNodeProxies.size() == 1 && runPreProxy(comingNode)) {
                normalRun(executorService, graphRunningInfo, comingNode);
            } else if (preNodeProxies.size() > 1 && runPreProxies(comingNode)) {
                normalRun(executorService, graphRunningInfo, comingNode);
            }
            return;
        }
        normalRun(executorService, graphRunningInfo, comingNode);
    }

    private void runNext(ExecutorService executorService, GraphRunningInfo graphRunningInfo) {
        Set<AbstractNodeWork<?, ?>> nextNodeProxies = getNextNodeProxies();
        if (CollectionUtils.isEmpty(nextNodeProxies)) {
            return;
        }
        if (nextNodeProxies.size() == 1) {
            nextNodeProxies.stream().findFirst().get().run(executorService, this, graphRunningInfo);
        } else {
            if (executorService == null) {
                List<ForkJoinNodeAction> forkJoinNodeActions = nextNodeProxies.stream().map(abstractNodeWork ->
                                new ForkJoinNodeAction(abstractNodeWork, this, graphRunningInfo, 1000L))
                        .collect(Collectors.toList());
                ForkJoinTask.invokeAll(forkJoinNodeActions);
            } else {
                Stream<CompletableFuture<Void>> completableFutureStream =
                        nextNodeProxies.stream().map(platoNodeProxy -> CompletableFuture.runAsync(
                                () -> platoNodeProxy.run(executorService, this, graphRunningInfo),
                                executorService));
                List<CompletableFuture<Void>> completableFutureList =
                        completableFutureStream.collect(Collectors.toList());
                try {
                    CompletableFuture.allOf(completableFutureList.toArray(new CompletableFuture[] {}))
                            .get(6000_0, TimeUnit.MILLISECONDS);
                } catch (InterruptedException | TimeoutException | ExecutionException e) {
                    throw new PlatoException("runNext异常");
                }
            }
        }
    }

    protected boolean fastFail(State expect, Exception e) {
        stopThread();
        if (!State.setState(state, expect, State.ERROR)) {
            return false;
        }
        ResultState resultState = ResultState.DEFAULT;
        if (e != null) {
            if (e instanceof TimeoutException) {
                resultState = ResultState.TIMEOUT;
            } else {
                resultState = ResultState.EXCEPTION;
            }
        }
        resultDataReference.compareAndSet(null, new ResultData(platoNode.getUniqueId(), resultState, null));
        return true;
    }

    private void stopThread() {
        Thread _doWorkingThread;
        if ((_doWorkingThread = this.doWorkingThread.get()) != null
                && !Objects.equals(Thread.currentThread(), _doWorkingThread)) {
            _doWorkingThread.interrupt();
        }
    }

    private boolean runPreProxy(AbstractNodeWork<?, ?> comingNode) {
        if (ResultState.EXCEPTION.equals(comingNode.resultDataReference.get().getResultState())) {
            fastFail(INIT, null);
            if (ResultState.EXCEPTION.equals(comingNode.resultDataReference.get().getResultState())) {
                resultDataReference.compareAndSet(null,
                        ResultData.defaultExResultEx(platoNode.getUniqueId(), comingNode.getResult().getEx()));
            } else if (ResultState.TIMEOUT.equals(comingNode.resultDataReference.get().getResultState())) {
                resultDataReference.compareAndSet(null, ResultData.defaultResultTimeOut(platoNode.getUniqueId()));
            }
            return false;
        }
        return true;
    }

    private ResultData<V> getResult() {
        ResultData<V> res = resultDataReference.get();
        return res == null ? ResultData.defaultResultData(platoNode.getUniqueId()) : res;
    }

    private boolean executor(AbstractNodeWork<?, ?> comingNode, GraphRunningInfo graphRunningInfo) {
        try {
            if (!State.setState(state, INIT, WORKING)) {
                return false;
            }
            convert(comingNode, graphRunningInfo);
            V resultValue = platoNode.getiNodeWork().work(param);
            if (!State.setState(state, WORKING, SUCCESS)) {
                return false;
            }
            ResultData resultData = new ResultData(platoNode.getUniqueId(), ResultState.SUCCESS, resultValue);
            resultDataReference.compareAndSet(null, resultData);
            platoNode.getiNodeWork().hook(param, resultData);
        } catch (Exception e) {
            throw new PlatoException(e, "执行异常" + platoNode.getUniqueId());
        }
        return true;
    }

    private void convert(AbstractNodeWork<?, ?> comingNode, GraphRunningInfo graphRunningInfo) {
        if (comingNode != null && preHandler != null) {
            if (preHandler.equals(PreHandler.DEFAULT_PRE_HANDLER)) {
                ResultData resultData = graphRunningInfo.getResultData(comingNode.platoNode.getUniqueId());
                if (resultData != null) {
                    param = (P) resultData.getResult();
                }
            } else {
                param = preHandler.paramHandle(graphRunningInfo);
            }
        }
    }

    private boolean runPreProxies(AbstractNodeWork<?, ?> comingNode) {
        Set<AbstractNodeWork<?, ?>> preEntryNodeProxies = getPreEntryNodeProxies(RelationEnum.STRONG_RELATION);
        return !preEntryNodeProxies.stream().anyMatch(temp -> {
            AtomicInteger state = temp.state;
            if (State.isState(state, INIT) || State.isState(state, WORKING)) {
                return true;
            }
            ResultData<?> resultData = temp.resultDataReference.get();
            if (ResultState.EXCEPTION.equals(resultData.getResultState())) {
                fastFail(INIT, comingNode.getResult().getEx());
                return true;
            }
            return false;
        });
    }

    protected abstract Set<AbstractNodeWork<?, ?>> getNextNodeProxies();

    protected abstract Set<AbstractNodeWork<?, ?>> getPreNodeProxies();

    protected abstract Set<AbstractNodeWork<?, ?>> getPreEntryNodeProxies(RelationEnum relationEnum);

    protected abstract Set<AbstractNodeWork<?, ?>> getNextEntryNodeProxies(RelationEnum relationEnum);

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractNodeWork<?, ?> that = (AbstractNodeWork<?, ?>) o;
        return traceId == that.traceId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(traceId);
    }

    private void shutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.println("执行钩子方法"), "shut down hook"));
    }

    public enum State {
        /**
         * 初始化中，builder正在设置其数值
         */
        BUILDING(-1),
        /**
         * 初始化完成，但是还未执行过。
         */
        INIT(0),
        /**
         * 执行过。
         * 即至少进行了一次各种判定，例如判断 是否跳过/是否启动工作
         */
        STARTED(1),
        /**
         * 工作状态
         */
        WORKING(2),
        /**
         * 工作完成后的收尾工作，例如调用下游wrapper
         */
        AFTER_WORK(3),
        /**
         * 成功执行结束
         */
        SUCCESS(4),
        /**
         * 失败
         */
        ERROR(5),
        /**
         * 被跳过
         */
        SKIP(6);

        public boolean finished() {
            return this == SUCCESS || this == ERROR || this == SKIP;
        }

        State(int id) {
            this.id = id;
        }

        final int id;

        static final State[] states_of_notWorked = new State[] {INIT, STARTED};

        static final State[] states_of_skipOrAfterWork = new State[] {SKIP, AFTER_WORK};

        static final State[] states_of_beforeWorkingEnd = new State[] {INIT, STARTED, WORKING};

        static final State[] states_of_finish = new State[] {SUCCESS, ERROR, SKIP};

        static final State[] states_all =
                new State[] {BUILDING, INIT, STARTED, WORKING, AFTER_WORK, SUCCESS, ERROR, SKIP};

        static boolean setState(AtomicInteger state,
                State[] exceptValues,
                State newValue,
                Consumer<State> withOperate) {
            int current;
            boolean inExcepts;
            while (true) {
                // 判断当前值是否在exceptValues范围内
                current = state.get();
                inExcepts = false;
                for (State exceptValue : exceptValues) {
                    if (inExcepts = current == exceptValue.id) {
                        break;
                    }
                }
                // 如果不在 exceptValues 范围内，直接返回false。
                if (!inExcepts) {
                    return false;
                }
                // 如果在 exceptValues 范围，cas成功返回true，失败（即当前值被修改）则自旋。
                if (state.compareAndSet(current, newValue.id)) {
                    if (withOperate != null) {
                        withOperate.accept(of(current));
                    }
                    return true;
                }
            }
        }

        static boolean setState(AtomicInteger state,
                State exceptValue,
                State newValue) {
            int current;
            // 如果当前值与期望值相同
            while ((current = state.get()) == exceptValue.id) {
                // 则尝试CAS设置新值
                if (state.compareAndSet(current, newValue.id)) {
                    return true;
                }
                // 如果当前值被改变，则尝试自旋
            }
            // 如果当前值与期望值不相同了，就直接返回false
            return false;
        }

        @SuppressWarnings("unused")
        static boolean inStates(AtomicInteger state, State... excepts) {
            int current;
            boolean inExcepts;
            while (true) {
                current = state.get();
                inExcepts = false;
                for (State except : excepts) {
                    if (current == except.id) {
                        inExcepts = true;
                        break;
                    }
                }
                if (state.get() == current) {
                    return inExcepts;
                }
            }
        }

        static boolean isState(AtomicInteger state, @SuppressWarnings("SameParameterValue") State except) {
            return state.compareAndSet(except.id, except.id);
        }

        static State of(int id) {
            return id2state.get(id);
        }

        static final Map<Integer, State> id2state;

        static {
            HashMap<Integer, State> map = new HashMap<>();
            for (State s : State.values()) {
                map.put(s.id, s);
            }
            id2state = Collections.unmodifiableMap(map);
        }


    }
}
