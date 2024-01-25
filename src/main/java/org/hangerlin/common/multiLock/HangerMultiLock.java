package org.hangerlin.common.multiLock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

public abstract class HangerMultiLock {
    private final List<Lock> locks;

    public HangerMultiLock(List<Lock> locks) {
        this.locks = locks;
    }

    protected abstract int getMaxAllowedFailures();
    protected abstract int getMinSuccessLocks();

    public boolean tryLock() {
        int successes = 0;
        int failures = 0;
        List<Lock> acquiredLocks = new ArrayList<>();

        for (Lock lock : locks) {
            try {
                if (lock.tryLock()) {
                    acquiredLocks.add(lock);
                    successes++;
                    if (successes >= getMinSuccessLocks()) {
                        return true;
                    }
                } else {
                    failures++;
                    if (failures > getMaxAllowedFailures()) {
                        releaseLocks(acquiredLocks);
                        return false;
                    }
                }
            } catch (Exception e) {
                releaseLocks(acquiredLocks);
                return false;
            }
        }

        return successes >= getMinSuccessLocks();
    }

    private void releaseLocks(List<Lock> locks) {
        for (Lock lock : locks) {
            try {
                lock.unlock();
            } catch (Exception ignored) {
            }
        }
    }
}
