package com.tananushka.project;

import java.util.concurrent.atomic.AtomicInteger;

public class PooledObject {
   private static final AtomicInteger counter = new AtomicInteger(0);
   private final String id;

   public PooledObject() {
      this.id = "Obj-" + counter.incrementAndGet();
   }

   public String getId() {
      return id;
   }

   @Override
   public String toString() {
      return id;
   }
}
