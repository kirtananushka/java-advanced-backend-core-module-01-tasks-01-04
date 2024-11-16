package com.tananushka.project;

import static com.tananushka.project.ObjectPoolLogger.logError;
import static com.tananushka.project.ObjectPoolLogger.logOperation;
import static com.tananushka.project.ObjectPoolLogger.logSeparator;
import static com.tananushka.project.ObjectPoolLogger.logStart;
import static com.tananushka.project.ObjectPoolLogger.logStop;
import static com.tananushka.project.ObjectPoolLogger.logSuccess;
import static com.tananushka.project.ObjectPoolLogger.logSystemInfo;
import static com.tananushka.project.ObjectPoolLogger.logThreadCreated;
import static com.tananushka.project.ObjectPoolLogger.logWarning;

public class ObjectPoolMain {
   public static void main(String[] args) {
      int poolSize = 5;
      int numProducers = 3;
      int numConsumers = 3;
      int durationSeconds = 5;

      printInitialParameters(poolSize, numProducers, numConsumers, durationSeconds);

      BlockingObjectPool objectPool = new BlockingObjectPool(poolSize);

      Thread[] producers = new Thread[numProducers];
      Thread[] consumers = new Thread[numConsumers];

      for (int i = 0; i < numProducers; i++) {
         producers[i] = new Thread(() -> {
            try {
               while (!Thread.currentThread().isInterrupted()) {
                  PooledObject obj = new PooledObject();
                  logOperation(Thread.currentThread().getName(), "Produced: " + obj);
                  objectPool.take(obj);
                  logSuccess(Thread.currentThread().getName(), "Added: " + obj);
                  objectPool.logPoolState();
                  Thread.sleep(500);
               }
            } catch (InterruptedException e) {
               logWarning(Thread.currentThread().getName(), "Producer interrupted");
               Thread.currentThread().interrupt();
            }
         }, "Producer-" + (i + 1));
         producers[i].start();
         logThreadCreated(producers[i].getName());
      }

      for (int i = 0; i < numConsumers; i++) {
         consumers[i] = new Thread(() -> {
            try {
               while (!Thread.currentThread().isInterrupted()) {
                  PooledObject obj = (PooledObject) objectPool.get();
                  logOperation(Thread.currentThread().getName(), "Consumed: " + obj);
                  objectPool.logPoolState();
                  Thread.sleep(800);
               }
            } catch (InterruptedException e) {
               logWarning(Thread.currentThread().getName(), "Consumer interrupted");
               Thread.currentThread().interrupt();
            }
         }, "Consumer-" + (i + 1));
         consumers[i].start();
         logThreadCreated(consumers[i].getName());
      }

      try {
         logStart("Main", "Running test for " + durationSeconds + " seconds");
         Thread.sleep(durationSeconds * 1000);
      } catch (InterruptedException e) {
         logError("Main", "Main thread interrupted: " + e.getMessage());
      }

      logStart("Main", "Stopping all threads");
      for (Thread producer : producers) {
         producer.interrupt();
      }
      for (Thread consumer : consumers) {
         consumer.interrupt();
      }

      objectPool.logPoolState();
      logStop("Main", "All threads stopped");
   }

   private static void printInitialParameters(int poolSize, int numProducers, int numConsumers, int durationSeconds) {
      logSystemInfo(Thread.currentThread().getName(), "Initial Parameters:");
      logSystemInfo(Thread.currentThread().getName(), "- Pool Size: " + poolSize);
      logSystemInfo(Thread.currentThread().getName(), "- Number of Producers: " + numProducers);
      logSystemInfo(Thread.currentThread().getName(), "- Number of Consumers: " + numConsumers);
      logSystemInfo(Thread.currentThread().getName(), "- Test Duration: " + durationSeconds + " seconds");
      logSeparator();
   }

}
