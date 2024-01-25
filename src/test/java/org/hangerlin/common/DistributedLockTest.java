package org.hangerlin.common;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DistributedLockTest {
    private DistributedLock lock;
    private DistributedLock.LockClient mockClient;

    @Before
    public void setUp() {
        mockClient = mock(DistributedLock.LockClient.class);
        lock = new DistributedLock("testKey", "testValue", 30, mockClient);
    }

    @Test
    public void testAcquireLockSuccess() {
        when(mockClient.setNxPx("testKey", "testValue", 30)).thenReturn("RESULT_OK");
        assertTrue("Lock should be acquired", lock.acquireLock());
    }

    @Test
    public void testAcquireLockFailure() {
        when(mockClient.setNxPx("testKey", "testValue", 30)).thenReturn("RESULT_FAIL");
        assertFalse("Lock should not be acquired", lock.acquireLock());
    }

    @Test
    public void testLockRenewal() throws InterruptedException, DistributedLock.LockException {
        when(mockClient.setNxPx("testKey", "testValue", 30)).thenReturn("RESULT_OK");
        lock.acquireLock();
        verify(mockClient, timeout(10000).atLeastOnce()).expire("testKey", 30);
    }

    @Test
    public void testLockRelease() {
        when(mockClient.setNxPx("testKey", "testValue", 30)).thenReturn("RESULT_OK");
        lock.acquireLock();
        lock.close();
        assertFalse("Lock should be released", lock.isRunning);
    }
}
