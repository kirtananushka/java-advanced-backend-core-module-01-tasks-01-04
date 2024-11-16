# Module 01 Task 02: SafeDemo vs UnsafeDemo

1. Synchronization mechanism:

```java
// UnsafeDemo - using two separate locks
private static final Object WRITE_LOCK = new Object();
private static final Object READ_LOCK = new Object();

// Unsafe lock ordering:
// Writer:
synchronized (WRITE_LOCK){    // 1. Acquires WRITE_LOCK
synchronized (READ_LOCK){ // 2. Tries to acquire READ_LOCK
      // operations
      }
      }

// Calculator:
synchronized (READ_LOCK){      // 1. Acquires READ_LOCK
synchronized (WRITE_LOCK){ // 2. Tries to acquire WRITE_LOCK
      // operations
      }
      }

// SafeDemo - single lock on collection
synchronized (numbers){  // All threads synchronize on the same object
      // operations
      }
```

2. Why deadlock occurs in UnsafeDemo:

```java
// Possible scenario:
Time 1:
Writer acquires
WRITE_LOCK
Time 2:
Calculator acquires
READ_LOCK
Time 3:
Writer waits for

READ_LOCK(held by Calculator)

Time 4:
Calculator waits for

WRITE_LOCK(held by Writer)
// → Deadlock: both threads waiting for each other's resources
```

3. How it appears in logs:

```
// UnsafeDemo deadlock:
[UnsafeWriter   ] ✅ Acquired WRITE_LOCK
[UnsafeSqrtCalc ] ✅ Acquired READ_LOCK
[UnsafeWriter   ] 🔒 Attempting to acquire READ_LOCK  // Waits forever
[UnsafeSqrtCalc ] 🔒 Attempting to acquire WRITE_LOCK // Waits forever

// SafeDemo - no deadlock:
[SafeWriter     ] 🔒 Attempting to acquire numbers
[SafeWriter     ] 📊 Added: 19, Operations: 1, Size: 1
[SafeWriter     ] 🔓 Released numbers
[SafeSqrtCalc   ] 🔒 Attempting to acquire numbers
[SafeSqrtCalc   ] 📊 Sqrt: 19.00, Operations: 1
[SafeSqrtCalc   ] 🔓 Released numbers
```

4. Key differences:

- UnsafeDemo:
    - Uses two locks
    - Different lock acquisition order
    - Results in deadlock
    - Threads get stuck indefinitely
    - Requires forced interruption

- SafeDemo:
    - Single lock (the list itself)
    - Simple synchronization
    - No possibility of deadlock
    - Threads work sequentially
    - Terminates correctly

5. Performance implications:

```
// UnsafeDemo:
Completes fewer operations due to deadlock

// SafeDemo:
Completes more operations because:
- No deadlocks
- More efficient resource utilization
- Predictable behavior
```

6. Final outcome:

```
UnsafeDemo: 
- Doesn't terminate normally
- Requires deadlock detection
- Threads remain in BLOCKED state

SafeDemo:
- Normal termination
- Correct list size
- All threads reach TERMINATED state
```

Key takeaways:

1. Multiple locks increase deadlock risk
2. Lock ordering is crucial for preventing deadlocks
3. Single lock approach is often simpler and safer
4. Proper synchronization ensures predictable behavior
5. Safe version provides better resource utilization
