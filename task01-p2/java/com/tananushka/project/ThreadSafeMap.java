package com.tananushka.project;

import java.util.ArrayList;
import java.util.List;

public class ThreadSafeMap<K, V> {
   private static final int INITIAL_CAPACITY = 16;
   private static final float LOAD_FACTOR = 0.75f;
   private final float loadFactor;
   private Entry<K, V>[] buckets;
   private int size;
   private int threshold;

   @SuppressWarnings("unchecked")
   public ThreadSafeMap() {
      this.loadFactor = LOAD_FACTOR;
      this.buckets = new Entry[INITIAL_CAPACITY];
      this.threshold = (int) (INITIAL_CAPACITY * loadFactor);
   }

   public Iterable<Entry<K, V>> entries() {
      final List<Entry<K, V>> entries = new ArrayList<Entry<K, V>>();
      for (Entry<K, V> bucket : buckets) {
         Entry<K, V> current = bucket;
         while (current != null) {
            entries.add(current);
            current = current.next;
         }
      }
      return entries;
   }

   public synchronized V put(K key, V value) {
      if (key == null) {
         throw new IllegalArgumentException("Key cannot be null");
      }

      if (size >= threshold) {
         resize();
      }

      int index = getIndex(key);
      Entry<K, V> entry = buckets[index];

      while (entry != null) {
         if (entry.key.equals(key)) {
            V oldValue = entry.value;
            entry.value = value;
            return oldValue;
         }
         entry = entry.next;
      }

      Entry<K, V> newEntry = new Entry<K, V>(key, value);
      newEntry.next = buckets[index];
      buckets[index] = newEntry;
      size++;

      return null;
   }

   @SuppressWarnings("unchecked")
   private void resize() {
      Entry<K, V>[] oldBuckets = buckets;
      int newCapacity = oldBuckets.length * 2;
      Entry<K, V>[] newBuckets = new Entry[newCapacity];

      for (Entry<K, V> bucket : oldBuckets) {
         while (bucket != null) {
            Entry<K, V> next = bucket.next;

            int newIndex = Math.abs(bucket.key.hashCode() % newCapacity);

            bucket.next = newBuckets[newIndex];
            newBuckets[newIndex] = bucket;

            bucket = next;
         }
      }

      buckets = newBuckets;
      threshold = (int) (newCapacity * loadFactor);
   }

   public synchronized V get(K key) {
      if (key == null) {
         throw new IllegalArgumentException("Key cannot be null");
      }

      int index = getIndex(key);
      Entry<K, V> entry = buckets[index];

      while (entry != null) {
         if (entry.key.equals(key)) {
            return entry.value;
         }
         entry = entry.next;
      }

      return null;
   }

   private int getIndex(K key) {
      return Math.abs(key.hashCode() % buckets.length);
   }

   public synchronized int size() {
      return size;
   }

   public static class Entry<K, V> {
      K key;
      V value;
      Entry<K, V> next;

      Entry(K key, V value) {
         this.key = key;
         this.value = value;
      }
   }
}