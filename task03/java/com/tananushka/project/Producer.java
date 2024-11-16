package com.tananushka.project;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static com.tananushka.project.MessageBusLogger.logOperation;

public class Producer implements Runnable {
   private static final AtomicInteger messageCounter = new AtomicInteger(1);
   private static final int MESSAGE_NUMBER_LENGTH = 5;
   private final MessageBus messageBus;
   private final String[] topics;
   private final Random random = new Random();

   public Producer(MessageBus messageBus, String[] topics) {
      this.messageBus = messageBus;
      this.topics = topics;
   }

   @Override
   public void run() {
      try {
         while (true) {
            String topic = topics[random.nextInt(topics.length)];
            String payload = String.format("Message-%0" + MESSAGE_NUMBER_LENGTH + "d", messageCounter.getAndIncrement());
            Message message = new Message(topic, payload);
            messageBus.post(message);
            logOperation(Thread.currentThread().getName(), "Produced: " + payload + " to " + topic);
            Thread.sleep(500);
         }
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
      }
   }
}
