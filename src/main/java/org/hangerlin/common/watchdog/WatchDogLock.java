package org.hangerlin.common.watchdog;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class WatchDogLock implements Lock {
    private final AtomicBoolean locked = new AtomicBoolean(false);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> watchDogTask;
    private final long defaultLeaseTime = 30000; // 默认租约时间为30秒
    private long leaseTime = -1; // 租约时间，-1代表未设置，将启动watchDog
    private long expirationTime; // 锁的过期时间

    @Override
    public void lock() {
        if (tryLock()) {
            if (leaseTime == -1) { // 没有设置过期时间，启动watchDog
                startWatchDog();
            }
        }
    }

    // 尝试获取锁，如果获取成功返回true
    public boolean tryLock() {
        boolean acquired = locked.compareAndSet(false, true);
        if (acquired) {
            expirationTime = System.currentTimeMillis() + defaultLeaseTime; // 设置锁的过期时间
        }
        return acquired;
    }

    // watchDog机制，定期检查并续期锁
    private void startWatchDog() {
        leaseTime = defaultLeaseTime; // 设置默认租约时间
        watchDogTask = scheduler.scheduleAtFixedRate(this::renewLock, leaseTime / 3, leaseTime / 3, TimeUnit.MILLISECONDS);
    }

    // 模拟Lua脚本续期锁
    private void renewLock() {
        // 在这里使用synchronized代码块模拟Lua脚本的原子操作
        synchronized (this) {
            if (locked.get() && System.currentTimeMillis() < expirationTime) {
                expirationTime = System.currentTimeMillis() + defaultLeaseTime; // 更新锁的过期时间
                System.out.println("Lock lease renewed for another " + (leaseTime / 1000) + " seconds");
                // 实际上，这里应该是与Redis等分布式存储交互的代码，在这里进行续期操作（本处使用直接叠加的模式来续期）
            } else {
                // 如果锁已经被释放或者已经过期，取消watchDog任务
                if (watchDogTask != null) {
                    watchDogTask.cancel(true);
                }
            }
        }
    }

    @Override
    public void unlock() {
        locked.set(false);
        // 如果watchDogTask正在运行，需要取消它
        if (watchDogTask != null && !watchDogTask.isCancelled()) {
            watchDogTask.cancel(true);
        }
    }

    // 实现Lock接口其他必要方法...
    @Override
    public void lockInterruptibly() throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException();
    }
}

