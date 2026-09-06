# Async Processing — `com.accelaero.common.type.async`

> Part of the `aero-ops-common` module — a reusable core library shared across the AeroOPS platform.

---

## Overview

This package provides a simple way to run multiple tasks **at the same time** instead of one after another.

**The core idea:**
- Register tasks → call `execute()` → all tasks run in parallel → wait for all to finish → read results.

There are two kinds of tasks you can register:

| Task Type | Use When | Returns a value? |
|---|---|---|
| `AsyncProcess` | You want to run something in parallel but don't need the result | No |
| `AsyncDataProvider` / `AsyncDataListProvider` | You want to fetch data in parallel and use the result later | Yes |

---

## Components at a Glance

```
AsyncProcessor              ← orchestrator: registers tasks, runs them, waits for all
├── AsyncProcess            ← fire-and-forget task (no return value)
├── AsyncDataProvider<T>    ← data-fetching task, returns T
└── AsyncDataListProvider<T>← data-fetching task, returns List<T>

AsyncExecutor               ← Spring @Async wrapper for one-off background execution
```

---

## `AsyncProcessor`

The main class. You register tasks, call `execute()`, then read results.

**Default timeout: 2 minutes.** All tasks must finish within this window or they are cancelled and an exception is thrown.

### Methods

| Method | What it does |
|---|---|
| `addProcessor(AsyncProcess)` | Registers a fire-and-forget parallel task |
| `getProviderBySupplier(Supplier<T>)` | Registers a data-fetching task that returns a single value |
| `getProviderByList(ListSupplier<T>)` | Registers a data-fetching task that returns a list |
| `execute()` | Runs all registered tasks in parallel and waits for them to complete |

### How `execute()` works internally

1. Creates a fixed thread pool sized to the total number of registered tasks.
2. Submits all tasks simultaneously.
3. Uses a `CountDownLatch` to wait until every task completes (or the timeout is reached).
4. Shuts down the thread pool cleanly.
5. Throws `AccelaeroProcessException` if timeout is exceeded or an error occurs.

---

## `AsyncProcess`

A simple interface for tasks that run in parallel but **don't return a value** (side effects, writes, notifications, etc.).

```java
public interface AsyncProcess {
    void process();
}
```

### Usage

```java
AsyncProcessor async = new AsyncProcessor();

async.addProcessor(() -> notificationService.sendAlert(flightId));
async.addProcessor(() -> auditService.log(event));

async.execute(); // both run in parallel
```

---

## `AsyncDataProvider<T>`

For tasks that **fetch data** in parallel. You register it before `execute()`, then call `.get()` after to retrieve the result.

```java
AsyncDataProvider<T> provider = asyncProcessor.getProviderBySupplier(supplier);

asyncProcessor.execute(); // supplier runs in parallel

T result = provider.get(); // blocks until the result is available
```

> `.get()` is safe to call only after `execute()` returns. Calling it before will block or throw.

---

## `AsyncDataListProvider<T>`

Same as `AsyncDataProvider<T>`, but for suppliers that return a `List<T>`.

```java
AsyncDataListProvider<Flight> flightsProvider = asyncProcessor.getProviderByList(
    () -> flightRepository.findByDate(date)
);
```

---

## Full Example

Imagine a service that needs flight data, crew data, and gate data — all from separate repositories. Normally these run one after another. With `AsyncProcessor`, they all run at the same time.

```java
AsyncProcessor async = new AsyncProcessor();

// Register parallel data fetches
AsyncDataListProvider<Flight>  flights  = async.getProviderByList(() -> flightRepo.findByDate(date));
AsyncDataListProvider<Crew>    crews    = async.getProviderByList(() -> crewRepo.findByDate(date));
AsyncDataProvider<GateStatus>  gates    = async.getProviderBySupplier(() -> gateService.getStatus());

// Run all three in parallel — waits until all complete
async.execute();

// All results are now available
List<Flight>  flightList  = flights.get();
List<Crew>    crewList    = crews.get();
GateStatus    gateStatus  = gates.get();
```

**Without `AsyncProcessor`:** 3 calls run sequentially → total time = A + B + C.
**With `AsyncProcessor`:** 3 calls run in parallel → total time ≈ max(A, B, C).

---

## Custom Timeout

The default timeout is **2 minutes (120,000 ms)**. Pass a custom value in milliseconds to the constructor:

```java
AsyncProcessor async = new AsyncProcessor(30_000); // 30 seconds
```

If tasks exceed the timeout, pending ones are cancelled and `AccelaeroProcessException` is thrown.

---

## `AsyncExecutor`

A lightweight Spring `@Async` wrapper for one-off background tasks. Unlike `AsyncProcessor` (which you control), `AsyncExecutor` delegates thread management entirely to Spring's async thread pool.

```java
public class AsyncExecutor {
    @Async
    public void executeAsync(SimpleProcess process) {
        process.execute();
    }
}
```

Use this when you want to fire a single background task and don't need to wait for the result.

```java
asyncExecutor.executeAsync(() -> reportService.generateReport(flightId));
// returns immediately; report generates in the background
```

**`AsyncExecutor` vs `AsyncProcessor` — when to use which:**

| | `AsyncProcessor` | `AsyncExecutor` |
|---|---|---|
| Number of tasks | Multiple tasks at once | One task at a time |
| Wait for completion | Yes — `execute()` blocks until done | No — fire and forget |
| Collect results | Yes — via `.get()` | No |
| Thread management | Self-managed fixed pool | Spring's `@Async` thread pool |

---

## Error Handling

| Situation | What happens |
|---|---|
| A task throws an exception | Wrapped in `AccelaeroProcessException` |
| Timeout exceeded | Pending tasks cancelled, `AccelaeroProcessException` thrown |
| Thread interrupted | Thread interrupt flag restored, `AccelaeroProcessException` thrown |
| `.get()` called on a failed provider | `AccelaeroProcessException` thrown |

---

## Package Reference

```
com.accelaero.common.type.async
├── AsyncProcessor.java          // Main orchestrator
├── AsyncProcess.java            // Fire-and-forget task interface
├── AsyncDataProvider.java       // Data-fetching task (single value)
├── AsyncDataListProvider.java   // Data-fetching task (list value)
└── AsyncExecutor.java           // Spring @Async single-task wrapper
```

---

*Copyright (C) ACCELaero — Information Systems Associates (pvt) Ltd. Internal use only.*
