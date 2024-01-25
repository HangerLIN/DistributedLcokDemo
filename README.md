# DistributedLcokDemo

## ConcurrentHashMap 的使用

`ConcurrentHashMap` 是 Java 中的一个线程安全的哈希表实现，它允许多个线程同时访问和修改其中的元素，而不需要显式的同步措施。`ConcurrentHashMap` 提供了一些方法来处理键值对的计算和更新，其中包括 `computeIfPresent` 和 `compute` 方法。

1. `computeIfPresent` 方法：
   `computeIfPresent` 方法用于在指定的键存在时对其进行计算和更新。它的方法签名如下：
   
   ```java
   V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)
   ```
   
   - `key` 是要查找的键。
   - `remappingFunction` 是一个 `BiFunction` 函数，它接受两个参数：键 `key` 和与该键关联的当前值 `value`，并返回一个新的值，用于替代旧值。如果 `remappingFunction` 返回 `null`，则表示删除该键。

   示例：
   
   ```java
   ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
   map.put("key1", 10);
   
   map.computeIfPresent("key1", (k, v) -> v + 5); // 更新键 "key1" 对应的值，现在值为 15
   map.computeIfPresent("key2", (k, v) -> v + 5); // key2 不存在，不进行任何操作
   ```

2. `compute` 方法：
   `compute` 方法用于对指定键的值进行计算和更新，无论该键是否存在。它的方法签名如下：
   
   ```java
   V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction)
   ```
   
   - `key` 是要查找的键。
   - `remappingFunction` 是一个 `BiFunction` 函数，它接受两个参数：键 `key` 和与该键关联的当前值 `value`，并返回一个新的值，用于替代旧值。如果 `remappingFunction` 返回 `null`，则表示删除该键。

   示例：
   
   ```java
   ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
   map.put("key1", 10);
   
   map.compute("key1", (k, v) -> v + 5); // 更新键 "key1" 对应的值，现在值为 15
   map.compute("key2", (k, v) -> v + 5); // 创建键 "key2" 并将其关联的值设置为 5
   ```

这些方法都允许在多线程环境中安全地更新 `ConcurrentHashMap` 中的键值对，并且提供了一种更高效的方式来实现复杂的更新逻辑，而不需要显式的锁定操作。



## watchdogDemo

在`RedissonMockLock`类中，我们创建了一个简化的看门狗锁（WatchDog Lock）机制，模拟了Redisson中的自动续期功能。

1. **锁状态管理**：使用`AtomicBoolean locked`来安全地检查和设置锁的状态，确保在多线程环境中的线程安全。

2. **默认租约时间**：定义了一个`defaultLeaseTime`变量来设置锁的默认租约时间，在没有指定租约时间时使用。

3. **租约时间和过期时间**：
   - `leaseTime`变量用于记录锁的租约时间。当其值为`-1`时表示未设置，此时会启动watchDog机制。
   - `expirationTime`变量用于跟踪锁的过期时间，这是计算是否需要续期的依据。

4. **锁的获取和释放**：
   - `lock`方法尝试获取锁，并在成功时根据`leaseTime`的值决定是否启动watchDog机制。
   - `unlock`方法释放锁，并取消watchDog续期任务（如果有的话）。

5. **watchDog机制实现**：
   - `startWatchDog`方法启动一个定时任务，使用`ScheduledExecutorService`在特定周期（默认租约时间的三分之一）执行`renewLock`方法。而，`renewLock`方法检查锁的状态和过期时间，如果锁是有效的并且未过期，则更新`expirationTime`以续期锁。
   
6. **续期操作模拟**：
   
   在`renewLock`方法中，使用`synchronized`代码块来模拟原子性操作，避免在续期时发生竞态条件。如果当前时间小于`expirationTime`，则模拟续期操作通过更新`expirationTime`变量。

7. **取消watchDog任务**：
   - 如果在`renewLock`方法检查时发现锁已经被释放或过期，则取消watchDog任务，停止续期。

8. **Lock接口实现**：
   - 实现了`Lock`接口的`lock`和`unlock`方法。
   - 其他`Lock`接口方法如`lockInterruptibly`，`tryLock`，`newCondition`被声明为不支持，抛出`UnsupportedOperationException`。



## FairLock

这三个数据结构：一个HashMap来存储线程和其重入次数的关系，一个LinkedBlockingQueue来维护等待线程的顺序（以保证公平性），以及一个PriorityBlockingQueue（作为zset）来管理和排序线程的等待时间。



## HangerReadWriteLock 类

-   使用 `ConcurrentHashMap` 来存储和管理锁的状态。
-   `threadLockInfo` 是一个 `ThreadLocal` 对象，用于存储每个线程的锁信息。
-   `acquireReadLock` 和 `releaseReadLock` 方法实现了读锁的获取和释放。
-   `acquireWriteLock` 和 `releaseWriteLock` 方法实现了写锁的获取和释放。
-   `upgradeReadToWriteLock` 方法提供了从读锁升级到写锁的功能。
-   `LockInfo` 类用于存储锁的模式和重入次数。



## HangerDistributedLock 类

### 基本功能实现

-   `HangerMultiLock` 作为模板父类，定义了加锁的基本流程和通用逻辑。它使用了两个抽象方法 `getMaxAllowedFailures` 和 `getMinSuccessLocks`，这两个方法由子类实现，以提供特定的失败允许个数和成功所需最小锁的个数。
-   `HangerDistributedLock` 是 `HangerMultiLock` 的一个具体实现。它提供了这两个抽象方法的具体实现，即允许的失败个数和成功所需的最小锁的个数。



### calculateLockWaitTime 动态计算

实际上，在这一步的过程中，就是找到`lockWaitTime`, `remainTime`两个数值之间的最小值

使用指数退避策略，随着尝试次数增加，每次尝试的等待时间逐渐减少。

```java
 acquired = lock.tryLock(Math.min(lockWaitTime, remainTime), TimeUnit.MILLISECONDS);
```

`lockWaitTime`就是为了完成，使用 `Math.min(lockWaitTime, remainTime)` 的含义是：在每次尝试获取锁时，我们取这两个时间中较小的一个作为当前尝试的等待时间。这样做的原因有两个：

-   **保证不超时**：确保在任何尝试中，等待获取锁的时间不会超过总剩余的等待时间（`remainTime`）。这是为了遵守最初设定的 `waitTime` 总限制。
-   **优化锁获取尝试**：`lockWaitTime` 可以根据实际情况调整，以优化锁获取的机会，==**如果多次尝试失败，可以逐渐减少每次尝试的等待时间，以更快地重新尝试或释放资源。**==
