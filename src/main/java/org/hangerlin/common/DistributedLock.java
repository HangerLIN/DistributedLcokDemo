package org.hangerlin.common;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class DistributedLock implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(DistributedLock.class.getName());
    public volatile boolean isRunning;
    private final String key;
    private final String value;
    private final long ttl; // 时间以秒为单位
    private final LockClient client; // 假设 LockClient 是用于锁操作的客户端
    private RenewTask renewalTask;

    public DistributedLock(String key, String value, long ttl, LockClient client) {
        this.key = key;
        this.value = value;
        this.ttl = ttl;
        this.client = client;
    }

    public boolean acquireLock() {
        // 抢锁逻辑
        if ("RESULT_OK".equals(client.setNxPx(key, value, ttl))) {
            // 续期
            startRenewalTask();
            return true;
        }
        return false;
    }

    private void startRenewalTask() {
        renewalTask = new RenewTask(() -> {
            // 刷新值
            client.expire(key, ttl <= 0 ? 10 : ttl);
        }, ttl);

        isRunning = true;
        new Thread(this).start();
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                // 1、续租，刷新值
                renewalTask.callBack();
                LOGGER.info("续租成功!");
                // 2、三分之一过期时间续租
                TimeUnit.SECONDS.sleep(ttl * 1000 / 3);
            } catch (InterruptedException e) {
                LOGGER.warning("续期线程被中断");
                close();
            } catch (LockException e) {
                LOGGER.warning("续期失败");
                close();
            }
        }
    }

    public synchronized void close() {
        if (isRunning) {
            isRunning = false;
            // 清理资源，如有必要
            // ...
        }
    }

    // 假设的 RenewTask 类
    private static class RenewTask {
        private final IRenewalHandler callBack;
        private final long ttl;

        public RenewTask(IRenewalHandler callBack, long ttl) {
            this.callBack = callBack;
            this.ttl = ttl;
        }

        public void callBack() throws LockException {
            this.callBack.callBack();
        }
    }

    // 假设的 IRenewalHandler 接口
    public interface IRenewalHandler {
        void callBack() throws LockException;
    }

    // 假设的 LockClient 类
    public static class LockClient {
        public String setNxPx(String key, String value, long ttl) {
            // 实现抢锁逻辑
            return "RESULT_OK"; // 示例返回值
        }

        public void expire(String key, long ttl) {
            // 实现续期逻辑
        }
    }

    // 假设的 LockException 类
    public static class LockException extends Exception {
        public LockException(String message) {
            super(message);
        }
    }
}
