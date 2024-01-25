package org.hangerlin.common.bucket;

import java.util.Queue;
import java.util.LinkedList;
import java.util.UUID;

public class HangerBucket {
    private final Queue<HangerToken> tokens;
    private final int capacity;

    public HangerBucket(int capacity) {
        this.capacity = capacity;
        this.tokens = new LinkedList<>();
        // 初始化令牌
        for (int i = 0; i < capacity; i++) {
            tokens.add(new HangerToken("Node-" + UUID.randomUUID().toString(), new byte[0]));
        }
    }

    public synchronized HangerToken acquireToken() throws InterruptedException {
        while (tokens.isEmpty()) {
            wait(); // 如果没有可用的令牌，则等待
        }
        return tokens.poll(); // 获取令牌
    }

    public synchronized void releaseToken(HangerToken token) {
        if (tokens.size() < capacity) {
            tokens.offer(token);
            notifyAll(); // 释放令牌并通知等待的线程
        }
    }
}
