## Task 1 - Das Experiment

**Cost**: 20 points.

Create `HashMap<Integer, Integer>`.
The first thread adds elements into the map, the other go along the given map and sum the values.
Threads should work before catching `ConcurrentModificationException`.
Try to fix the problem with `ConcurrentHashMap` and `Collections.synchronizedMap()`.
What has happened after simple `Map` implementation exchanging?
How it can be fixed in code? Try to write your custom `ThreadSafeMap` with synchronization and without.
Run your samples with different versions of Java (6, 8, and 10, 11) and measure the performance.
Provide a simple report to your mentor.

## Task 2 - Deadlocks

**Cost**: 20 points.

Create three threads:

* 1st thread is infinitely writing random number to the collection;
* 2nd thread is printing sum of the numbers in the collection;
* 3rd is printing square root of sum of squares of all numbers in the collection.

Make these calculations thread-safe using synchronization block. Fix the possible deadlock.

## Task 3 - Where’s Your Bus, Dude?

**Cost**: 20 points.

Implement message bus using Producer-Consumer pattern.

1. Implement asynchronous message bus. Do not use queue implementations from `java.util.concurrent`.
2. Implement producer, which will generate and post randomly messages to the queue.
3. Implement consumer, which will consume messages on specific topic and log to the console message payload.
4. (Optional) Application should create several consumers and producers that run in parallel.

## Task 4

**Cost**: 20 points.

Create simple object pool with support for multithreaded environment.
No any extra inheritance, polymorphism or generics needed here, just implementation of simple class:

```java
/**
 * Block that pool when it has not any items or it full

 */
public class BlockingObjectPool {

   /**
    * Creates filled pool of passed size
    *
    * @param size of pool
    */
   public BlockingObjectPool(int size) {
        ...
   }

   /**
    * Gets object from pool or blocks if pool is empty
    *
    * @return object from pool
    */
   public Object get() {
        ...
   }

   /**
    * Puts object to pool or blocks if pool is full
    *
    * @param object to be taken back to pool
    */
   public void take(Object object) {
        ...
   }
}
```

Use any blocking approach you like.
