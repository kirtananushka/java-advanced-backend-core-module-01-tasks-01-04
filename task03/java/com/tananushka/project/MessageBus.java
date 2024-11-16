package com.tananushka.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.tananushka.project.MessageBusLogger.logLockAttempt;
import static com.tananushka.project.MessageBusLogger.logOperation;

public class MessageBus {
   private final List<Message> messages;
   private final Map<String, Integer> consumedCounts;
   private final int capacity;
   private final Lock lock;
   private final Condition notFull;
   private final Condition notEmpty;

   public MessageBus(int capacity) {
      this.messages = new ArrayList<>(capacity);
      this.consumedCounts = new HashMap<>();
      this.capacity = capacity;
      this.lock = new ReentrantLock();
      this.notFull = lock.newCondition();
      this.notEmpty = lock.newCondition();
   }

   public void post(Message message) throws InterruptedException {
      lock.lock();
      try {
         while (messages.size() == capacity) {
            logLockAttempt(Thread.currentThread().getName(), "notFull condition");
            notFull.await();
         }
         messages.add(message);
         logOperation(Thread.currentThread().getName(), "Posted message: " + message.payload());
         notEmpty.signalAll();
      } finally {
         lock.unlock();
      }
   }

   public Message consume(String topic) throws InterruptedException {
      lock.lock();
      try {
         while (messages.isEmpty()) {
            logLockAttempt(Thread.currentThread().getName(), "notEmpty condition");
            notEmpty.await();
         }
         for (Message message : messages) {
            if (message.topic().equals(topic)) {
               messages.remove(message);
               logOperation(Thread.currentThread().getName(), "Consumed message: " + message.payload());
               consumedCounts.put(topic, consumedCounts.getOrDefault(topic, 0) + 1);
               notFull.signalAll();
               return message;
            }
         }
         return null;
      } finally {
         lock.unlock();
      }
   }

   public int getMessageCount() {
      lock.lock();
      try {
         return messages.size();
      } finally {
         lock.unlock();
      }
   }

   public int getConsumedCount(String topic) {
      lock.lock();
      try {
         return consumedCounts.getOrDefault(topic, 0);
      } finally {
         lock.unlock();
      }
   }
}