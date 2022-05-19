package com.example.plato.element;

import static com.example.plato.element.AbstractNodeProxy.State.AFTER_WORK;
import static com.example.plato.element.AbstractNodeProxy.State.BUILDING;
import static com.example.plato.element.AbstractNodeProxy.State.ERROR;
import static com.example.plato.element.AbstractNodeProxy.State.STARTED;
import static com.example.plato.element.AbstractNodeProxy.State.SUCCESS;
import static com.example.plato.element.AbstractNodeProxy.State.WORKING;
import static com.example.plato.element.AbstractNodeProxy.State.isState;
import static com.example.plato.element.AbstractNodeProxy.State.setState;
import static com.example.plato.element.AbstractNodeProxy.State.states_all;
import static com.example.plato.element.AbstractNodeProxy.State.states_of_beforeWorkingEnd;
import static com.example.plato.element.AbstractNodeProxy.State.states_of_notWorked;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import com.example.plato.exception.EndsNormallyException;
import com.example.plato.exception.NotExpectedException;
import com.example.plato.exception.SkippedException;
import com.example.plato.handler.AfterHandler;
import com.example.plato.handler.PreHandler;
import com.example.plato.runningInfo.GraphRunningInfo;
import com.example.plato.runningInfo.ResultData;
import com.example.plato.runningInfo.ResultData.ResultState;
import com.example.plato.util.SystemClock;

/**
 * @author zhaodongpo
 * create  2022/5/15 10:31 下午
 * @version 1.0
 */
public abstract class AbstractNodeProxy<P, V> {

    private PlatoNode<P, V> platoNode;

    protected String traceId;

    protected volatile P param;

    protected final AtomicInteger state = new AtomicInteger(State.BUILDING.id);

    protected final AtomicReference<Thread> doWorkingThread = new AtomicReference<>();

    protected AtomicReference<ResultData<V>> resultDataReference = new AtomicReference<>(null);

    AbstractNodeProxy(INodeWork<P, V> iNodeWork, String uniqueId, String graphId,
            PreHandler<P> preHandler, AfterHandler afterHandler, long timeLimit) {
        platoNode = PlatoNode.getInstance(uniqueId, graphId, iNodeWork, preHandler, afterHandler, timeLimit);
    }

    public ResultData<V> getResult() {
        ResultData<V> res = resultDataReference.get();
        return res == null ? ResultData.defaultResultData(platoNode.getUniqueId()) : res;
    }

    public void run(ExecutorService executorService, AbstractNodeProxy<?, ?> comingNode, long remainTime,
            GraphRunningInfo<V> graphRunningInfo) {
        long currentTime = SystemClock.now();
        final Consumer<Boolean> __function_hook = success -> {
            ResultData<V> result = getResult();
            try {
                platoNode.getiNodeWork().hook(param, graphRunningInfo.getResultData(platoNode.getUniqueId()));
            } catch (Exception e) {
                if (setState(state, State.states_of_skipOrAfterWork, ERROR, null)) {
                    fastFail(false, e, result.getEx() instanceof EndsNormallyException);
                }
            }
        };
        final Runnable __function_hook_fast_fail_run_next = () -> {
            __function_hook.accept(false);
            runNext(executorService, currentTime, remainTime, graphRunningInfo);
        };
        final BiConsumer<Boolean, Exception> __function_fast_fail_run_next =
                (fastFail_isTimeout, fastFail_exception) -> {
                    boolean isEndsNormally = fastFail_exception instanceof EndsNormallyException;
                    fastFail(fastFail_isTimeout && !isEndsNormally, fastFail_exception, isEndsNormally);
                    __function_hook_fast_fail_run_next.run();
                };
        final Runnable __function_run = () -> {
            if (setState(state, STARTED, WORKING)) {
                try {
                    execute(comingNode, graphRunningInfo);
                } catch (Exception e) {
                    if (setState(state, WORKING, ERROR)) {
                        __function_fast_fail_run_next.accept(false, e);
                    }
                    return;
                }
            }
            if (setState(state, WORKING, AFTER_WORK)) {
                __function_hook.accept(true);
                runNext(executorService, currentTime, remainTime, graphRunningInfo);
            }
        };
        try {
            if (platoNode.getUniqueId().equals("nodeC")) {
                System.out.println();
            }
            if (isState(state, BUILDING)) {
                throw new IllegalStateException("nodeProxy 还在创建中，不能执行 " + platoNode.getUniqueId());
            }
            if (remainTime <= 0) {
                if (setState(state, states_of_beforeWorkingEnd, ERROR, null)) {
                    __function_fast_fail_run_next.accept(true, null);
                }
                return;
            }
            final AtomicReference<State> oldStateRef = new AtomicReference<>(null);
            if (!setState(state, states_of_notWorked, STARTED, oldStateRef::set)) {
                return;
            }
            if (comingNode == null) {
                __function_run.run();
                return;
            }
            PreHandler<P> preHandler = platoNode.getPreHandler();
            Set<AbstractNodeProxy<?, ?>> preNodeProxies = getPreNodeProxies();
            boolean preLimitThisRun = false;
            if (CollectionUtils.isNotEmpty(preNodeProxies)) {
                preLimitThisRun = preNodeProxies.parallelStream().anyMatch(preNode -> {
                    AfterHandler afterHandler = preNode.platoNode.getAfterHandler();
                    if (afterHandler != null) {
                        Set<String> limitRunNodes = afterHandler.notShouldRunNodes(graphRunningInfo);
                        return CollectionUtils.isNotEmpty(limitRunNodes) && limitRunNodes.contains(
                                platoNode.getUniqueId());
                    }
                    return false;
                });
            }
            if (preLimitThisRun || (preHandler != null && preHandler.suicide(graphRunningInfo))) {
                if (setState(state, STARTED, State.SKIP)) {
                    __function_fast_fail_run_next.accept(false, new SkippedException());
                }
                return;
            }
            __function_run.run();
        } catch (Exception e) {
            setState(state, states_all, ERROR, null);
            NotExpectedException ex = new NotExpectedException(e, this);
            resultDataReference.set(new ResultData(null, ResultState.EXCEPTION, ex));
            __function_fast_fail_run_next.accept(false, ex);
        }
    }

    private void runNext(ExecutorService executorService, long currentTime, long remainTime,
            GraphRunningInfo graphRunningInfo) {
        final long costTime = SystemClock.now() - currentTime;
        final long nextRemainTIme = remainTime - costTime;
        Set<AbstractNodeProxy<?, ?>> nextNodeProxies = getNextNodeProxies();
        if (nextNodeProxies == null) {
            return;
        }
        if (nextNodeProxies.size() == 1) {
            AbstractNodeProxy<?, ?> next = null;
            try {
                next = nextNodeProxies.stream().findFirst().get();
                setState(state, AFTER_WORK, SUCCESS);
            } finally {
                if (next != null) {
                    next.run(executorService, this, nextRemainTIme, graphRunningInfo);
                }
            }
        } else {
            if (executorService == null) {
                List<ForkJoinNodeAction> forkJoinNodeActions = nextNodeProxies.stream()
                        .map(platoNodeProxy -> new ForkJoinNodeAction(platoNodeProxy, this, graphRunningInfo, 1000L))
                        .collect(Collectors.toList());
                ForkJoinTask.invokeAll(forkJoinNodeActions);
            } else {
                nextNodeProxies.forEach(nextNode -> {
                    executorService.execute(() -> {
                        nextNode.run(executorService, this, nextRemainTIme, graphRunningInfo);
                    });
                });
            }
            setState(state, AFTER_WORK, SUCCESS);
        }
    }

    protected void execute(AbstractNodeProxy<?, ?> comingNode, GraphRunningInfo graphRunningInfo) {
        try {
            V result = platoNode.getiNodeWork().work(convertParam(comingNode, graphRunningInfo));
            resultDataReference.compareAndSet(null,
                    graphRunningInfo.putResult(new ResultData(platoNode.getUniqueId(), ResultState.SUCCESS, result)));
        } finally {
            doWorkingThread.set(null);
        }
    }

    private P convertParam(AbstractNodeProxy<?, ?> comingNode, GraphRunningInfo graphRunningInfo) {
        if (comingNode == null) {
            return param;
        }
        PreHandler<P> preHandler = platoNode.getPreHandler();
        if (preHandler == null) {
            ResultData resultData = graphRunningInfo.getResultData(comingNode.platoNode.getUniqueId());
            if (resultData == null) {
                throw new RuntimeException("前一个结果异常");
            } else if (resultData.getResult() != null) {
                return (P) resultData.getResult();
            }
            return null;
        }
        return preHandler.paramHandle(graphRunningInfo);
    }

    protected void fastFail(boolean isTimeOut, Exception e, boolean isEndsNormally) {
        Thread _doWorkingThread;
        if ((_doWorkingThread = this.doWorkingThread.get()) != null
                && !Objects.equals(Thread.currentThread(), _doWorkingThread)) {
            _doWorkingThread.interrupt();
        }
        resultDataReference.compareAndSet(null, new ResultData(platoNode.getUniqueId(), null,
                isTimeOut ? ResultState.TIMEOUT : (isEndsNormally ? ResultState.DEFAULT : ResultState.EXCEPTION), e
        ));
    }

    public abstract Set<AbstractNodeProxy<?, ?>> getNextNodeProxies();

    public abstract Set<AbstractNodeProxy<?, ?>> getPreNodeProxies();

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
