# Results

| Java Version | Map Type      | Successful Iterations | Exceptions Caught | Final Map Size |
|--------------|---------------|-----------------------|-------------------|----------------|
| Java 6       | CustomMap     | 44,362                | 6,115             | 10,000         |
|              | ThreadSafeMap | 154,941               | 0                 | 1,046          |
| Java 8       | CustomMap     | 42,550                | 2,579             | 10,000         |
|              | ThreadSafeMap | 121,810               | 0                 | 1,302          |
| Java 10      | CustomMap     | 29,444                | 2,163             | 10,000         |
|              | ThreadSafeMap | 132,918               | 0                 | 1,003          |
| Java 11      | CustomMap     | 27,125                | 2,790             | 10,000         |
|              | ThreadSafeMap | 196,376               | 0                 | 722            |

1. **CustomMap** (unsafe) consistently throws `ConcurrentModificationException` across all Java versions due to lack of
   thread safety.
2. **ThreadSafeMap** handles concurrency without exceptions by synchronizing access to each operation, making it
   thread-safe. However, it has a significantly lower final map size than expected (10,000) across all versions, likely
   due to high contention leading to missed or overwritten entries. This indicates that while `ThreadSafeMap` prevents
   modification exceptions, it struggles under high concurrency.

### Conclusion

- **CustomMap** is not thread-safe and is unsuitable for concurrent access as it frequently throws exceptions in
  multithreaded environments.
- **ThreadSafeMap** provides basic thread safety by synchronizing operations, but it suffers from data consistency
  issues, as its final map size is inconsistent and lower than expected.