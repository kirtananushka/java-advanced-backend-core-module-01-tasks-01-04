package com.tananushka.project;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class CustomMap<K, V> {
   private static final int INITIAL_CAPACITY = 16;
   private static final float LOAD_FACTOR = 0.75f;
   private final float loadFactor;
   transient int modCount;
   private Entry<K, V>[] buckets;
   private int size;
   private int threshold;

   @SuppressWarnings("unchecked")
   public CustomMap() {
      this.loadFactor = LOAD_FACTOR;
      this.buckets = new Entry[INITIAL_CAPACITY];
      this.threshold = (int) (INITIAL_CAPACITY * loadFactor);
   }

   public Iterable<Entry<K, V>> entries() {
      return new Iterable<Entry<K, V>>() {
         @Override
         public Iterator<Entry<K, V>> iterator() {
            return new HashIterator() {
               @Override
               public Entry<K, V> next() {
                  return nextEntry();
               }
            };
         }
      };
   }

   public V put(K key, V value) {
      if (key == null)
         throw new IllegalArgumentException("Key cannot be null");

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
      modCount++;
      return null;
   }

   @SuppressWarnings("unchecked")
   private void resize() {
      Entry<K, V>[] oldBuckets = buckets;
      int newCapacity = oldBuckets.length * 2;
      Entry<K, V>[] newBuckets = new Entry[newCapacity];
      modCount++;

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

   public V get(K key) {
      if (key == null)
         throw new IllegalArgumentException("Key cannot be null");

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

   public int size() {
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

   private abstract class HashIterator implements Iterator<Entry<K, V>> {
      Entry<K, V> next;
      Entry<K, V> current;
      int expectedModCount;
      int index;

      HashIterator() {
         expectedModCount = modCount;
         current = null;
         next = null;
         index = 0;
         while (index < buckets.length && next == null) {
            next = buckets[index++];
         }
      }

      @Override
      public final boolean hasNext() {
         return next != null;
      }

      final Entry<K, V> nextEntry() {
         if (modCount != expectedModCount)
            throw new ConcurrentModificationException();
         Entry<K, V> e = next;
         if (e == null)
            throw new NoSuchElementException();

         next = e.next;
         while (next == null && index < buckets.length) {
            next = buckets[index++];
         }
         current = e;
         return e;
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException();
      }
   }
}