# OS-P2

## Project 2: Real-Time Process Execution & Synchronization

This part of the project simulates process execution using Java threads and demonstrates inter-thread synchronization using the Producer-Consumer problem.

### Files:
- `Scheduler.java` — Simulates process scheduling using thread-based sleep to represent CPU burst time (from Project 1).
- `ProducerConsumerDemo.java` — Implements the Producer-Consumer problem using a bounded buffer, semaphores, and a mutex for thread synchronization.
- `processes.txt` — Used by `Scheduler.java` for process info.

### How to Run:
Compile and run both Java files using:
```bash
javac Scheduler.java
java Scheduler

javac ProducerConsumer.java
java ProducerConsumer
```

### What It Demonstrates:
- Simulated real-time process execution using multithreading.
- Classic Producer-Consumer synchronization using semaphores and mutual exclusion.
