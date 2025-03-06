// 定义时间轮类
package com.example.lock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimeWheel {
    private final int wheelSize; // 时间轮大小（槽位数量）
    private final long tickDuration; // 每个槽位的时间间隔（毫秒）
    private final TimerTaskList[] wheel; // 时间轮槽位数组，每个槽位是一个TimerTaskList
    private int currentIndex; // 当前指针位置
    private final ScheduledExecutorService scheduler; // 定时任务调度器
    private volatile TimeWheel overflowWheel; // 上层时间轮，用于处理溢出任务

    // 构造函数，初始化时间轮
    public TimeWheel(int wheelSize, long tickDuration) {
        this.wheelSize = wheelSize; // 设置时间轮大小
        this.tickDuration = tickDuration; // 设置每个槽位的时间间隔
        this.wheel = new TimerTaskList[wheelSize]; // 初始化槽位数组
        for (int i = 0; i < wheelSize; i++) {
            wheel[i] = new TimerTaskList(); // 每个槽位初始化为一个空的TimerTaskList
        }
        this.currentIndex = 0; // 初始化当前指针位置为0
        this.scheduler = Executors.newSingleThreadScheduledExecutor(); // 创建单线程调度器
        start(); // 启动时间轮
    }

    // 启动时间轮
    private void start() {
        scheduler.scheduleAtFixedRate(this::advance, tickDuration, tickDuration, TimeUnit.MILLISECONDS); // 定时推进指针
    }

    // 推进指针，执行当前槽位的任务
    private void advance() {
        TimerTaskList tasks = wheel[currentIndex]; // 获取当前槽位的任务列表
        for (TimerTask task : tasks.getAllTasks()) {
            task.run(); // 执行任务
        }
        tasks = new TimerTaskList(); // 清空当前槽位
        currentIndex = (currentIndex + 1) % wheelSize; // 移动指针到下一个槽位
    }

    // 添加任务到时间轮
    public void addTask(TimerTask task, long delay) {
        long expirationMs = System.currentTimeMillis() + delay; // 计算任务过期时间
        if (delay < tickDuration * wheelSize) { // 如果延迟在当前时间轮范围内
            int index = (int) ((currentIndex + delay / tickDuration) % wheelSize); // 计算目标槽位索引
            TimerTaskEntry entry = new TimerTaskEntry(task, expirationMs); // 创建任务节点
            wheel[index].add(entry); // 将任务节点添加到目标槽位
        } else { // 如果延迟超出当前时间轮范围
            if (overflowWheel == null) { // 如果上层时间轮不存在
                synchronized (this) { // 加锁确保线程安全
                    if (overflowWheel == null) { // 双重检查锁定
                        overflowWheel = new TimeWheel(wheelSize, tickDuration * wheelSize); // 创建上层时间轮
                    }
                }
            }
            overflowWheel.addTask(task, delay - tickDuration * wheelSize); // 将任务交给上层时间轮处理
        }
    }

    // 停止时间轮
    public void stop() {
        scheduler.shutdown(); // 关闭调度器
        if (overflowWheel != null) { // 如果存在上层时间轮
            overflowWheel.stop(); // 递归停止上层时间轮
        }
    }
}