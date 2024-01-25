package org.hangerlin.common.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class HangerOptimizeAcquireLock {
    private final ReentrantLock lock = new ReentrantLock();
    private long leaseTimeMillis; // 锁的租期时间
    private long waitTimeMillis;  // 客户端愿意等待锁的最长时间

    public HangerOptimizeAcquireLock(long waitTime, long leaseTime, TimeUnit unit) {
        this.waitTimeMillis = unit.toMillis(waitTime);
        this.leaseTimeMillis = unit.toMillis(leaseTime);
    }

    /**
     * 尝试获取锁
     * @return 是否成功获取锁
     */
    public boolean tryLock() {
        long deadline = System.currentTimeMillis() + waitTimeMillis;
        long newLeaseTime = leaseTimeMillis;
        long remainTime = waitTimeMillis;
        long lockWaitTime = calculateLockWaitTime(remainTime);

        boolean acquired = false;
        try {
            while (System.currentTimeMillis() < deadline) {
                long time = System.currentTimeMillis();
                acquired = lock.tryLock(Math.min(lockWaitTime, remainTime), TimeUnit.MILLISECONDS);

                if (acquired) {
                    startLeaseTimer(newLeaseTime);
                    break;
                }

                // 更新剩余时间，不管是否获取锁成功都需要了：反映的是从当前时刻开始，还剩多少时间可以用于尝试获取锁
                remainTime = remainTime - (System.currentTimeMillis() - time);

                // 短暂休眠以避免过度占用CPU
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return acquired;
    }

    /**
     * 释放锁
     */
    public void unlock() {
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    /**
     * 计算每次尝试获取锁的最大等待时间
     * @param remainTime 剩余的总等待时间
     * @return 每次尝试获取锁的等待时间
     */
    private long attemptCount = 0;

    private long calculateLockWaitTime(long remainTime) {
        attemptCount++;
        // 使用指数退避策略，随着尝试次数增加，每次尝试的等待时间逐渐减少
        long exponentialBackoff = (long) (remainTime * 0.5 / (attemptCount * attemptCount));
        // 确保每次尝试的等待时间不会小于一个合理的最小值，比如 100 毫秒
        long minWaitTime = 100;
        return Math.max(exponentialBackoff, minWaitTime);
    }


    /**
     * 启动一个计时器，在租期结束时自动释放锁
     * @param leaseTime 租期时间
     */
    private void startLeaseTimer(long leaseTime) {
        new Thread(() -> {
            try {
                Thread.sleep(leaseTime);
                unlock();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
