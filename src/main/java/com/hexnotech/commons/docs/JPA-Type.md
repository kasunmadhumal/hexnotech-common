# JPA Type Utilities — `com.accelaero.common.jpa.type`

> Part of the `aero-ops-common` module — a reusable core library shared across the AeroOPS platform.

---

## Overview

The `com.accelaero.common.jpa.type` package provides a set of generic, reusable JPA utilities that address three recurring patterns in data-access layers:

| Component | Responsibility |
|---|---|
| [`EntityGraphProcessor`](#1-entitygraphprocessor) | Initializes Hibernate lazy-loaded associations based on `@NamedEntityGraph` annotations |
| [`JpaPredicateBuilder`](#2-jpapredicatebuilder) | Fluent builder for assembling JPA `CriteriaQuery` predicates |
| [`TupleProcessor`](#3-tupleprocessor) | Maps JPA `Tuple` query results to typed POJOs via reflection |
| [`PredicationResolver`](#4-predicationresolver) | Contract for bridging a request object to a JPA `Specification` |

---

## 1. `EntityGraphProcessor<T>`

### Purpose

Programmatically walks a `@NamedEntityGraph` (or `@NamedEntityGraphs`) annotation declared on a JPA entity and eagerly initializes all referenced Hibernate proxies and lazy collections — without requiring an active persistence context.

This is particularly useful when entities are detached from the session (e.g. after a transaction boundary) and downstream service logic still needs to access lazy associations safely.

### How It Works

1. Reads `@NamedEntityGraph` / `@NamedEntityGraphs` from the entity's class annotations by matching the provided `graphName`.
2. Iterates over each `@NamedAttributeNode` in the graph.
3. For **collections**: calls `Hibernate.initialize(collection)` and recurses into each element.
4. For **single entities**: calls `Hibernate.initialize(entity)`, unproxies `HibernateProxy` instances (replacing the proxy reference on the parent via setter), then recurses into any declared `subgraph`.
5. Subgraph traversal is recursive — deeply nested associations are initialized in one call.

### API

```java
public class EntityGraphProcessor<T> {
    public void process(T entity, String graphName);
}
```

| Parameter | Description |
|---|---|
| `entity` | The root JPA entity to process |
| `graphName` | The name of the `@NamedEntityGraph` to apply |

### Usage Example

```java
// Entity declaration
@NamedEntityGraphs({
    @NamedEntityGraph(
        name = "flight.full",
        attributeNodes = {
            @NamedAttributeNode(value = "flightDelays", subgraph = "flightDelay.detail"),
            @NamedAttributeNode("divertedFlights")
        },
        subgraphs = {
            @NamedSubgraph(
                name = "flightDelay.detail",
                attributeNodes = { @NamedAttributeNode("delay") }
            )
        }
    )
})
@Entity
public class Flight { ... }

// Usage in a service
EntityGraphProcessor<Flight> processor = new EntityGraphProcessor<>();
processor.process(flight, "flight.full");
// flight.flightDelays, each delay's nested 'delay' association,
// and flight.divertedFlights are now initialized.
```

### Behaviour Details

| Scenario | Behaviour |
|---|---|
| Field value is `null` | Skipped silently — no exception thrown |
| Field is a `Collection` | `Hibernate.initialize` called on the collection; each non-null element processed recursively |
| Field is a `HibernateProxy` | Unproxied; parent reference updated via setter so callers hold the real instance |
| No matching graph found | No-op — method returns without error |
| Property access failure | Throws `AccelaeroProcessException` |

---

## 2. `JpaPredicateBuilder<E>`

### Purpose

A fluent, type-safe builder that accumulates `Predicate` instances for use in JPA `CriteriaQuery` construction. Eliminates the boilerplate of manually managing a `List<Predicate>` and converting it to an array.

### API

```java
public class JpaPredicateBuilder<E> {

    // Factory
    public static <E> JpaPredicateBuilder<E> of(CriteriaBuilder cb, Root<E> root);

    // Predicate methods (all return `this` for chaining)
    public JpaPredicateBuilder<E> equal(String fieldName, Object value);
    public <Y extends Comparable<? super Y>> JpaPredicateBuilder<E> between(String fieldName, Y from, Y to);
    public JpaPredicateBuilder<E> inOrAll(String fieldName, Collection<?> values);
    public JpaPredicateBuilder<E> inOrNone(String fieldName, Collection<?> values);
    public JpaPredicateBuilder<E> add(Predicate... predicates);

    // Terminal methods
    public List<Predicate> generate();
    public Predicate[] generateArray();
}
```

### Method Reference

| Method | Description |
|---|---|
| `equal(field, value)` | Adds an equality predicate (`field = value`) |
| `between(field, from, to)` | Adds a range predicate (`field BETWEEN from AND to`) |
| `inOrAll(field, values)` | Adds an `IN` predicate **only if** `values` is non-empty; skips predicate entirely otherwise (matches all rows) |
| `inOrNone(field, values)` | Adds an `IN` predicate if `values` is non-empty; adds a **disjunction** (`1=0`) otherwise (matches no rows) |
| `add(predicates...)` | Appends one or more externally constructed predicates |
| `generate()` | Returns the accumulated predicates as a `List<Predicate>` |
| `generateArray()` | Returns the accumulated predicates as a `Predicate[]` (ready for `cb.and(...)`) |

> **`inOrAll` vs `inOrNone`:** Use `inOrAll` when an empty filter should return all records (no restriction). Use `inOrNone` when an empty filter should return no records (hard exclusion).

### Usage Example

```java
public List<Flight> findFlights(List<String> statusCodes, LocalDate from, LocalDate to,
                                 CriteriaBuilder cb, Root<Flight> root) {
    Predicate[] predicates = JpaPredicateBuilder.<Flight>of(cb, root)
        .inOrAll("statusCode", statusCodes)
        .between("scheduledDate", from, to)
        .equal("activeFlag", true)
        .generateArray();

    criteriaQuery.where(cb.and(predicates));
    // ...
}
```

---

## 3. `TupleProcessor<T>`

### Purpose

Maps the results of a JPA `Tuple` (native or criteria) query to a typed POJO. Avoids the boilerplate of manually calling `tuple.get(...)` for each field by using reflection to match tuple aliases to POJO field names.

### API

```java
public class TupleProcessor<T> {

    // Factory
    public static <T> TupleProcessor<T> by(Class<T> clazz);

    // Single tuple
    public T process(Tuple tuple);
    public T process(Tuple tuple, BiConsumer<T, Tuple> customMapper);

    // Collection of tuples
    public List<T> process(Collection<Tuple> tuples);
    public List<T> process(Collection<Tuple> tuples, BiConsumer<T, Tuple> customMapper);
}
```

### Mapping Convention

Tuple aliases are matched to POJO field names using **lowercase comparison**:

- Tuple alias `"flightNumber"` → lowercased to `"flightnumber"` → matched against `field.getName().toLowerCase()`
- The POJO must have a **no-arg constructor** (default or explicit).
- Fields that have no matching alias in the tuple are left at their default value — no exception is thrown.

### Custom Mapper

The `BiConsumer<T, Tuple> customMapper` overload allows handling of fields that cannot be mapped automatically (e.g. type conversions, joined alias names, conditional logic):

```java
TupleProcessor.<FlightSummaryDTO>by(FlightSummaryDTO.class)
    .process(tuples, (dto, tuple) -> {
        dto.setAirlineName(tuple.get("airlineName", String.class));
        dto.setDelayMinutes(tuple.get("totalDelay", Integer.class));
    });
```

### Usage Example

```java
// POJO
public class FlightSummaryDTO {
    private String flightnumber;   // alias must match lowercase
    private String statuscode;
    private LocalDate scheduleddate;
    // no-arg constructor + getters/setters
}

// Repository / query
List<Tuple> tuples = entityManager.createNativeQuery(sql, Tuple.class).getResultList();

List<FlightSummaryDTO> results = TupleProcessor.by(FlightSummaryDTO.class)
                                               .process(tuples);
```

### Error Handling

| Scenario | Behaviour |
|---|---|
| Tuple alias not found for a field | Silently skipped (`IllegalArgumentException` caught internally) |
| Field value is `null` | Field left at default; `null` is not applied |
| POJO instantiation fails | Throws `AccelaeroProcessException` |

---

## 4. `PredicationResolver<Req, E>`

### Purpose

A functional interface that establishes a standard contract for translating a typed request object (filter/search criteria) into a JPA `Specification<E>`. Implementations are typically co-located with their repository or query service.

### API

```java
public interface PredicationResolver<Req, E extends BaseEntity<?>> {

    Predicate predicate(Req req, SearchFilter searchFilter,
                        Root<E> root, CriteriaQuery<?> criteriaQuery,
                        CriteriaBuilder criteriaBuilder);

    default Specification<E> specification(Req req, SearchFilter searchFilter);
}
```

| Type Parameter | Constraint | Description |
|---|---|---|
| `Req` | None | The request/filter DTO carrying search criteria |
| `E` | `extends BaseEntity<?>` | The JPA entity being queried |

### Default Method: `specification`

The default `specification(req, searchFilter)` method wraps the abstract `predicate(...)` call into a `Specification<E>` lambda — so callers only need to invoke `specification(...)` and pass it to a Spring Data JPA `JpaSpecificationExecutor`.

### Usage Example

```java
// Implementation
@Component
public class FlightPredicationResolver
        implements PredicationResolver<FlightSearchRequest, Flight> {

    @Override
    public Predicate predicate(FlightSearchRequest req, SearchFilter filter,
                               Root<Flight> root, CriteriaQuery<?> query,
                               CriteriaBuilder cb) {
        return JpaPredicateBuilder.<Flight>of(cb, root)
            .inOrAll("statusCode", req.getStatusCodes())
            .equal("flightNumber", req.getFlightNumber())
            .between("scheduledDate", filter.getDateFrom(), filter.getDateTo())
            .generateArray()[0]; // or wrap in cb.and(...)
    }
}

// Repository call
Specification<Flight> spec = resolver.specification(request, searchFilter);
List<Flight> flights = flightRepository.findAll(spec);
```

---

## Component Interaction

The four components are designed to be composed together in a typical query flow:

```
HTTP Request / Service call
        |
        v
  PredicationResolver          <-- translates request DTO to a JPA Specification
        |
        v
  JpaPredicateBuilder          <-- assembles individual Predicates fluently
        |
        v
  CriteriaQuery execution
        |
        v
  TupleProcessor               <-- maps Tuple results to response DTOs
        |
        v
  EntityGraphProcessor         <-- initializes lazy associations on detached entities
        |
        v
  Response
```

---

## Package Reference

```
com.accelaero.common.jpa.type
├── EntityGraphProcessor.java     // Hibernate proxy/lazy-init processor
├── JpaPredicateBuilder.java      // Fluent predicate builder
├── TupleProcessor.java           // Tuple-to-POJO mapper
└── PredicationResolver.java      // Specification contract interface
```

---

*Copyright (C) ACCELaero — Information Systems Associates (pvt) Ltd. Internal use only.*
