package com.example.plato.util;

import java.util.concurrent.locks.Lock;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/3/8 3:21 下午
 */
public class WeakRefTest {

    private WeakRefHashLock weakRefHashLock;

    public WeakRefTest(WeakRefHashLock weakRefHashLock) {
        this.weakRefHashLock = weakRefHashLock;
    }

    public void testLock(Object key) throws InterruptedException {
        Lock lock = weakRefHashLock.lock(key);
        lock.lock();
        try {
            Thread.sleep(100l);
            //模拟流程执行时间
        } finally {
            lock.unlock();
        }
    }
}
