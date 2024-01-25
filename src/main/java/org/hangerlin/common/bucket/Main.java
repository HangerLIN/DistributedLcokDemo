package org.hangerlin.common.bucket;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        HangerBucket bucket = new HangerBucket(5); // 令牌桶容量为 5

        // 模拟获取令牌（本台机子上面）
        HangerToken token = bucket.acquireToken();
        try {
            token.use(); // 使用令牌
            // 执行需要令牌的操作
        } finally {
            token.release(); // 释放令牌
            bucket.releaseToken(token); // 将令牌归还到桶中
        }
    }
}
