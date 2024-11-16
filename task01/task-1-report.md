# Results

| Java Version | Map Type                    | Successful Iterations | Exceptions Caught | Final Map Size |
|--------------|-----------------------------|-----------------------|-------------------|----------------|
| Java 6       | HashMap                     | 25,036                | 1,674             | 10,000         |
|              | ConcurrentHashMap           | 44,544                | 0                 | 10,000         |
|              | Collections.synchronizedMap | 174,358               | 0                 | 1,916          |
| Java 8       | HashMap                     | 37,102                | 815               | 10,000         |
|              | ConcurrentHashMap           | 37,844                | 0                 | 10,000         |
|              | Collections.synchronizedMap | 246,575               | 0                 | 1,136          |
| Java 10      | HashMap                     | 19,075                | 775               | 10,000         |
|              | ConcurrentHashMap           | 34,678                | 0                 | 10,000         |
|              | Collections.synchronizedMap | 358,248               | 0                 | 725            |
| Java 11      | HashMap                     | 19,252                | 686               | 10,000         |
|              | ConcurrentHashMap           | 32,964                | 0                 | 10,000         |
|              | Collections.synchronizedMap | 240,856               | 0                 | 814            |

1. **HashMap** (unsafe) consistently throws exceptions across all Java versions, as expected, since it isn’t
   thread-safe.
2. **ConcurrentHashMap** performs well with no exceptions, maintaining consistent data integrity with a final map size
   of 10,000 across all versions.
3. **Collections.synchronizedMap** handles concurrency well without exceptions but shows unexpectedly low final map
   sizes in all Java versions after Java 6. This discrepancy suggests that while `synchronizedMap` prevents concurrent
   modifications, it might not handle high write contention effectively, leading to overwriting or missed entries.

### Conclusion

- **ConcurrentHashMap** is the preferred choice for concurrent access.
- **synchronizedMap** provides thread safety but suffers from performance and data consistency issues.
- **HashMap** is unsuitable for concurrent scenarios.
