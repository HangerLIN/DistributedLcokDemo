// 定义时间轮槽位中的任务节点
package com.example.lock;

public class TimerTaskEntry {
    private final TimerTask task; // 任务对象
    private final long expirationMs; // 任务过期时间戳
    private TimerTaskList list; // 所属的任务链表
    private TimerTaskEntry prev; // 前驱节点
    private TimerTaskEntry next; // 后继节点

    // 构造函数，初始化任务节点
    public TimerTaskEntry(TimerTask task, long expirationMs) {
        this.task = task; // 设置任务对象
        this.expirationMs = expirationMs; // 设置过期时间
    }

    // 获取任务对象
    public TimerTask getTask() {
        return task; // 返回任务对象
    }

    // 获取任务过期时间
    public long getExpirationMs() {
        return expirationMs; // 返回过期时间
    }

    // 获取所属的任务链表
    public TimerTaskList getList() {
        return list; // 返回链表引用
    }

    // 设置所属的任务链表
    public void setList(TimerTaskList list) {
        this.list = list; // 设置链表引用
    }

    // 获取前驱节点
    public TimerTaskEntry getPrev() {
        return prev; // 返回前驱节点
    }

    // 设置前驱节点
    public void setPrev(TimerTaskEntry prev) {
        this.prev = prev; // 设置前驱节点
    }

    // 获取后继节点
    public TimerTaskEntry getNext() {
        return next; // 返回后继节点
    }

    // 设置后继节点
    public void setNext(TimerTaskEntry next) {
        this.next = next; // 设置后继节点
    }

    // 从链表中移除任务节点
    public void remove() {
        if (list != null) { // 如果任务节点属于某个链表
            list.remove(this); // 调用链表的remove方法移除节点
        }
    }
}