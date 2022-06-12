package com.example.plato.runningInfo;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;

/**
 * @author zhaodongpo
 * create  2022/5/25 2:54 下午
 * @version 1.0
 */
public class NodeRunningInfo<P, V> {

    private final CountDownLatch COUNT_DOWN_LATCH = new CountDownLatch(1);

    private final AtomicBoolean TIME_OUT = new AtomicBoolean(false);

    private final AtomicInteger cancelState = new AtomicInteger();

    private final CountDownLatch endCDL = new CountDownLatch(1);

    private volatile long endTime = -1;

    private final long startTime;

    private final long timeoutLength;

    public NodeRunningInfo(long startTime, long timeoutLength) {
        this.startTime = startTime;
        this.timeoutLength = timeoutLength;
    }

    public CountDownLatch getEndCDL() {
        return endCDL;
    }

    public class CheckFinishTask implements TimerTask {

        @Override
        public void run(Timeout timeout) throws Exception {
            if (endCDL.getCount() < 1) {
                return;
            }
        }

    }
}
