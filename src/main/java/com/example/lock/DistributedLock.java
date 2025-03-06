// 定义分布式锁类
package com.example.lock;

public class DistributedLock {
    private final TimeWheel timeWheel; // 时间轮实例

    // 构造函数，初始化时间轮，60个槽位，每个槽位1秒
    public DistributedLock() {
        this.timeWheel = new TimeWheel(60, 1000); // 初始化时间轮
    }

    // 注册看门狗任务到时间轮
    public void registerWatchdog(WatchdogTask task, long timeout) {
        timeWheel.addTask(task, timeout); // 将任务添加到时间轮
    }

    // 示例方法：模拟任务完成
    public void completeTask(WatchdogTask task) {
        task.markCompleted(); // 标记任务为已完成
    }

    // 关闭时间轮
    public void shutdown() {
        timeWheel.stop(); // 停止时间轮
    }
}