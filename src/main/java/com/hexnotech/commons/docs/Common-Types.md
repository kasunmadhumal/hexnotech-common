# Common Types — `com.accelaero.common.type`

> Part of the `aero-ops-common` module — a reusable core library shared across the AeroOPS platform.

---

## Classes

| Class | Package | Purpose |
|---|---|---|
| [`ComparisonProvider<T>`](#comparisonprovidert) | `type.comparison` | Detect which fields changed between two versions of an object |
| [`AccelaeroValidator`](#accelaerovalidator) | `type.generic` | Fluent validation chain that collects all errors before throwing |
| [`MergeList<T, I>`](#mergelistt-i) | `type.generic` | Categorise a new list against an original into added / updated / removed |
| [`GroupedList<T, I>`](#groupedlistt-i) | `type.generic` | Group a flat list by key and de-duplicate with optional accumulation |
| [`Range<E>`](#rangee) | `type.generic` | Immutable from/to boundary pair with overlap detection |
| [`RangeContract<E>`](#rangecontracte) | `type.generic` | Interface for entities that carry their own range |
| [`TriFunction<X,Y,Z,R>`](#trifunctionxyzr) | `type.generic` | Three-argument functional interface |
| [`SimpleProcess`](#simpleprocess) | `type.generic` | Zero-argument executable unit interface |
| [`ListSupplier<T>`](#listsuppliert) | `type.generic` | Typed alias for `Supplier<List<T>>` |
| [`JAXBContextProvider<T>`](#jaxbcontextprovidert) | `type` | Parse XML streams into typed Java objects |

---

## `ComparisonProvider<T>`

### What it does

Given an **existing** object and an **updated** object of the same type, tells you exactly which fields changed and what their new values are.

The result is a `Map<String, String>` where:
- **key** = field name (or dot-path for nested JSON fields, e.g. `"address.city"`)
- **value** = new value as a string (`"null"` if the field was cleared)

Only changed fields appear in the map. Unchanged fields are not included.

### API

```java
// Factory
ComparisonProvider.by(T existing, T updated)
ComparisonProvider.by(Optional<T> existing, T updated)

// Comparison methods (chainable)
.compare("fieldName", getter)           // flat field comparison
.compareJson("fieldName", getter)       // deep JSON diff on a nested object

// Result
.getUpdatedFields()                     // returns Map<String, String>
```

### Usage Example

```java
User existing = new User("John", 30, new Address("123 Main St", "New York"));
User updated  = new User("John", 31, new Address("123 Main St", "Los Angeles"));

Map<String, String> changes = ComparisonProvider.by(existing, updated)
    .compare("name",         User::getName)
    .compare("age",          User::getAge)
    .compareJson("address",  User::getAddress)
    .getUpdatedFields();

// Result: { "age" -> "31", "address.city" -> "\"Los Angeles\"" }
// "name" is absent — it did not change.
```

### Behaviour

| Scenario | Result |
|---|---|
| Field value unchanged | Not included in the result map |
| Field changed to `null` | Recorded as `"null"` |
| Existing object is `null` | All non-null fields on the updated object are treated as new |
| `compareJson` on a nested object | Reports only changed sub-fields as dot-path keys (`address.city`) |
| `compareJson` on an array | Reports changed elements using indexed paths (`items[0].code`) |

---

## `AccelaeroValidator`

### What it does

A fluent validation chain that **collects all errors first**, then throws a single `AccelaeroValidationException` with all messages joined by `, ` — instead of stopping at the first failure.

### API

```java
AccelaeroValidator.of()
    .validateNotNull(value, fieldName)
    .validateStringField(value, fieldName)
    ...
    .validate();  // throws AccelaeroValidationException if any errors were collected
```

### Available Validations

| Method | Validates |
|---|---|
| `validateNotNull(value, fieldName)` | Value is not null / empty |
| `validateStringField(value, fieldName)` | String is not blank |
| `validateLength(value, fieldName, exact)` | String is exactly N characters |
| `validateMinLength(value, fieldName, min)` | String is at least N characters |
| `validateMaxLength(value, fieldName, max)` | String is at most N characters |
| `validateLength(value, fieldName, min, max)` | String length is between min and max |
| `validateForNumber(value, fieldName)` | String parses as a valid number |
| `validatePositive(value, fieldName)` | Number is zero or positive |
| `validateRange(min, max, fieldName)` | `min` is strictly less than `max` (valid range definition) |
| `validateRange(value, min, max, fieldName)` | Value falls within [min, max] |
| `validateList(list, fieldName)` | List is not null / empty |
| `validateNonEmptyList(list, fieldName)` | List must be empty (use when a list should not contain items) |
| `addValidationError(condition, message)` | Custom conditional error |
| `validate(condition, message)` *(static)* | Quick one-off inline check |

### Usage Example

```java
AccelaeroValidator.of()
    .validateNotNull(request.getFlightId(), "Flight ID")
    .validateStringField(request.getFlightNumber(), "Flight Number")
    .validateLength(request.getFlightNumber(), "Flight Number", 3, 6)
    .validateRange(request.getFromDate(), request.getToDate(), "Date range")
    .validate();
```

If two fields fail, the exception message will be: `"Flight Number required, Date range is invalid"`.

---

## `MergeList<T, I>`

### What it does

Compares an **original list** (e.g. from the database) against a **new list** (e.g. from a request) and categorises each item as:
- **Added** — new item with no matching ID in the original
- **Updated** — item whose ID exists in both lists
- **Removed** — item in the original whose ID is absent from the new list

Identity is determined by a key function you provide (typically `Entity::getId`).

### API

```java
// Factory
MergeList.of(originalItems, identifierFunction)
MergeList.of(originalItems, identifierFunction, idValidationPredicate)

// Processing (chainable)
.process(newItems)                        // categorise items

// Merge updated items back onto original entities
.merge(BiConsumer<original, updated>)     // returns final list (updated + added)

// Access categories
.getAddedItems()
.getUpdatedItems()
.getRemovedItems()
.getOriginalItems()
```

### Usage Example

```java
MergeList<FlightDelay, Long> merge = MergeList.of(
    existingDelays,       // original list from DB
    FlightDelay::getId    // identity key
);

merge.process(incomingDelays);

// Copy fields from new items onto the tracked original entities
List<FlightDelay> toSave = merge.merge((existing, updated) -> {
    existing.setDuration(updated.getDuration());
    existing.setCode(updated.getCode());
});

Collection<FlightDelay> toDelete = merge.getRemovedItems();
```

> `merge()` returns updated entities + newly added items combined — this is the list you typically persist.
> `getRemovedItems()` gives you entities to delete from the database.

---

## `GroupedList<T, I>`

### What it does

Groups a flat list by a key, then collapses each group into a single "master" element — optionally folding data from duplicates into the master via an accumulator.

Useful when query results contain repeating rows for the same entity (e.g. a join that multiplies rows), and you need one clean record per group.

### API

```java
// Factory
GroupedList.of(items, identifierFunction)
GroupedList.of(items, identifierFunction, masterElementFilter)  // custom master selection
GroupedList.of(groupedMap)

// Result
.getUniques()                              // one master per group, duplicates discarded
.getUniques(BiConsumer<master, duplicate>) // one master per group, duplicates folded in
```

By default, the **first element** in each group is the master.

### Usage Example

```java
// Discard duplicates, keep first per flight number
List<Flight> unique = GroupedList.of(rawFlights, Flight::getFlightNumber)
                                 .getUniques();

// Merge each duplicate's delays into the master flight object
List<Flight> merged = GroupedList.of(rawFlights, Flight::getFlightNumber)
    .getUniques((master, duplicate) -> {
        master.getDelays().addAll(duplicate.getDelays());
    });
```

---

## `Range<E>`

### What it does

An immutable value object holding a `from` / `to` boundary pair for any `Comparable` type (dates, times, numbers, etc.). Validates that `from <= to` at construction and provides overlap detection between two ranges.

### API

```java
// Factory
Range.of(from, to)             // throws AccelaeroValidationException if from > to

// Overlap detection
.isOverlap(Range<E> other)
.isOverlap(RangeContract<E> other)
```

### Usage Example

```java
Range<LocalDate> period = Range.of(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));

boolean overlaps = period.isOverlap(Range.of(
    LocalDate.of(2025, 6, 1),
    LocalDate.of(2026, 1, 1)
)); // true
```

Throws `AccelaeroValidationException` if `from > to` or either value is `null`.

---

## `RangeContract<E>`

### What it does

An interface for entities or DTOs that carry their own range. Implement it to get a free `toRange()` default method that converts the entity's boundaries into a `Range<E>`.

### API

```java
public interface RangeContract<E extends Comparable<? super E>> {
    E getFrom();
    E getTo();
    default Range<E> toRange();   // built-in — no implementation needed
}
```

### Usage Example

```java
public class OperatingWindow implements RangeContract<LocalDateTime> {
    public LocalDateTime getFrom() { return windowOpen; }
    public LocalDateTime getTo()   { return windowClose; }
}

Range<LocalDateTime> range = window.toRange();
boolean clash = range.isOverlap(anotherWindow.toRange());
```

---

## `TriFunction<X,Y,Z,R>`

### What it does

A three-argument functional interface — the natural extension of Java's `BiFunction` for cases where a lambda needs three inputs.

### API

```java
@FunctionalInterface
public interface TriFunction<X, Y, Z, R> {
    R apply(X x, Y y, Z z);
}
```

### Usage Example

```java
TriFunction<Flight, Crew, Gate, Turnaround> builder =
    (flight, crew, gate) -> new Turnaround(flight, crew, gate);

Turnaround t = builder.apply(flight, crew, gate);
```

---

## `SimpleProcess`

### What it does

A zero-argument, no-return functional interface for wrapping any block of logic as an executable unit. Primarily used with `AsyncExecutor` to submit background tasks.

### API

```java
@FunctionalInterface
public interface SimpleProcess {
    void execute();
}
```

### Usage Example

```java
SimpleProcess sendAlert = () -> notificationService.send(event);
asyncExecutor.executeAsync(sendAlert);
```

---

## `ListSupplier<T>`

### What it does

A typed alias for `Supplier<List<T>>`. Makes method signatures more readable when passing list-returning lambdas. Primarily used with `AsyncProcessor.getProviderByList(...)`.

### API

```java
public interface ListSupplier<T> extends Supplier<List<T>> {}
```

### Usage Example

```java
ListSupplier<Flight> flightSupplier = () -> flightRepo.findByDate(date);

AsyncDataListProvider<Flight> provider = asyncProcessor.getProviderByList(flightSupplier);
```

---

## `JAXBContextProvider<T>`

### What it does

A thin wrapper around JAXB that initialises the context and unmarshaller once and exposes a simple `toType(InputStream)` method to parse XML into a typed Java object.

### API

```java
// Factory
JAXBContextProvider.of(Class<T> clazz)

// Parse XML
.toType(InputStream stream)     // returns T

// Advanced access
.getUnmarshaller()              // returns the underlying Unmarshaller
```

### Usage Example

```java
JAXBContextProvider<FlightPlan> provider = JAXBContextProvider.of(FlightPlan.class);

InputStream xmlStream = fileService.open("flight-plan.xml");
FlightPlan plan = provider.toType(xmlStream);
```

Throws `AccelaeroProcessException` if the JAXB context cannot be created or if the XML fails to parse.

---

## Package Reference

```
com.accelaero.common.type
│
├── comparison/
│   └── ComparisonProvider.java       // Field-level diff between two object versions
│
├── generic/
│   ├── AccelaeroValidator.java       // Fluent validation chain
│   ├── MergeList.java                // Add/update/remove diff for two lists
│   ├── GroupedList.java              // Group-and-deduplicate a flat list
│   ├── Range.java                    // Immutable from/to value object with overlap check
│   ├── RangeContract.java            // Interface for entities that carry a range
│   ├── TriFunction.java              // 3-argument functional interface
│   ├── SimpleProcess.java            // No-arg executable unit interface
│   └── ListSupplier.java             // Typed alias for Supplier<List<T>>
│
└── JAXBContextProvider.java          // XML-to-typed-object parser (JAXB wrapper)
```

---

*Copyright (C) ACCELaero — Information Systems Associates (pvt) Ltd. Internal use only.*
