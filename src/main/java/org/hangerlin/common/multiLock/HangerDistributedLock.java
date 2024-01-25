package org.hangerlin.common.multiLock;

import java.util.List;
import java.util.concurrent.locks.Lock;

public class HangerDistributedLock extends HangerMultiLock {
    private final int maxAllowedFailures;
    private final int minSuccessLocks;

    public HangerDistributedLock(List<Lock> locks, int maxAllowedFailures, int minSuccessLocks) {
        super(locks);
        this.maxAllowedFailures = maxAllowedFailures;
        this.minSuccessLocks = minSuccessLocks;
    }

    @Override
    protected int getMaxAllowedFailures() {
        return maxAllowedFailures;
    }

    @Override
    protected int getMinSuccessLocks() {
        return minSuccessLocks;
    }
}
