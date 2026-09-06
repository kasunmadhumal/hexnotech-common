# Utility Classes — `com.accelaero.common.util`

> Part of the `aero-ops-common` module — a reusable core library shared across the AeroOPS platform.

---

## Classes

| Class | Purpose |
|---|---|
| [`AccelaeroConstant`](#accelaeroConstant) | Shared string symbols and date-time format pattern constants |
| [`AccelaeroDateTimeUtil`](#accelaeroDateTimeUtil) | Parse `LocalDate` / `LocalTime` from strings; convert time strings to minutes |
| [`AccelaeroReflectionUtil`](#accelaeroReflectionUtil) | Inspect static fields and annotated instance fields via reflection |
| [`AccelaeroUtil`](#accelaeroUtil) | General-purpose null-safety, string, collection, map, JSON, and object helpers |
| [`OpsExtensions`](#opsExtensions) | Safe map-lookup with a mapped fallback value |
| [`PredicateUtil`](#predicateUtil) | JPA `Specification` and `Predicate` factory methods for criteria queries |
| [`ValidatorUtil`](#validatorUtil) | Programmatic JSR-303 Bean Validation runner |

---

## `AccelaeroConstant`

### What it does

A hierarchy of nested `@UtilityClass` constants to avoid hardcoded strings scattered across the codebase.

| Inner class | Contents |
|---|---|
| `AccelaeroConstant.Symbol` | Common punctuation constants: `EMPTY`, `COMMA`, `DOT`, `COLON`, `DASH`, `SLASH` |
| `AccelaeroConstant.DateTimeFormat` | Named date/time format patterns used throughout the platform |

### DateTimeFormat patterns

| Constant | Pattern | Example |
|---|---|---|
| `YYYY_MM_DD_HH_MM_SS` | `yyyy-MM-dd HH:mm:ss` | `2025-06-15 14:30:00` |
| `DD_MM_YYYY_DASH` | `dd-MM-yyyy` | `15-06-2025` |
| `YYYY_MM_DD` | `yyyy-MM-dd` | `2025-06-15` |
| `HH_MM` | `HH:mm` | `14:30` |
| `HH_MM_SS` | `HH:mm:ss` | `14:30:00` |
| `DD_MM_YYYY_SLASH` | `dd/MM/yyyy` | `15/06/2025` |

### Usage Example

```java
String delimiter = AccelaeroConstant.Symbol.DASH;
String pattern   = AccelaeroConstant.DateTimeFormat.YYYY_MM_DD;
```

---

## `AccelaeroDateTimeUtil`

### What it does

Parses date and time strings into Java 8 time objects (`LocalDate`, `LocalTime`) with flexible overloads — returning either an `Optional` (safe parse) or throwing `AccelaeroValidationException` (strict parse). Also converts `HH:mm` strings to total minutes.

### Capabilities

| Area | Description |
|---|---|
| **LocalDate — safe parse** | `parseDate(value)` / `parseUIDate(value)` — returns `Optional.empty()` on failure |
| **LocalDate — strict parse** | `parseDateOrThrow(...)` / `parseCommonDateOrThrow(...)` — throws `AccelaeroValidationException` with a field-aware message |
| **LocalTime — safe parse** | `parseTime(value)` / `parseTime(value, pattern)` — returns `Optional.empty()` on failure |
| **LocalTime — strict parse** | `parseTimeOrThrow(...)` / `parseCommonTimeOrThrow(...)` — throws `AccelaeroValidationException` |
| **Time conversion** | `parseHhMmToMinutes(String)` — converts `"HH:mm"` or `"HH"` to total minutes as `int` |

All strict overloads accept a `Supplier<String>` for the error message, enabling dynamic, field-aware messages without eager string construction.

### Usage Example

```java
// Safe parse — returns Optional
Optional<LocalDate> date = AccelaeroDateTimeUtil.parseDate("2025-06-15");

// Strict parse — throws on invalid input
LocalDate departure = AccelaeroDateTimeUtil.parseCommonDateOrThrow(request.getDepartureDate(), "Departure Date");

// Time to minutes
int minutes = AccelaeroDateTimeUtil.parseHhMmToMinutes("02:30"); // returns 150
```

---

## `AccelaeroReflectionUtil`

### What it does

A set of reflection helpers to inspect class fields without boilerplate. Two main capabilities:

1. **Static field value extraction** — collect values of static fields from a class (optionally filtered by type or a custom predicate).
2. **Annotated instance field mapping** — find instance fields carrying a specific annotation and build various `Map` representations of their names and values.

### Capabilities

| Method group | Returns | Use case |
|---|---|---|
| `getStaticFieldValues(clazz)` | `Set<Object>` | All static field values |
| `getStringStaticFieldValues(clazz)` | `Set<String>` | Only `String` static field values |
| `getStaticFieldsValues(clazz, fieldFilter)` | `Set<R>` | Static values filtered by a custom predicate |
| `getFieldsValueMap(instance, annotationClass)` | `Map<Field, Object>` | Annotated fields → their runtime values |
| `getFieldsNameValueMap(instance, annotationClass)` | `Map<String, Object>` | Field names → their runtime values |
| `getFieldsNameTypeMap(instance, annotationClass)` | `Map<String, Pair<Type, Object>>` | Field names → (generic type, runtime value) pairs |
| `getFields(clazz, annotationClass)` | `List<Field>` | All fields carrying the annotation (made accessible) |

### Usage Example

```java
// Collect all String constants from a constants class
Set<String> codes = AccelaeroReflectionUtil.getStringStaticFieldValues(StatusCode.class);

// Map annotated field names to their values on a request object
Map<String, Object> fieldValues = AccelaeroReflectionUtil.getFieldsNameValueMap(request, MyAnnotation.class);
```

---

## `AccelaeroUtil`

### What it does

The primary general-purpose utility class in the platform. Covers null-safety wrappers, string manipulation, collection and map operations, stream collectors, object helpers, JSON serialization, and PDF generation.

### Capabilities by area

#### Null-safety (`nvl`)

Null-safe getters for primitives and objects that return a default instead of throwing `NullPointerException`.

```java
long   value = AccelaeroUtil.nvlLong(entity.getCount());      // 0 if null
String code  = AccelaeroUtil.nvl(dto.getCode(), "N/A");       // "N/A" if null/empty
String id    = AccelaeroUtil.nvlUuid(entity.getId());         // "" if null
```

#### String utilities

| Feature | Methods |
|---|---|
| Emptiness checks | `isEmpty`, `isNotEmpty`, `isTrimmedEmpty` |
| Null-safe get / format | `nvl`, `nvlTrim`, `nvlEqualIgnoreCase` |
| Substring | `safeSubstring` — bounds-checked, never throws |
| Join | `concat(delimiter, values...)` — skips blank values |
| Case conversion | `toSnakeCase`, `toKebabCase` |
| Random string | `generateRandomString(length)` — alphanumeric, `SecureRandom` |

#### Number utilities

```java
AccelaeroUtil.isNumber("3.14");            // true
AccelaeroUtil.isPositive(value);           // null-safe positive check
AccelaeroUtil.withinRange(score, 0, 100);  // inclusive range check
```

#### Collection utilities

| Feature | Methods |
|---|---|
| Emptiness checks | `isEmpty`, `isNotEmpty` |
| Null-safe get | `nvlList`, `nvlToList`, `toStream` |
| Type-mapped list | `nvlToTypeList(list, mapper)` |
| Safe element access | `getFirstElement`, `getLastElement` |
| Split string to list | `splitToList`, `splitToIntList` |
| Distinct by key | `distinctByKey` — thread-safe, for use in `Stream.filter` |
| Merge by key | `mergeByKey` — merge items with duplicate keys using a `BinaryOperator` |
| Partition | `partition(list, predicate)` |
| Group by | `groupBy(items, keyExtractor, downstream)` |
| Enum conversion | `enumToStringList`, `stringToEnumList`, `getByValue` |

#### Map utilities

```java
AccelaeroUtil.nvlMap(map);                                 // empty map if null
AccelaeroUtil.mergeMaps(base, overlay, mergeWhenPresent);  // non-destructive merge
AccelaeroUtil.sortMapByValueAttribute(map, Value::getOrder);
AccelaeroUtil.createMapWithKeys(keys, key -> computeValue(key));
AccelaeroUtil.groupForList(Item::getCategory);             // Collector<T, ?, Map<K, List<T>>>
```

#### Object utilities

```java
// Conditional get with conversion
String label = AccelaeroUtil.testAndGet(code, Objects::nonNull, String::toUpperCase, "UNKNOWN");

// Safe execution — logs and swallows exception, returns Optional
Optional<Data> result = AccelaeroUtil.handleExecution(() -> riskyCall());

// Selective property copy — only the named fields are copied
AccelaeroUtil.copyProperties(source, target, Set.of("name", "status"));

// Conditional execution
AccelaeroUtil.consume(flag, () -> doSomething());
AccelaeroUtil.consumeNonNull(value, consumer::accept);
```

#### JSON utilities

```java
String  json = AccelaeroUtil.toJson(myObject);
MyDto   dto  = AccelaeroUtil.fromJson(json, MyDto.class);

Optional<MyDto> safe  = AccelaeroUtil.fromJson(json, new TypeReference<MyDto>() {});

// Extract a single field by JSON Pointer
Optional<String> field = AccelaeroUtil.getParsedMessageValue(json, "/flight/number");

// Extract multiple fields at once
Map<String, String> fields = AccelaeroUtil.getParsedMessageValueMap(json, List.of("/id", "/status"));
```

#### PDF generation

```java
byte[] pdf = AccelaeroUtil.generatePdfFromHtml("FlightReport", htmlContent);
// Throws AccelaeroInternalException on failure
```

---

## `OpsExtensions`

### What it does

A focused utility with a single method: safe map-lookup that applies a mapping function to the found value, returning a default if the key is absent.

### Usage Example

```java
// Returns carrier.getName() if "CARRIER_KEY" is in the map, otherwise "UNKNOWN"
String name = OpsExtensions.checkForDefault(carrierMap, "CARRIER_KEY", Carrier::getName, "UNKNOWN");
```

This avoids a null-check + `Optional.map` chain at the call site when reading from lookup maps.

---

## `PredicateUtil`

### What it does

A factory of raw JPA `Predicate` instances and `Specification` wrappers for common query patterns. Works alongside `JpaPredicateBuilder` (see [JPA-Type.md](JPA-Type.md)) — `PredicateUtil` provides lower-level, single-predicate helpers that can be composed freely in custom `PredicationResolver` implementations.

### Capabilities

| Method | Description |
|---|---|
| `defaultSpecification()` | Returns a `Specification` that matches all rows (`conjunction`) |
| `equalSpecification(field, value)` | `Specification` for a simple equality filter |
| `defaultTruePredicate()` | `PredicationResolver` that always returns conjunction |
| `equalPredicate(...)` | Raw `Predicate` for equality |
| `like(...)` | `LIKE` predicate with exact pattern |
| `preLike(...)` | `LIKE '%value'` — suffix match |
| `postLike(...)` | `LIKE 'value%'` — prefix match |
| `likeWildCard(...)` | `LIKE '%value%'` — contains match |
| `between(...)` | `BETWEEN from AND to` for any `Comparable` |
| `greaterThan(...)` / `greaterThanOrEqualTo(...)` | Comparison predicates |
| `lessThan(...)` / `lessThanOrEqualTo(...)` | Comparison predicates |
| `inOrAll(...)` | `IN (values)` or conjunction if the collection is empty |
| `inOrNone(...)` | `IN (values)` or disjunction (no rows) if the collection is empty |
| `modelActiveInTimeRangePredicate(...)` | Overlap check: model's `[startDate, endDate]` overlaps with a given time range |

### Usage Example

```java
// Inside a Specification lambda
Predicate flightNumber = PredicateUtil.postLike(cb, root, "flightNumber", req.getPrefix());
Predicate status       = PredicateUtil.inOrAll(cb, root, "statusCode", req.getStatusCodes());
Predicate dateRange    = PredicateUtil.between(cb, root, "scheduledDate", from, to);

return cb.and(flightNumber, status, dateRange);
```

---

## `ValidatorUtil`

### What it does

Runs programmatic JSR-303 (Jakarta Bean Validation) on any object using the default `ValidatorFactory`. Collects all constraint violations and throws a single `AccelaeroValidationException` with all messages joined by `, `.

Use this when you need to trigger Bean Validation outside of Spring's automatic validation pipeline — for example, on objects constructed internally before persistence.

### Usage Example

```java
FlightRequest request = buildRequest(incoming);
ValidatorUtil.validate(request);
// If @NotNull on flightNumber and @Size on remarks both fail:
// throws AccelaeroValidationException("must not be null, size must be between 0 and 255")
```

> Requires the validated class to carry standard JSR-303 annotations (`@NotNull`, `@NotBlank`, `@Size`, `@Min`, etc.).

---

## Package Reference

```
com.accelaero.common.util
├── AccelaeroConstant.java         // String symbols and date-time format patterns
├── AccelaeroDateTimeUtil.java     // LocalDate / LocalTime parsing helpers
├── AccelaeroReflectionUtil.java   // Static and annotated field inspection via reflection
├── AccelaeroUtil.java             // General-purpose null-safety, string, collection, map, JSON, PDF
├── OpsExtensions.java             // Safe map-lookup with mapped fallback
├── PredicateUtil.java             // JPA Specification and Predicate factory methods
└── ValidatorUtil.java             // Programmatic JSR-303 Bean Validation runner
```

---

*Copyright (C) ACCELaero — Information Systems Associates (pvt) Ltd. Internal use only.*
