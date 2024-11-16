package com.tananushka.project;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.tananushka.project.ObjectPoolLogger.logLockAttempt;
import static com.tananushka.project.ObjectPoolLogger.logLockReleased;
import static com.tananushka.project.ObjectPoolLogger.logOperation;
import static com.tananushka.project.ObjectPoolLogger.logSystemInfo;

public class BlockingObjectPool {
   private final Queue<Object> pool;
   private final int maxSize;
   private final Lock lock = new ReentrantLock();
   private final Condition notEmpty = lock.newCondition();
   private final Condition notFull = lock.newCondition();

   public BlockingObjectPool(int size) {
      this.maxSize = size;
      this.pool = new LinkedList<>();
      for (int i = 0; i < size; i++) {
         pool.add(new PooledObject());
      }
      logSystemInfo("BlockingObjectPool", "Pool initialized with size: " + size);
   }

   public Object get() throws InterruptedException {
      lock.lock();
      try {
         logLockAttempt(Thread.currentThread().getName(), "notEmpty condition");
         while (pool.isEmpty()) {
            logLockAttempt(Thread.currentThread().getName(), "Pool is empty. Waiting...");
            notEmpty.await();
         }
         Object obj = pool.poll();
         logLockReleased(Thread.currentThread().getName(), "Object retrieved: " + obj);
         logOperation(Thread.currentThread().getName(), "Pool size after get: " + pool.size());
         notFull.signal();
         return obj;
      } finally {
         lock.unlock();
      }
   }

   public void take(Object object) throws InterruptedException {
      lock.lock();
      try {
         logLockAttempt(Thread.currentThread().getName(), "notFull condition");
         while (pool.size() == maxSize) {
            logLockAttempt(Thread.currentThread().getName(), "Pool is full. Waiting...");
            notFull.await();
         }
         pool.offer(object);
         logLockReleased(Thread.currentThread().getName(), "Object added: " + object);
         logOperation(Thread.currentThread().getName(), "Pool size after take: " + pool.size());
         notEmpty.signal();
      } finally {
         lock.unlock();
      }
   }

   public void logPoolState() {
      lock.lock();
      try {
         logSystemInfo("BlockingObjectPool", "Current pool size: " + pool.size());
         logSystemInfo("BlockingObjectPool", "Current pool contents: " + pool);
      } finally {
         lock.unlock();
      }
   }
}
