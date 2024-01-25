package org.hangerlin.common.readwritelock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class HangerReadWriteLock {

    // 锁信息存储结构，使用 ConcurrentHashMap 以支持高并发
    private final ConcurrentHashMap<String, LockInfo> lockMap = new ConcurrentHashMap<>();

    // 使用 ThreadLocal 来存储每个线程的锁信息
    private final ThreadLocal<LockInfo> threadLockInfo = new ThreadLocal<>();

    // 获取读锁
    public void acquireReadLock(String uuid) {
        // 获取当前线程的锁信息
        LockInfo currentThreadLockInfo = threadLockInfo.get();

        // 如果当前线程已持有读锁，则增加重入计数并返回
        if (currentThreadLockInfo != null && currentThreadLockInfo.mode.equals("read")) {
            currentThreadLockInfo.increment();
            return;
        }

        // 在 lockMap 中存储或更新锁信息
        lockMap.compute(uuid, (k, v) -> {
            if (v == null || v.mode.equals("write")) {
                threadLockInfo.set(new LockInfo("read", 1));
                return new LockInfo("read", 1);
            } else if (v.mode.equals("read")) {
                threadLockInfo.set(v);
                v.increment();
            }
            return v;
        });
    }

    // 释放读锁
    public void releaseReadLock(String uuid) {
        // 获取当前线程的锁信息
        LockInfo currentThreadLockInfo = threadLockInfo.get();

        // 减少重入计数，如果计数为0，则移除线程的锁信息
        if (currentThreadLockInfo != null && currentThreadLockInfo.mode.equals("read")) {
            currentThreadLockInfo.decrement();
            if (currentThreadLockInfo.count.get() == 0) {
                threadLockInfo.remove();
            }
        }

        // 从 lockMap 中移除或更新锁信息
        lockMap.computeIfPresent(uuid, (k, v) -> {
            if (v.mode.equals("read")) {
                v.decrement();
                if (v.count.get() == 0) {
                    return null;
                }
            }
            return v;
        });
    }

    // 获取写锁
    public synchronized void acquireWriteLock(String uuid) {
        // 获取当前线程的锁信息
        LockInfo currentThreadLockInfo = threadLockInfo.get();

        // 如果当前线程已持有写锁，则增加重入计数并返回
        if (currentThreadLockInfo != null && currentThreadLockInfo.mode.equals("write")) {
            currentThreadLockInfo.increment();
            return;
        }

        // 等待写锁变得可用
        while (lockMap.containsKey(uuid) && !lockMap.get(uuid).mode.equals("write")) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        LockInfo lockInfo = new LockInfo("write", 1);
        threadLockInfo.set(lockInfo);
        lockMap.put(uuid, lockInfo);
    }

    // 释放写锁
    public synchronized void releaseWriteLock(String uuid) {
        // 获取当前线程的锁信息
        LockInfo currentThreadLockInfo = threadLockInfo.get();

        // 减少重入计数，如果计数为0，则移除线程的锁信息并唤醒等待的线程
        if (currentThreadLockInfo != null && currentThreadLockInfo.mode.equals("write")) {
            currentThreadLockInfo.decrement();
            if (currentThreadLockInfo.count.get() == 0) {
                threadLockInfo.remove();
                lockMap.remove(uuid);
                notifyAll();
            }
        }
    }

    // 从读锁升级到写锁
    public void upgradeReadToWriteLock(String uuid) throws InterruptedException {
        // 获取当前线程的锁信息
        LockInfo currentThreadLockInfo = threadLockInfo.get();

        // 检查当前线程是否持有读锁
        if (currentThreadLockInfo == null || !currentThreadLockInfo.mode.equals("read")) {
            throw new IllegalStateException("Attempt to upgrade lock without holding the read lock");
        }

        synchronized(this) {
            // 释放当前线程的所有读锁
            int readLockCount = currentThreadLockInfo.count.get();
            currentThreadLockInfo.count.set(0);
            threadLockInfo.remove();

            for (int i = 0; i < readLockCount

                    ; i++) {
                releaseReadLock(uuid);
            }

            // 获取写锁
            acquireWriteLock(uuid);
        }
    }

    // 内部类，用于存储锁的信息
    private static class LockInfo {
        String mode; // 锁的模式（读/写）
        AtomicInteger count; // 重入计数

        LockInfo(String mode, int initialCount) {
            this.mode = mode;
            this.count = new AtomicInteger(initialCount);
        }

        void increment() {
            count.incrementAndGet();
        }

        void decrement() {
            count.decrementAndGet();
        }
    }
}