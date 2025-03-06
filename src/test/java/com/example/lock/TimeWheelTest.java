package com.example.lock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class TimeWheelTest {

    private TimeWheel timeWheel;

    @BeforeEach
    void setUp() {
        timeWheel = new TimeWheel(60, 100); // 60个槽位，每个槽位100ms
    }

    @AfterEach
    void tearDown() {
        timeWheel.stop();
    }

    @Test
    void testTaskExecution() throws InterruptedException {
        AtomicBoolean executed = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        Runnable task = () -> {
            executed.set(true);
            latch.countDown();
        };

        timeWheel.addTask(task, 300); // 延迟300ms执行
        assertTrue(latch.await(500, TimeUnit.MILLISECONDS)); // 等待任务完成
        assertTrue(executed.get());
    }

    @Test
    void testOverflowWheel() throws InterruptedException {
        AtomicBoolean executed = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        Runnable task = () -> {
            executed.set(true);
            latch.countDown();
        };

        timeWheel.addTask(task, 7000); // 超出当前时间轮范围，进入上层时间轮
        assertTrue(latch.await(8000, TimeUnit.MILLISECONDS)); // 等待任务完成
        assertTrue(executed.get());
    }

    @Test
    void testStopTimeWheel() throws InterruptedException {
        AtomicBoolean executed = new AtomicBoolean(false);

        Runnable task = () -> executed.set(true);

        timeWheel.addTask(task, 500); // 延迟500ms执行
        timeWheel.stop(); // 立即停止时间轮
        Thread.sleep(1000); // 等待一段时间
        assertFalse(executed.get()); // 任务不应被执行
    }
}