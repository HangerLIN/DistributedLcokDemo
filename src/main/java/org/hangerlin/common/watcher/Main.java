package org.hangerlin.common.watcher;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        HangerWatcher watcher = new HangerWatcher();

        Thread thread = new Thread(() -> {
            try {
                System.out.println("Thread waiting for unlock...");
                watcher.waitForUnlock();
                System.out.println("Thread proceeds after unlock.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        thread.start();

        // 模拟某些操作，最终触发锁的释放
        Thread.sleep(2000); // 假设这里有一些操作导致锁被释放
        watcher.process(); // 模拟锁释放的事件
    }
}
