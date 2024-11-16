package com.tananushka.project;

import static com.tananushka.project.MessageBusLogger.logSuccess;

public class Consumer implements Runnable {
   private final MessageBus messageBus;
   private final String topic;

   public Consumer(MessageBus messageBus, String topic) {
      this.messageBus = messageBus;
      this.topic = topic;
   }

   @Override
   public void run() {
      try {
         while (true) {
            Message message = messageBus.consume(topic);
            if (message != null) {
               logSuccess(Thread.currentThread().getName(), "Consumed message on topic " + topic + ": " + message.payload());
            }
         }
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
      }
   }
}