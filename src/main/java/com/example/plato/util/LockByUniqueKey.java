package com.example.plato.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zhaodongpo
 * @version 1.0
 * create 2022/3/18 3:55 下午
 */
public class LockByUniqueKey {

    private final ConcurrentHashMap<Object, LockReference> lockReferenceConcurrentHashMap = new ConcurrentHashMap<>();

    private final ReferenceQueue<ReentrantLock> referenceQueue = new ReferenceQueue<>();

    public Lock getLock(Object key) {
        PlatoAssert.nullException(() -> "getLock param key is null", key);
        int LIMIT_COUNT = 10000;
        if (lockReferenceConcurrentHashMap.size() > LIMIT_COUNT) {
            clearEmptyReference();
        }
        LockReference reference = lockReferenceConcurrentHashMap.get(key);
        ReentrantLock lock = reference == null ? null : reference.get();
        while (lock == null) {
            reference = lockReferenceConcurrentHashMap.putIfAbsent(key,
                    new LockReference(key, new ReentrantLock(), referenceQueue));
            lock = reference == null ? null : reference.get();
            if (lock != null) {
                return lock;
            }
            clearEmptyReference();
        }
        return lock;
    }

    private void clearEmptyReference() {
        Reference<? extends ReentrantLock> reference;
        while ((reference = referenceQueue.poll()) != null) {
            LockReference lockReference = (LockReference) reference;
            lockReferenceConcurrentHashMap.remove(lockReference.key);
        }
    }

    class LockReference extends WeakReference<ReentrantLock> {
        private final Object key;

        public LockReference(Object key, ReentrantLock referent, ReferenceQueue<? super ReentrantLock> q) {
            super(referent, q);
            this.key = key;
        }
    }
}
