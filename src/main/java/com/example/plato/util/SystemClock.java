package com.example.plato.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhaodongpo
 * @version 1.0
 * create 2022/3/19 11:59 上午
 */
public class SystemClock {

    private final int period;

    private final AtomicLong now;

    private static class InstanceHolder {
        private static final SystemClock INSTANCE = new SystemClock(1/*1毫秒一次*/);
    }

    private SystemClock(int period) {
        this.period = period;
        this.now = new AtomicLong(System.currentTimeMillis());
        scheduleClockUpdating();
    }

    private static SystemClock instance() {
        return InstanceHolder.INSTANCE;
    }

    private void scheduleClockUpdating() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "plato System Clock");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(() -> now.set(System.currentTimeMillis()), period, period, TimeUnit.MILLISECONDS);
    }

    public static long now() {
        return instance().currentTimeMillis();
    }

    private long currentTimeMillis() {
        return now.get();
    }
}

