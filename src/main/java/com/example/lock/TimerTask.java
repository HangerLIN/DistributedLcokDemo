// 定义定时任务抽象类
package com.example.lock;

public abstract class TimerTask implements Runnable {
    private boolean completed; // 任务完成标志

    // 构造函数，初始化任务状态
    public TimerTask() {
        this.completed = false; // 初始状态为未完成
    }

    // 标记任务为已完成
    public void markCompleted() {
        this.completed = true; // 设置完成标志
    }

    // 检查任务是否完成
    public boolean isCompleted() {
        return completed; // 返回完成状态
    }

    // 抽象方法，定义超时逻辑
    public abstract void onTimeout();

    // 实现Runnable接口的run方法
    @Override
    public void run() {
        if (!isCompleted()) { // 如果任务未完成
            onTimeout(); // 触发超时逻辑
        }
    }
}