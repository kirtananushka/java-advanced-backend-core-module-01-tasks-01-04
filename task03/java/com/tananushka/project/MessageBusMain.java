package com.tananushka.project;

import static com.tananushka.project.MessageBusLogger.logError;
import static com.tananushka.project.MessageBusLogger.logSeparator;
import static com.tananushka.project.MessageBusLogger.logStart;
import static com.tananushka.project.MessageBusLogger.logStop;
import static com.tananushka.project.MessageBusLogger.logSystemInfo;
import static com.tananushka.project.MessageBusLogger.logThreadCreated;
import static com.tananushka.project.MessageBusLogger.logThreadState;

public class MessageBusMain {
   public static void main(String[] args) {
      int busCapacity = 20;
      int numTopics = 2;
      int numProducers = 4;
      int numConsumers = 2;
      int durationSeconds = 5;

      String[] topics = generateTopics(numTopics);

      MessageBus messageBus = new MessageBus(busCapacity);

      printInitialParameters(busCapacity, numTopics, numProducers, numConsumers, durationSeconds);

      Thread[] producerThreads = new Thread[numProducers];
      for (int i = 0; i < numProducers; i++) {
         producerThreads[i] = new Thread(new Producer(messageBus, topics), "Producer-" + (i + 1));
         producerThreads[i].start();
         logThreadCreated(producerThreads[i].getName());
      }

      Thread[][] consumerThreads = new Thread[numTopics][numConsumers];
      for (int i = 0; i < numTopics; i++) {
         for (int j = 0; j < numConsumers; j++) {
            consumerThreads[i][j] = new Thread(new Consumer(messageBus, topics[i]),
                  "Consumer-Topic" + (i + 1) + "-" + (j + 1));
            consumerThreads[i][j].start();
            logThreadCreated(consumerThreads[i][j].getName());
         }
      }

      logStart(Thread.currentThread().getName(), "Waiting for process completion");
      try {
         Thread.sleep(durationSeconds * 1000);
      } catch (InterruptedException e) {
         logError(Thread.currentThread().getName(), "Sleep interrupted: " + e.getMessage());
      }
      logStop(Thread.currentThread().getName(), "Waiting for process completion");

      logStart(Thread.currentThread().getName(), "Interrupting threads");
      for (Thread producerThread : producerThreads) {
         producerThread.interrupt();
         logThreadState(producerThread.getName(), "Interrupted");
      }
      for (Thread[] consumerThreadsPerTopic : consumerThreads) {
         for (Thread consumerThread : consumerThreadsPerTopic) {
            consumerThread.interrupt();
            logThreadState(consumerThread.getName(), "Interrupted");
         }
      }
      logStop(Thread.currentThread().getName(), "Interrupting threads");

      printFinalReport(messageBus, topics);
   }

   private static String[] generateTopics(int numTopics) {
      String[] topics = new String[numTopics];
      for (int i = 0; i < numTopics; i++) {
         topics[i] = "topic" + (i + 1);
      }
      return topics;
   }

   private static void printInitialParameters(int busCapacity, int numTopics, int numProducers,
                                              int numConsumers, int durationSeconds) {
      logSystemInfo(Thread.currentThread().getName(), "Initial Parameters:");
      logSystemInfo(Thread.currentThread().getName(), "- Bus Capacity: " + busCapacity);
      logSystemInfo(Thread.currentThread().getName(), "- Number of Topics: " + numTopics);
      logSystemInfo(Thread.currentThread().getName(), "- Number of Producers: " + numProducers);
      logSystemInfo(Thread.currentThread().getName(), "- Number of Consumers per Topic: " + numConsumers);
      logSystemInfo(Thread.currentThread().getName(), "- Duration: " + durationSeconds + " seconds");
      logSeparator();
   }

   private static void printFinalReport(MessageBus messageBus, String[] topics) {
      logSystemInfo(Thread.currentThread().getName(), "Final Report:");
      logSystemInfo(Thread.currentThread().getName(), "- Messages in the Bus: " + messageBus.getMessageCount());
      for (String topic : topics) {
         logSystemInfo(Thread.currentThread().getName(), "- Messages Consumed for Topic '" + topic + "': " + messageBus.getConsumedCount(topic));
      }
   }
}