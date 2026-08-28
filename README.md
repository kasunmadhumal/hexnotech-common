# hexnotech-commons

Shared library of **annotations**, **base classes**, **response wrappers**, **exception handling**, and **utility functions** for all Hexnotech backend services.

---

## Quick Start

### 1. Publish to your local Maven repo

```bash
cd hexnotech-commons
./gradlew publishToMavenLocal
```

### 2. Add to any Hexnotech project

In the consumer project's `build.gradle`:

```groovy
repositories {
    mavenLocal()        // ← add this before mavenCentral
    mavenCentral()
}

dependencies {
    implementation 'com.hexnotech:hexnotech-commons:1.0.0'
}
```

---

## What's Inside

### Annotations

| Annotation | Package | Purpose |
|---|---|---|
| `@Auditable` | `annotation.audit` | Marks a JPA entity for audit tracking (works with `BaseEntity`) |
| `@PhoneNumber` | `annotation.validation` | Validates international phone number format |
| `@NationalId` | `annotation.validation` | Validates Bangladeshi NID (10 / 13 / 17 digits) |
| `@ApiVersion` | `annotation.web` | Documents API version on controllers / methods |

### Base Classes

| Class | Purpose |
|---|---|
| `BaseEntity` | Abstract `@MappedSuperclass` with `id`, `createdAt`, `updatedAt` via JPA lifecycle |
| `BaseService<T,ID>` | Generic CRUD service interface |

### Response Wrappers

| Class | Purpose |
|---|---|
| `ApiResponse<T>` | Standard `{ success, message, data, timestamp }` envelope |
| `PagedResponse<T>` | Wraps Spring Data `Page<T>` for paginated list endpoints |

### Exception Handling

| Class | HTTP Status | Purpose |
|---|---|---|
| `ResourceNotFoundException` | 404 | Resource not found by ID |
| `BusinessException` | 422 | Business rule / domain invariant violated |
| `BaseException` | 400 | Abstract base — extend for custom exceptions |
| `GlobalExceptionHandler` | — | `@RestControllerAdvice` — handles all of the above + validation |

### Utilities

| Class | Key Methods |
|---|---|
| `DateTimeUtils` | `nowDhaka()`, `utcToDhaka()`, `toDisplayString()`, `startOfDay()`, `endOfDay()` |
| `StringUtils` | `isBlank()`, `mask()`, `toSlug()`, `toCamelCase()`, `truncate()` |
| `PaginationUtils` | `of(page, size, sortBy, direction)`, `ofDefault(page)` |
| `JsonUtils` | `toJson()`, `fromJson()`, `toMap()`, `convert()` |

---

## Usage Examples

### Extend BaseEntity

```java
@Auditable
@Entity
@Table(name = "bookings")
public class Booking extends BaseEntity {
    private String reference;
}
```

### Use ApiResponse in a controller

```java
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<BookingDto>> get(@PathVariable Long id) {
    BookingDto dto = bookingService.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Booking", id));
    return ResponseEntity.ok(ApiResponse.success(dto));
}
```

### Paginated list

```java
@GetMapping
public ResponseEntity<ApiResponse<PagedResponse<BookingDto>>> list(
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "20") int size) {

    Pageable pageable = PaginationUtils.of(page, size, "createdAt", Sort.Direction.DESC);
    Page<BookingDto> result = bookingService.findAll(pageable);
    return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(result)));
}
```

### Import GlobalExceptionHandler

In your Spring Boot app, if `com.hexnotech.commons` is **not** under your base package, import explicitly:

```java
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class AnyBookingApplication { ... }
```

---

## Publishing to GitHub Packages (future)

Uncomment the `GitHubPackages` block in `build.gradle` and set:

```bash
export GITHUB_ACTOR=your-username
export GITHUB_TOKEN=your-pat-token
./gradlew publish
```

---

## Tech Stack

- Java 21
- Gradle 9.5.1 (`java-library` + `maven-publish`)
- Spring Web MVC, Spring Data Commons, Jakarta Persistence, Jakarta Validation
- Jackson (with JavaTimeModule), Lombok
