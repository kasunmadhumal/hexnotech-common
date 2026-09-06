# Accelaero Logger

Method level entry/exit/error logging via the `@AccelaeroLogger` annotation. It answers:
**What was called, with what arguments, how long did it take, and what went wrong?**

Instead of hand writing `log.info("start ...")` / `log.info("end ...")` pairs and try/catch blocks
in every method, annotate the method and the `AccelaeroLoggerAspect` does it around the invocation.

## Enable in Your Application

Add `@EnableAccelaeroLogger` to your application class or any `@Configuration` class. This imports
`AccelaeroLoggerAspect` as a Spring bean. Without this annotation nothing is logged even if the
library is on the classpath.

```java
@SpringBootApplication
@EnableAccelaeroLogger
@ComponentScan("com.accelaero.*")
public class ManagerServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ManagerServiceApplication.class, args);
    }
}
```

## `@AccelaeroLogger` Annotation

Target: **methods only** (`@Target(ElementType.METHOD)`).

### Attributes

| Attribute | Required | Default | Description |
|---|---|---|---|
| `value` | No | `""` | Free text tag appended to every log line as `[tag]` — use it to group related logs (feature name, job name, flow id) |
| `onError` | No | `ErrorMode.THROW` | `THROW` — log the error and rethrow. `SILENT` — log the error and return `null` instead of propagating |

### Log Output

Three lines per invocation (the `END` and `ERROR` lines are mutually exclusive):

```
START  <ClassName>.<methodName> [tag] - params: [name=value, name=value]
END    <ClassName>.<methodName> [tag] - completed in <elapsed>ms
ERROR  <ClassName>.<methodName> [tag] - failed after <elapsed>ms - <ExceptionType>: <message>
```

`START` / `END` are logged at `INFO`, `ERROR` at `ERROR` with the full stack trace attached.

Parameters are rendered as `paramName=value` using the real Java parameter names (requires the
`-parameters` compiler flag, standard in Spring Boot). Where a name is unavailable it falls back to
`arg0`, `arg1`, ... Each value goes through `SafeProcessor`, so a `toString()` that throws produces
`<toString failed>` rather than breaking the call.

## Usage Examples

### Basic

```java
@AccelaeroLogger
public FlightResponse getFlight(String flightNumber) {
    return flightRepository.findByFlightNumber(flightNumber);
}
```

```
START FlightServiceImpl.getFlight - params: [flightNumber=G9101]
END FlightServiceImpl.getFlight - completed in 42ms
```

### With a Tag

```java
@AccelaeroLogger("FDDR")
public void sendFddrNotification(Long flightId, String recipient) { ... }
```

```
START FddrServiceImpl.sendFddrNotification [FDDR] - params: [flightId=101, recipient=ops@example.com]
END FddrServiceImpl.sendFddrNotification [FDDR] - completed in 310ms
```

### Swallowing Errors (`ErrorMode.SILENT`)

Use for non critical side effects (notifications, cache warm ups, best effort syncs) where a
failure must be logged but must not fail the caller:

```java
@AccelaeroLogger(value = "AOG-NOTIFICATION", onError = ErrorMode.SILENT)
public NotificationResult notifyAog(AogEvent event) {
    return notificationClient.send(event);
}
```

On failure the error is logged with its stack trace and the method returns `null` — callers must
be null safe. On a `void` method the returned `null` is simply discarded.

## Behaviour Notes

- **Spring proxy semantics apply.** The aspect only fires on calls that go through the Spring
  proxy. A `private` method, or a call to another method on the same bean via `this`, is not
  intercepted.
- **Arguments are logged in full.** Do not annotate methods whose arguments carry passwords,
  tokens, card data or other sensitive values — everything is written to the log via
  `String.valueOf(...)`.
- **Large payloads are logged in full too.** Annotating a method that takes a big proto or
  collection produces a large log line; prefer a wrapper method with narrow arguments.
- **The elapsed time includes everything downstream** of the annotated method, the aspect measures
  wall clock around `joinPoint.proceed()`.

## Common Mistakes

| Mistake | Symptom | Fix |
|---|---|---|
| Missing `@EnableAccelaeroLogger` | No log lines appear | Add it to the application class or a `@Configuration` class |
| Annotating a `private` method or an internal `this.` call | No log lines for that method | Annotate a method invoked through the Spring proxy |
| Using `ErrorMode.SILENT` on a method whose result is dereferenced | `NullPointerException` in the caller instead of the original exception | Keep `ErrorMode.THROW`, or handle `null` in the caller |
| Annotating a method that receives credentials or PII | Sensitive data written to logs | Remove the annotation, or move logging to a method with safe arguments |
