package com.tananushka.project;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class CustomMapConcurrencyTest {
   private static final int ELEMENTS_COUNT = 10000;
   private static final int READER_THREADS = 3;
   private static final int TEST_DURATION_MS = 2000;

   public static void main(String[] args) {
      System.out.println("Testing CustomMap (unsafe):");
      testMap(new com.tananushka.project.CustomMap<Integer, Integer>());

      System.out.println("\nTesting ThreadSafeMap:");
      testMap(new com.tananushka.project.ThreadSafeMap<Integer, Integer>());
   }

   private static void testMap(final Object map) {
      final CountDownLatch startLatch = new CountDownLatch(1);
      final AtomicLong totalIterations = new AtomicLong(0);
      final AtomicLong exceptionsCount = new AtomicLong(0);
      List<Thread> threads = new ArrayList<Thread>();

      Thread writer = new Thread(new Runnable() {
         public void run() {
            try {
               startLatch.await();
               int counter = 0;
               while (!Thread.currentThread().isInterrupted()) {
                  if (map instanceof com.tananushka.project.CustomMap) {
                     ((com.tananushka.project.CustomMap<Integer, Integer>) map).put(counter % ELEMENTS_COUNT, counter);
                  } else {
                     ((com.tananushka.project.ThreadSafeMap<Integer, Integer>) map).put(counter % ELEMENTS_COUNT, counter);
                  }
                  counter++;
                  if (counter % 100 == 0) {
                     Thread.sleep(1);
                  }
               }
            } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
            }
         }
      });
      threads.add(writer);

      for (int i = 0; i < READER_THREADS; i++) {
         Thread reader = new Thread(new Runnable() {
            public void run() {
               try {
                  startLatch.await();
                  while (!Thread.currentThread().isInterrupted()) {
                     try {
                        long sum = 0;
                        if (map instanceof com.tananushka.project.CustomMap) {
                           com.tananushka.project.CustomMap<Integer, Integer> customMap = (com.tananushka.project.CustomMap<Integer, Integer>) map;
                           for (com.tananushka.project.CustomMap.Entry<Integer, Integer> entry : customMap.entries()) {
                              if (entry.value != null) {
                                 sum += entry.value;
                              }
                           }
                        } else {
                           com.tananushka.project.ThreadSafeMap<Integer, Integer> threadSafeMap =
                                 (com.tananushka.project.ThreadSafeMap<Integer, Integer>) map;
                           synchronized (threadSafeMap) {
                              for (com.tananushka.project.ThreadSafeMap.Entry<Integer, Integer> entry :
                                    threadSafeMap.entries()) {
                                 if (entry.value != null) {
                                    sum += entry.value;
                                 }
                              }
                           }
                        }
                        totalIterations.incrementAndGet();
                     } catch (ConcurrentModificationException e) {
                        exceptionsCount.incrementAndGet();
                     }
                  }
               } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
               }
            }
         });
         threads.add(reader);
      }

      for (Thread thread : threads) {
         thread.start();
      }
      startLatch.countDown();

      try {
         Thread.sleep(TEST_DURATION_MS);
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
      }

      for (Thread thread : threads) {
         thread.interrupt();
      }

      for (Thread thread : threads) {
         try {
            thread.join();
         } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
         }
      }

      System.out.println("Map implementation: " + map.getClass().getSimpleName());
      System.out.println("Total successful iterations: " + totalIterations.get());
      System.out.println("Total exceptions caught: " + exceptionsCount.get());
      if (map instanceof com.tananushka.project.CustomMap) {
         System.out.println("Final map size: " + ((com.tananushka.project.CustomMap) map).size());
      } else {
         System.out.println("Final map size: " + ((com.tananushka.project.ThreadSafeMap) map).size());
      }
   }
}