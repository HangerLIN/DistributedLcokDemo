// 定义时间轮槽位中的任务链表
package com.example.lock;

import java.util.concurrent.locks.ReentrantLock;

public class TimerTaskList {
    private final ReentrantLock lock = new ReentrantLock(); // 锁，用于保证线程安全
    private TimerTaskEntry root; // 哨兵节点，用于简化链表操作

    // 构造函数，初始化哨兵节点
    public TimerTaskList() {
        root = new TimerTaskEntry(null, -1); // 初始化哨兵节点
        root.setPrev(root); // 哨兵节点的前驱指向自身
        root.setNext(root); // 哨兵节点的后继指向自身
    }

    // 添加任务到链表
    public void add(TimerTaskEntry entry) {
        lock.lock(); // 加锁
        try {
            entry.setList(this); // 设置任务节点所属的链表
            TimerTaskEntry tail = root.getPrev(); // 获取链表尾节点
            entry.setPrev(tail); // 设置任务节点的前驱
            entry.setNext(root); // 设置任务节点的后继
            tail.setNext(entry); // 更新尾节点的后继
            root.setPrev(entry); // 更新哨兵节点的前驱
        } finally {
            lock.unlock(); // 解锁
        }
    }

    // 移除任务节点
    public void remove(TimerTaskEntry entry) {
        lock.lock(); // 加锁
        try {
            if (entry.getList() == this) { // 如果任务节点属于当前链表
                TimerTaskEntry prev = entry.getPrev(); // 获取前驱节点
                TimerTaskEntry next = entry.getNext(); // 获取后继节点
                prev.setNext(next); // 更新前驱节点的后继
                next.setPrev(prev); // 更新后继节点的前驱
                entry.setList(null); // 清空任务节点的链表引用
            }
        } finally {
            lock.unlock(); // 解锁
        }
    }

    // 获取链表中的所有任务
    public List<TimerTask> getAllTasks() {
        lock.lock(); // 加锁
        try {
            List<TimerTask> tasks = new ArrayList<>(); // 创建任务列表
            TimerTaskEntry current = root.getNext(); // 从哨兵节点的后继开始遍历
            while (current != root) { // 遍历链表
                tasks.add(current.getTask()); // 将任务添加到列表
                current = current.getNext(); // 移动到下一个节点
            }
            return tasks; // 返回任务列表
        } finally {
            lock.unlock(); // 解锁
        }
    }
}