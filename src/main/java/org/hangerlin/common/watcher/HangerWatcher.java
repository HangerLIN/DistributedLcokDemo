package org.hangerlin.common.watcher;

public class HangerWatcher {
    private final Object monitor = new Object();
    private boolean isLocked = true;

    public void process() {
        synchronized(monitor) {
            isLocked = false;
            monitor.notifyAll(); // 当检测到锁释放时，通知所有等待的线程
        }
    }

    public void waitForUnlock() throws InterruptedException {
        synchronized(monitor) {
            while (isLocked) {
                monitor.wait(); // 等待锁释放
            }
        }
    }

    public void lock() {
        synchronized(monitor) {
            isLocked = true;
        }
    }
}
