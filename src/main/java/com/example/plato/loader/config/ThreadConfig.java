package com.example.plato.loader.config;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/11 10:58 上午
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreadConfig extends PlatoConfig {

    private int corePoolSize;

    private int maximumPoolSize;

    private long keepAliveTime;

    private TimeUnit unit;

    private BlockingQueue<Runnable> workQueue;

    private RejectedExecutionHandler handler;

    @Override
    void validate() {

    }
}
