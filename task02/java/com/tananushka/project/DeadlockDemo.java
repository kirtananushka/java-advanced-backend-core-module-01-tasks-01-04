package com.tananushka.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.tananushka.project.DeadlockDemoLogger.log;
import static com.tananushka.project.DeadlockDemoLogger.logDeadlock;
import static com.tananushka.project.DeadlockDemoLogger.logError;
import static com.tananushka.project.DeadlockDemoLogger.logLockAcquired;
import static com.tananushka.project.DeadlockDemoLogger.logLockAttempt;
import static com.tananushka.project.DeadlockDemoLogger.logLockReleased;
import static com.tananushka.project.DeadlockDemoLogger.logOperation;
import static com.tananushka.project.DeadlockDemoLogger.logResourceUsage;
import static com.tananushka.project.DeadlockDemoLogger.logSeparator;
import static com.tananushka.project.DeadlockDemoLogger.logStart;
import static com.tananushka.project.DeadlockDemoLogger.logStop;
import static com.tananushka.project.DeadlockDemoLogger.logSuccess;
import static com.tananushka.project.DeadlockDemoLogger.logSystemInfo;
import static com.tananushka.project.DeadlockDemoLogger.logThreadCreated;
import static com.tananushka.project.DeadlockDemoLogger.logThreadState;
import static com.tananushka.project.DeadlockDemoLogger.logWarning;

public class DeadlockDemo {
   public static void main(String[] args) throws InterruptedException {
      logSystemInfo("Main", "Java version: " + System.getProperty("java.version"));
      logSystemInfo("Main", "Available processors: " +
            Runtime.getRuntime().availableProcessors());
      logSystemInfo("Main", "Max memory: " +
            Runtime.getRuntime().maxMemory() / (1024 * 1024) + "MB");
      logStart("Main", "UNSAFE demonstration (prone to deadlock)");
      demonstrateUnsafeVersion();

      log("Main", "Waiting before starting safe version...");
      Thread.sleep(1000);
      System.out.println("\n" + "=".repeat(50) + "\n");
      logStart("Main", "SAFE demonstration");
      demonstrateSafeVersion();
   }

   private static void demonstrateUnsafeVersion() throws InterruptedException {
      UnsafeDemo.Writer writer = new UnsafeDemo.Writer();
      UnsafeDemo.Calculator sumCalculator = new UnsafeDemo.Calculator(true);
      UnsafeDemo.Calculator sqrtCalculator = new UnsafeDemo.Calculator(false);

      Thread writerThread = new Thread(writer, "UnsafeWriter");
      Thread sumThread = new Thread(sumCalculator, "UnsafeSumCalc");
      Thread sqrtThread = new Thread(sqrtCalculator, "UnsafeSqrtCalc");

      long startTime = System.currentTimeMillis();

      writerThread.start();
      sumThread.start();
      sqrtThread.start();

      log("Main", "Unsafe threads started, waiting for potential deadlock...");
      Thread.sleep(5000);

      logWarning("Main", "Attempting to stop unsafe threads (might be deadlocked)");
      writer.stop();
      sumCalculator.stop();
      sqrtCalculator.stop();

      writerThread.interrupt();
      sumThread.interrupt();
      sqrtThread.interrupt();
      writerThread.join(1000);
      sumThread.join(1000);
      sqrtThread.join(1000);
      if (writerThread.isAlive() || sumThread.isAlive() || sqrtThread.isAlive()) {
         logDeadlock("Main");
         logWarning("Main", "Threads failed to finish properly - likely deadlocked");
         logThreadState("Main", "Writer thread state: " + writerThread.getState());
         logThreadState("Main", "Sum thread state: " + sumThread.getState());
         logThreadState("Main", "Sqrt thread state: " + sqrtThread.getState());
      } else {
         logSuccess("Main", "All threads completed successfully");
      }

      long duration = System.currentTimeMillis() - startTime;
      logSystemInfo("Main", String.format("Unsafe demo ran for %.2f seconds", duration / 1000.0));
      logSeparator();
   }

   private static void demonstrateSafeVersion() throws InterruptedException {
      logSystemInfo("Main", "Starting safe version demonstration");
      List<Integer> numbers = new ArrayList<>();

      SafeDemo.Writer writer = new SafeDemo.Writer(numbers);
      SafeDemo.Calculator sumCalculator = new SafeDemo.Calculator(numbers, true);
      SafeDemo.Calculator sqrtCalculator = new SafeDemo.Calculator(numbers, false);

      Thread writerThread = new Thread(writer, "SafeWriter");
      Thread sumThread = new Thread(sumCalculator, "SafeSumCalc");
      Thread sqrtThread = new Thread(sqrtCalculator, "SafeSqrtCalc");

      long startTime = System.currentTimeMillis();

      log("Main", "Starting safe threads");
      writerThread.start();
      sumThread.start();
      sqrtThread.start();

      log("Main", "Safe threads started, will run without deadlock");
      Thread.sleep(5000);

      logStop("Main", "Initiating graceful shutdown");
      writer.stop();
      sumCalculator.stop();
      sqrtCalculator.stop();

      writerThread.interrupt();
      sumThread.interrupt();
      sqrtThread.interrupt();

      writerThread.join();
      sumThread.join();
      sqrtThread.join();

      long duration = System.currentTimeMillis() - startTime;
      logSystemInfo("Main", String.format("Safe demo ran for %.2f seconds", duration / 1000.0));
      logSuccess("Main", "Safe demo completed successfully with " + numbers.size() + " elements");
      logResourceUsage("Main", "Final list size", numbers.size() + " elements");
      logSeparator();
   }

   static class UnsafeDemo {
      private static final List<Integer> numbers = new ArrayList<>();
      private static final Object WRITE_LOCK = new Object();
      private static final Object READ_LOCK = new Object();

      static class Writer implements Runnable {
         private final Random random = new Random();
         private volatile boolean running = true;
         private int operationCount = 0;

         @Override
         public void run() {
            logThreadCreated(Thread.currentThread().getName());
            logStart(Thread.currentThread().getName(), "unsafe writer");
            logThreadState(Thread.currentThread().getName(), "RUNNING");

            while (running) {
               try {
                  logLockAttempt(Thread.currentThread().getName(), "WRITE_LOCK");
                  synchronized (WRITE_LOCK) {
                     logLockAcquired(Thread.currentThread().getName(), "WRITE_LOCK");
                     logLockAttempt(Thread.currentThread().getName(), "READ_LOCK");
                     synchronized (READ_LOCK) {
                        int numberToAdd = random.nextInt(100);
                        numbers.add(numberToAdd);
                        operationCount++;
                        logOperation(Thread.currentThread().getName(),
                              String.format("Added number: %d, Operations: %d, Size: %d",
                                    numberToAdd, operationCount, numbers.size()));

                        if (operationCount % 10 == 0) {
                           logSuccess(Thread.currentThread().getName(),
                                 "Completed " + operationCount + " operations");
                        }
                     }
                     logLockReleased(Thread.currentThread().getName(), "READ_LOCK");
                  }
                  logLockReleased(Thread.currentThread().getName(), "WRITE_LOCK");

                  if (operationCount % 5 == 0) {
                     logResourceUsage(Thread.currentThread().getName(),
                           "Memory", Runtime.getRuntime().freeMemory() / 1024 + "KB free");
                  }

                  Thread.sleep(100);
               } catch (InterruptedException e) {
                  logError(Thread.currentThread().getName(), "Interrupted");
                  logThreadState(Thread.currentThread().getName(), "INTERRUPTED");
                  Thread.currentThread().interrupt();
                  break;
               }
            }
            logStop(Thread.currentThread().getName(), "writer operations");
         }

         public void stop() {
            running = false;
         }
      }

      static class Calculator implements Runnable {
         private final boolean isSumCalculator;
         private volatile boolean running = true;
         private int operationCount = 0;

         public Calculator(boolean isSumCalculator) {
            this.isSumCalculator = isSumCalculator;
         }

         @Override
         public void run() {
            String operationType = isSumCalculator ? "sum" : "sqrt";
            logThreadCreated(Thread.currentThread().getName());
            logStart(Thread.currentThread().getName(), "unsafe " + operationType + " calculator");
            logThreadState(Thread.currentThread().getName(), "RUNNING");

            while (running) {
               try {
                  logLockAttempt(Thread.currentThread().getName(), "READ_LOCK");
                  synchronized (READ_LOCK) {
                     logLockAcquired(Thread.currentThread().getName(), "READ_LOCK");
                     logLockAttempt(Thread.currentThread().getName(), "WRITE_LOCK");
                     synchronized (WRITE_LOCK) {
                        if (!numbers.isEmpty()) {
                           if (isSumCalculator) {
                              calculateSum();
                           } else {
                              calculateSqrt();
                           }
                        } else {
                           logWarning(Thread.currentThread().getName(), "Empty collection");
                        }
                     }
                     logLockReleased(Thread.currentThread().getName(), "WRITE_LOCK");
                  }
                  logLockReleased(Thread.currentThread().getName(), "READ_LOCK");
                  Thread.sleep(isSumCalculator ? 200 : 300);
               } catch (InterruptedException e) {
                  logError(Thread.currentThread().getName(), "Interrupted");
                  logThreadState(Thread.currentThread().getName(), "INTERRUPTED");
                  Thread.currentThread().interrupt();
                  break;
               }
            }
            logStop(Thread.currentThread().getName(), operationType + " calculations");
         }

         private void calculateSum() {
            int sum = numbers.stream().mapToInt(Integer::intValue).sum();
            operationCount++;
            logOperation(Thread.currentThread().getName(),
                  String.format("Sum: %d, Operations: %d", sum, operationCount));
            if (operationCount % 10 == 0) {
               logSuccess(Thread.currentThread().getName(),
                     "Completed " + operationCount + " sum calculations");
            }
         }

         private void calculateSqrt() {
            double sumOfSquares = numbers.stream()
                  .mapToDouble(num -> num * num)
                  .sum();
            double sqrtOfSum = Math.sqrt(sumOfSquares);
            operationCount++;
            logOperation(Thread.currentThread().getName(),
                  String.format("Sqrt: %.2f, Operations: %d", sqrtOfSum, operationCount));
            if (operationCount % 10 == 0) {
               logSuccess(Thread.currentThread().getName(),
                     "Completed " + operationCount + " sqrt calculations");
            }
         }

         public void stop() {
            running = false;
         }
      }
   }

   static class SafeDemo {
      static class Writer implements Runnable {
         private final List<Integer> numbers;
         private final Random random = new Random();
         private volatile boolean running = true;
         private int operationCount = 0;

         public Writer(List<Integer> numbers) {
            this.numbers = numbers;
         }

         @Override
         public void run() {
            logThreadCreated(Thread.currentThread().getName());
            logStart(Thread.currentThread().getName(), "safe writer");
            logThreadState(Thread.currentThread().getName(), "RUNNING");
            logSystemInfo(Thread.currentThread().getName(),
                  "Using synchronized list of size: " + numbers.size());

            while (running) {
               try {
                  logLockAttempt(Thread.currentThread().getName(), "numbers");
                  synchronized (numbers) {
                     int numberToAdd = random.nextInt(100);
                     numbers.add(numberToAdd);
                     operationCount++;
                     logOperation(Thread.currentThread().getName(),
                           String.format("Added: %d, Operations: %d, Size: %d",
                                 numberToAdd, operationCount, numbers.size()));

                     if (operationCount % 10 == 0) {
                        logSuccess(Thread.currentThread().getName(),
                              "Completed " + operationCount + " write operations");
                        logResourceUsage(Thread.currentThread().getName(),
                              "List size", numbers.size() + " elements");
                     }
                  }
                  logLockReleased(Thread.currentThread().getName(), "numbers");
                  Thread.sleep(100);
               } catch (InterruptedException e) {
                  logError(Thread.currentThread().getName(), "Interrupted");
                  logThreadState(Thread.currentThread().getName(), "INTERRUPTED");
                  Thread.currentThread().interrupt();
                  break;
               }
            }
            logStop(Thread.currentThread().getName(), "write operations");
         }

         public void stop() {
            running = false;
         }
      }

      static class Calculator implements Runnable {
         private final List<Integer> numbers;
         private final boolean isSumCalculator;
         private volatile boolean running = true;
         private int operationCount = 0;

         public Calculator(List<Integer> numbers, boolean isSumCalculator) {
            this.numbers = numbers;
            this.isSumCalculator = isSumCalculator;
         }

         @Override
         public void run() {
            String operationType = isSumCalculator ? "sum" : "sqrt";
            logThreadCreated(Thread.currentThread().getName());
            logStart(Thread.currentThread().getName(), "safe " + operationType + " calculator");
            logThreadState(Thread.currentThread().getName(), "RUNNING");

            while (running) {
               try {
                  logLockAttempt(Thread.currentThread().getName(), "numbers");
                  synchronized (numbers) {
                     if (!numbers.isEmpty()) {
                        if (isSumCalculator) {
                           calculateSum();
                        } else {
                           calculateSqrt();
                        }
                     } else {
                        logWarning(Thread.currentThread().getName(), "Empty collection");
                     }
                  }
                  logLockReleased(Thread.currentThread().getName(), "numbers");
                  Thread.sleep(isSumCalculator ? 200 : 300);
               } catch (InterruptedException e) {
                  logError(Thread.currentThread().getName(), "Interrupted");
                  logThreadState(Thread.currentThread().getName(), "INTERRUPTED");
                  Thread.currentThread().interrupt();
                  break;
               }
            }
            logStop(Thread.currentThread().getName(), operationType + " calculations");
         }

         private void calculateSum() {
            int sum = numbers.stream().mapToInt(Integer::intValue).sum();
            operationCount++;
            logOperation(Thread.currentThread().getName(),
                  String.format("Sum: %d, Operations: %d", sum, operationCount));
            if (operationCount % 10 == 0) {
               logSuccess(Thread.currentThread().getName(),
                     "Completed " + operationCount + " sum calculations");
            }
         }

         private void calculateSqrt() {
            double sumOfSquares = numbers.stream()
                  .mapToDouble(num -> num * num)
                  .sum();
            double sqrtOfSum = Math.sqrt(sumOfSquares);
            operationCount++;
            logOperation(Thread.currentThread().getName(),
                  String.format("Sqrt: %.2f, Operations: %d", sqrtOfSum, operationCount));
            if (operationCount % 10 == 0) {
               logSuccess(Thread.currentThread().getName(),
                     "Completed " + operationCount + " sqrt calculations");
            }
         }

         public void stop() {
            running = false;
         }
      }
   }
}