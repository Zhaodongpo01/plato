package com.example.plato.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;

/**
 * @author zhaodongpo
 * create  2022/5/27 3:06 下午
 * @version 1.0
 */
public class MyTimer {

    public static void main(String[] args) {
        Timer timer = new HashedWheelTimer();
        CountDownLatch countDownLatch = new CountDownLatch(2);
        Timeout timeoutFirst = timer.newTimeout(timeout1 -> {
            System.out.println("第一个任务");
            countDownLatch.countDown();
        }, 10, TimeUnit.SECONDS);

        Timeout timeoutSecond = timer.newTimeout(timeout2 -> {
            System.out.println("第二个任务");
            countDownLatch.countDown();
        }, 5, TimeUnit.SECONDS);

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("延迟任务结束");
    }

}
