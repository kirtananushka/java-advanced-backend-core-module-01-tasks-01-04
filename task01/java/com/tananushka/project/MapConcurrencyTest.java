package com.tananushka.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class MapConcurrencyTest {
   private static final int ELEMENTS_COUNT = 10000;
   private static final int READER_THREADS = 3;
   private static final int TEST_DURATION_MS = 2000;

   public static void main(String[] args) {
      System.out.println("Testing HashMap (unsafe):");
      testMap(new HashMap<Integer, Integer>());

      System.out.println("\nTesting ConcurrentHashMap:");
      testMap(new ConcurrentHashMap<Integer, Integer>());

      System.out.println("\nTesting Collections.synchronizedMap:");
      testMap(Collections.synchronizedMap(new HashMap<Integer, Integer>()));
   }

   private static void testMap(final Map<Integer, Integer> map) {
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
                  map.put(counter % ELEMENTS_COUNT, counter);
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
                        if (map instanceof ConcurrentHashMap) {
                           for (Integer value : map.values()) {
                              sum += value;
                           }
                        } else if (map instanceof Map) {
                           synchronized (map) {
                              for (Integer value : map.values()) {
                                 sum += value;
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
      System.out.println("Final map size: " + map.size());
   }
}