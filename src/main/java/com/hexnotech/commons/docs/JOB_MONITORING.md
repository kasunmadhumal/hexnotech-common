# Job Monitoring — `@MonitorJob`

Automatically tracks scheduler job execution history — start time, end time, and status — with zero boilerplate.

---

## Setup

### Step 1 — Enable in your application

```java
@SpringBootApplication
@EnableJobMonitoring   // ← add this
public class YourServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourServiceApplication.class, args);
    }
}
```

### Step 2 — Add DB migration

Create a Flyway migration file in your service:

```
src/main/resources/db/migration/V{NEXT}__create_job_execution_monitor.sql
```

> Replace `{YOUR_SCHEMA}` with your service's schema (e.g. `aero_ops`, `aero_mart`).

```sql
CREATE TABLE IF NOT EXISTS {YOUR_SCHEMA}.t_job_execution_monitor
(
    id               BIGSERIAL    PRIMARY KEY,
    job_name         VARCHAR(100) NOT NULL,
    execution_status VARCHAR(20)  NOT NULL,
    start_time       TIMESTAMP    NOT NULL,
    end_time         TIMESTAMP    NOT NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

---

## Usage

Annotate any `@Scheduled` method with `@MonitorJob`:

```java
@Scheduled(cron = "0 * * * * *")
@MonitorJob(jobName = JobNames.ACARS_MESSAGE_QUEUE_CONSUMER)
public void processAcarsMessages() {
    // your logic — no monitoring code needed
}
```

```java
public final class JobNames {
    public static final String ACARS_MESSAGE_QUEUE_CONSUMER = "ACARS_MESSAGE_QUEUE_CONSUMER";
    public static final String FLIGHT_DELAY_SYNC             = "FLIGHT_DELAY_SYNC";
}
```

---

## DB Result

Every job execution saves one record:

| id | job_name | execution_status | start_time | end_time | created_at |
|----|----------|-----------------|------------|----------|------------|
| 1 | ACARS_MESSAGE_QUEUE_CONSUMER | SUCCESS | 2026-03-10 05:00:00 | 2026-03-10 05:00:03 | 2026-03-10 05:00:03 |
| 2 | FLIGHT_DELAY_SYNC | FAILED | 2026-03-10 05:01:00 | 2026-03-10 05:01:01 | 2026-03-10 05:01:01 |

---

## Notes

- `@MonitorJob` must be on a `public` method of a Spring-managed bean