package org.hangerlin.common.lock;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class FairLock implements Lock {
    // 使用 ConcurrentHashMap 来存储每个线程的重入次数
    private final Map<Thread, Integer> threadReentranceCount = new ConcurrentHashMap<>();
    // 使用 LinkedBlockingQueue 来保持等待锁的线程的顺序，以确保公平性
    private final BlockingQueue<Thread> waitingThreads = new LinkedBlockingQueue<>();
    // 使用 PriorityBlockingQueue 来记录每个线程等待的时间，用于后续的排序，模拟 ZSET 数据结构
    private final PriorityBlockingQueue<Long> waitingTimes = new PriorityBlockingQueue<>();
    // 原子性地记录锁的持有次数
    private final AtomicInteger lockHoldCount = new AtomicInteger(0);
    // 记录当前持有锁的线程
    private Thread lockingThread = null;

    @Override
    public void lock() {
        // 如果当前线程已经持有锁，则增加重入次数
        if (isHeldByCurrentThread()) {
            threadReentranceCount.put(Thread.currentThread(), threadReentranceCount.get(Thread.currentThread()) + 1);
            return;
        }

        // 将当前线程加入等待队列
        waitingThreads.add(Thread.currentThread());
        // 记录当前线程的等待时间
        waitingTimes.offer(System.nanoTime());

        // 无限循环，尝试获取锁
        while (true) {
            // 检查当前线程是否为队列中的下一个线程，并且锁当前没有被持有
            if (Thread.currentThread() == waitingThreads.peek() &&
                    lockHoldCount.get() == 0 &&
                    waitingTimes.peek() != null &&
                    waitingTimes.peek() <= System.nanoTime()) {

                // 当前线程出队，表示获得了锁
                waitingThreads.remove();
                // 从等待时间队列中移除当前线程的记录
                waitingTimes.remove();
                // 设置当前线程为持有锁的线程
                lockingThread = Thread.currentThread();
                // 设置锁的持有次数为1
                lockHoldCount.set(1);
                // 设置当前线程的重入次数为1
                threadReentranceCount.put(Thread.currentThread(), 1);
                break;
            }

            // 如果没有获取锁，则等待
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    // 如果当前线程被中断，则重新设置中断状态，并继续尝试获取锁
                    Thread.currentThread().interrupt();
                    continue;
                }
            }
        }
    }

    @Override
    public void unlock() {
        // 如果尝试释放锁的线程不是当前持有锁的线程，则抛出异常
        if (!isHeldByCurrentThread()) {
            throw new IllegalMonitorStateException("Calling thread has not locked this lock");
        }
        // 获取当前线程的重入次数
        int currentReentranceCount = threadReentranceCount.get(Thread.currentThread());
        // 如果重入次数为1，表示需要完全释放锁
        if (currentReentranceCount == 1) {
            // 从重入次数记录中移除当前线程
            threadReentranceCount.remove(Thread.currentThread());
            // 设置锁的持有次数为0，表示锁已被释放
            lockHoldCount.set(0);
            // 清空当前持有锁的线程
            lockingThread = null;
            // 唤醒其他可能在等待锁的线程
            synchronized (this) {
                notify();
            }
        } else {
            // 如果重入次数大于1，则减少一次重入次数
            threadReentranceCount.put(Thread.currentThread(), currentReentranceCount - 1);
        }
    }

    // 不支持的操作
    @Override
    public void lockInterruptibly() throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    // 不支持的操作
    @Override
    public boolean tryLock() {
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

    private boolean isHeldByCurrentThread() {
        return Thread.currentThread() == lockingThread;
    }
}
