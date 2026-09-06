# aero-ops-common

> **Reusable core library module** — provides the foundational architecture, base classes, contracts, and utilities used across all feature modules within the AeroOPS manager service.

---

## Table of Contents

1. [Module Overview](#1-module-overview)
2. [Architecture Overview](#2-architecture-overview)
3. [Core Components](#3-core-components)
   - [Service Layer](#31-service-layer)
   - [Repository Layer](#32-repository-layer)
   - [Mapping Layer](#33-mapping-layer)
   - [Validation Layer](#34-validation-layer)
   - [Entity Contracts](#35-entity-contracts)
   - [API Types](#36-api-types)
   - [Exception Hierarchy](#37-exception-hierarchy)
   - [Job Monitoring](#38-job-monitoring)
4. [Request Processing Flow](#4-request-processing-flow)
5. [Extension Guidelines](#5-extension-guidelines)
6. [Design Principles](#6-design-principles)
7. [Conceptual Example](#7-conceptual-example)

---

## 1. Module Overview

`aero-ops-common` is the **shared foundation** of the AeroOPS manager service. It contains no business domain logic. Instead, it defines the **architectural skeleton** — a set of generic base classes, interfaces, and contracts — that all feature modules extend and implement.

**Responsibilities of this module:**

- Define a consistent, layered request/response processing pipeline for gRPC-backed services.
- Enforce separation between the gRPC entry point, business service, and data repository.
- Provide reusable CRUD and search operations so feature modules contain only domain-specific logic.
- Standardise entity contracts, pagination, exception handling, and response wrapping.
- Provide opt-in job execution monitoring via AOP annotations.

This module is a **compile-time dependency** of every feature module. It contains no Spring Boot application context of its own.

---

## 2. Architecture Overview

The module enforces a strict three-layer architecture. Each layer is defined by an abstract class or interface in this module; concrete implementations live in the consuming feature modules.

```
┌──────────────────────────────────────────────────────────────────┐
│                      gRPC / Transport Layer                      │
│                                                                  │
│   BaseAccelaroGrpcService<SearchReq, Req, Res, E, I>             │
│   BaseAccelaroBinaryGrpcService<Req, Res, E, I>   (variant)      │
│   BaseWildcardGrpcService<Req, Res, E, I>         (variant)      │
│                                                                  │
│   Depends on: RequestValidator, BaseAccelaroMapper,              │
│               PredicationResolver, BaseAccelaroServiceContract   │
└─────────────────────────┬────────────────────────────────────────┘
                          │  delegates to
┌─────────────────────────▼────────────────────────────────────────┐
│                       Business Service Layer                     │
│                                                                  │
│   BaseAccelaeroService<E, I>                                     │
│   implements BaseAccelaroServiceContract<E, I>                   │
│                                                                  │
│   Manages transactions, entity lifecycle hooks                   │
│   (preCreateProcess, postCreateProcess,                          │
│    preUpdateProcess, postUpdateProcess)                          │
└─────────────────────────┬────────────────────────────────────────┘
                          │  delegates to
┌─────────────────────────▼────────────────────────────────────────┐
│                      Repository Layer                            │
│                                                                  │
│   BaseAccelaeroRepository<E, I>                                  │
│   BaseAccelaeroTenantRepository<E, I, TenantId>  (tenant-aware)  │
│                                                                  │
│   Spring Data JPA + JpaSpecificationExecutor                     │
└──────────────────────────────────────────────────────────────────┘

Supporting contracts (cross-layer):
  ┌─────────────────────────────┐   ┌──────────────────────────────┐
  │  BaseAccelaroMapper         │   │  PredicationResolver         │
  │  AccelaeroMapperContract    │   │  SearchFilter / AccelaeroPage│
  └─────────────────────────────┘   └──────────────────────────────┘
  ┌─────────────────────────────┐   ┌──────────────────────────────┐
  │  RequestValidator           │   │  AccelaroResponse<E>         │
  │  (Default / Binary /        │   │  AccelaeroBaseException      │
  │   Persistence variants)     │   │  (exception hierarchy)       │
  └─────────────────────────────┘   └──────────────────────────────┘
```

---

## 3. Core Components

### 3.1 Service Layer

#### `BaseAccelaroGrpcServiceContract<SearchReq, Req, Res, E, I>`
_Interface_ — defines the full public API surface of a gRPC-backed service.

| Method | Description |
|---|---|
| `findAll(SearchReq, SearchFilter)` | Fetch all records matching the search criteria |
| `findPage(SearchReq, SearchFilter)` | Paginated fetch |
| `findOne(SearchReq, SearchFilter)` | Fetch a single record; throws if not found |
| `findById(I)` | Fetch by primary key |
| `create(Req)` | Persist a single entity from a request |
| `create(Collection<Req>)` | Batch persist |
| `update(I, Req)` | Full update by ID |
| `partialUpdate(I, Req, Set<String>)` | Partial update — only the named fields are written |
| `deleteById(I)` | Delete silently |
| `deleteOrThrowById(I)` | Delete or throw `AccelaeroNoDataFoundException` |
| `findByCustomQuery(Specification, Mapper)` | Ad-hoc query with a custom response mapper |
| `findPageByCustomQuery(Specification, Mapper, PageRequest)` | Paginated ad-hoc query |

---

#### `BaseAccelaroGrpcService<SearchReq, Req, Res, E, I>`
_Abstract class_ — the primary base for all gRPC service implementations.

Provides a complete implementation of `BaseAccelaroGrpcServiceContract`. Feature modules extend this class and supply the four required collaborators via constructor injection:

| Collaborator | Role |
|---|---|
| `BaseAccelaroServiceContract<E, I>` | Transactional business service |
| `BaseAccelaroMapper<Req, Res, E, I>` | Request ↔ Entity ↔ Response mapper |
| `PredicationResolver<SearchReq, E>` | Converts search requests into JPA `Specification` objects |
| `RequestValidator<SearchReq, Req>` | Validates incoming requests (optional — defaults to `DefaultRequestValidator`) |

**Lifecycle extension points** (override in subclasses as needed):

```
create()  → preCreateProcess(Req)     → [save] → postCreateProcess(Req, E, Res)
update()  → preUpdateProcess(I, Req)  → [save] → postUpdateProcess(I, Req, Res)
```

---

#### `BaseAccelaroBinaryGrpcService<Req, Res, E, I>`
_Abstract class_ — a specialisation where the **search request and mutation request are the same type** (`SearchReq == Req`). Use this when a single proto message serves both filtering and write operations.

---

#### `BaseWildcardGrpcService<Req, Res, E, I>`
_Abstract class_ — a specialisation where `findAll` requires **no search criteria**. Uses `google.protobuf.Empty` as `SearchReq` and applies a default always-true JPA predicate. Suitable for simple reference-data services.

---

#### `BaseAccelaeroService<E, I>`
_Abstract class_ — the transactional business service layer. Implements `BaseAccelaroServiceContract` and wraps all repository calls in appropriate Spring `@Transactional` boundaries.

Key behaviours:

- Read operations run under `@Transactional(readOnly = true)`.
- `save()` forces `entity.setId(null)` to prevent accidental updates masquerading as inserts.
- `update()` detaches the previously loaded entity from the JPA context before saving, preventing stale-state conflicts.
- Calls `ValidatorUtil.validate()` (JSR-303 bean validation) on every entity before persist or update.

**Lifecycle extension points:**

```
save()   → preCreateProcess(E)               → [repository.save] → postCreateProcess(E, savedE)
update() → preUpdateProcess(Optional<E>, E)  → [detach + repository.save] → postUpdateProcess(Optional<E>, savedE)
```

---

### 3.2 Repository Layer

#### `BaseAccelaeroRepository<Entity extends BaseEntity<Id>, Id>`
_Interface_ — combines `JpaRepository` and `JpaSpecificationExecutor`. Gives implementing repositories all standard Spring Data JPA operations plus dynamic Specification-based queries.

```java
public interface BaseAccelaeroRepository<Entity extends BaseEntity<Id>, Id>
        extends JpaRepository<Entity, Id>, JpaSpecificationExecutor<Entity> { }
```

---

#### `BaseAccelaeroTenantRepository<Entity extends BaseTenantEntity<Id, TenantId>, Id, TenantId>`
_Interface_ — the tenant-aware repository variant. Deliberately **overrides `findAll()`** to throw a `RuntimeException`, enforcing that callers must always supply a `Specification` that includes a tenant-ID predicate. This prevents accidental cross-tenant data exposure.

---

#### `PredicationResolver<Req, E>`
_Interface_ — translates a gRPC/search request and a `SearchFilter` into a JPA `Specification<E>`.

```
PredicationResolver.predicate(Req, SearchFilter, Root, CriteriaQuery, CriteriaBuilder)
                                   └──── wrapped into ────▶  Specification<E>
```

Implement one `PredicationResolver` per entity to express what the search fields mean in SQL terms. The `specification()` default method wraps the predicate into a `Specification` automatically.

---

### 3.3 Mapping Layer

#### `BaseAccelaroMapper<Req, Res, E, I>`
_Interface_ — the primary bidirectional mapper contract. Feature modules implement this interface to perform all conversions between the gRPC proto layer and the JPA entity layer.

| Method | Direction |
|---|---|
| `byRequest(Req)` | Proto request → JPA entity |
| `byEntity(E)` | JPA entity → Proto response |
| `byRequests(Collection<Req>)` | Batch: request list → entity list |
| `byEntities(Collection<E>)` | Batch: entity list → response list |
| `entityToResponseMapper()` | Returns an `AccelaeroMapperContract` delegate (used internally by `BaseAccelaroGrpcService`) |

---

#### `AccelaeroMapperContract<T, F>`
_Interface_ — a single-direction, generic conversion contract. Used internally for ad-hoc custom-response queries where the response type differs from the standard `Res`.

```java
T convert(F from);
List<T> convert(Collection<F> fromList);  // default: streams + maps
```

---

### 3.4 Validation Layer

#### `RequestValidator<SearchReq, Req>`
_Interface_ — two-method contract for validating incoming traffic:

| Method | When called |
|---|---|
| `validateSearchRequest(SearchReq)` | Before any find/search operation |
| `validate(Req)` | Before any create or update operation |

Three ready-made implementations are provided:

| Class | Use case |
|---|---|
| `DefaultRequestValidator` | Default — no-op search validation; null check on write requests |
| `PersitenceRequestValidator<Req>` | Write-only services where `SearchReq` is `google.protobuf.Empty` |
| `BinaryRequestValidator<Req>` | Services where `SearchReq` and `Req` are the same type |

When no custom validator is passed to `BaseAccelaroGrpcService`, `DefaultRequestValidator` is used automatically.

---

### 3.5 Entity Contracts

#### `BaseEntity<I>`
_Interface_ — the minimal contract all JPA entities must satisfy. Provides `getId()` (via `IdProvider<I>`) and `setId(I id)`. Also extends `Serializable`.

#### `BaseTenantEntity<Entity, TenantId>`
_Interface_ — extends `BaseEntity` and adds `getTenantId()` (via `TenantIdProvider<TenantId>`). Required for all entities that participate in multi-tenant data isolation.

---

### 3.6 API Types

#### `AccelaroResponse<E>`
Generic response wrapper returned by every operation in `BaseAccelaroGrpcServiceContract`.

| Constructor | Contents |
|---|---|
| `AccelaroResponse(E data)` | Single-item response |
| `AccelaroResponse(List<E> dataList)` | List response |
| `AccelaroResponse(List<E>, int page, int totalPages)` | Paginated list response |
| `AccelaroResponse(E/List, String message)` | Response with an optional status message |

#### `SearchFilter`
Carries pagination and sort parameters into the query pipeline. Wraps an `AccelaeroPage`.

| Factory method | Behaviour |
|---|---|
| `SearchFilter.empty()` | No paging — returns all results |
| `SearchFilter.sortBy(String)` | Sort ascending, no page limit |
| `SearchFilter.sortBy(String, SortOrder)` | Sort with explicit order |

#### `AccelaeroPage`
Holds `start`, `limit`, `page`, `sort`, and `sortOrder`. Converts to a Spring Data `PageRequest` via `toPageRequest()`. Supports offset-based pagination.

---

### 3.7 Exception Hierarchy

All exceptions extend `AccelaeroBaseException`, which carries an `io.grpc.Status`. This allows upstream gRPC interceptors to translate exceptions directly into gRPC status codes without additional mapping logic.

```
AccelaeroBaseException  (RuntimeException — carries gRPC Status)
├── AccelaeroNoDataFoundException      → Status.NOT_FOUND
├── AccelaeroValidationException       → Status.INVALID_ARGUMENT
├── AccelaeroAlreadyExistException     → Status.ALREADY_EXISTS
├── AccelaeroUnauthenticatedException  → Status.UNAUTHENTICATED
├── AccelaeroUnauthorizedException     → Status.PERMISSION_DENIED
├── AccelaeroInternalException         → Status.INTERNAL
├── AccelaeroStaleDataException        → (optimistic lock / stale data)
└── AccelaeroProcessException          → (general processing failure)
```

All exceptions expose a static factory `ExceptionType.by(String message)` for clean, readable throw sites.

---

### 3.8 Job Monitoring

An optional, AOP-based subsystem for tracking the execution of `@Scheduled` jobs. Activated by adding `@EnableJobMonitoring` to any Spring `@Configuration` class.

| Component | Role |
|---|---|
| `@EnableJobMonitoring` | Activates auto-configuration and registers the AOP aspect |
| `@MonitorJob(jobName = "...")` | Marks a `@Scheduled` method for execution monitoring |
| `JobMonitoringAspect` | Intercepts annotated methods; persists start time, end time, and status |
| `JobMonitoringService` | Interface for querying job execution history |
| `JobExecutionMonitor` | JPA entity representing one job execution record |
| `JobExecutionStatus` | Enum: `IN_PROGRESS`, `SUCCESS`, `FAILURE` |

```java
@Scheduled(cron = "0 * * * * *")
@MonitorJob(jobName = "MY_JOB_NAME")
public void runMyJob() {
    // scheduled logic — execution is tracked automatically
}
```

See [`src/docs/JOB_MONITORING.md`](JOB_MONITORING.md) for detailed setup and configuration.

---

## 4. Request Processing Flow

The following sequence describes the complete lifecycle of a **mutation request** (create or update). Read flows follow the same structure but skip the write-path hooks and mapping.

```
External gRPC Call
        │
        ▼
┌───────────────────────────────────────────────┐
│  1. RECEIVE                                   │
│     BaseAccelaroGrpcService                   │
│     Accepts gRPC proto request (Req)          │
└───────────────────┬───────────────────────────┘
                    │
                    ▼
┌───────────────────────────────────────────────┐
│  2. VALIDATE                                  │
│     RequestValidator.validate(Req)            │
│     • Null check (DefaultRequestValidator)    │
│     • Custom domain rules (if overridden)     │
│     • Throws AccelaeroValidationException     │
│       on failure                              │
└───────────────────┬───────────────────────────┘
                    │
                    ▼
┌───────────────────────────────────────────────┐
│  3. PRE-PROCESS HOOK                          │
│     preCreateProcess(Req) /                   │
│     preUpdateProcess(I, Req)                  │
│     No-op by default; override to add         │
│     business rules before the persist         │
└───────────────────┬───────────────────────────┘
                    │
                    ▼
┌───────────────────────────────────────────────┐
│  4. MAP REQUEST → ENTITY                      │
│     BaseAccelaroMapper.byRequest(Req)         │
│     Proto message → JPA entity                │
└───────────────────┬───────────────────────────┘
                    │
                    ▼
┌───────────────────────────────────────────────┐
│  5. SERVICE / TRANSACTION                     │
│     BaseAccelaeroService.save() / update()    │
│     • Opens @Transactional boundary           │
│     • Runs JSR-303 bean validation on entity  │
│     • Detaches existing entity (update path)  │
│     • Delegates to repository                 │
└───────────────────┬───────────────────────────┘
                    │
                    ▼
┌───────────────────────────────────────────────┐
│  6. REPOSITORY                                │
│     BaseAccelaeroRepository.save()            │
│     SQL INSERT / UPDATE via Spring Data JPA   │
└───────────────────┬───────────────────────────┘
                    │
                    ▼
┌───────────────────────────────────────────────┐
│  7. MAP ENTITY → RESPONSE                     │
│     BaseAccelaroMapper.byEntity(E)            │
│     Saved JPA entity → proto response         │
└───────────────────┬───────────────────────────┘
                    │
                    ▼
┌───────────────────────────────────────────────┐
│  8. POST-PROCESS HOOK                         │
│     postCreateProcess(Req, E, Res) /          │
│     postUpdateProcess(I, Req, Res)            │
│     No-op by default; override for side       │
│     effects (events, notifications, etc.)     │
└───────────────────┬───────────────────────────┘
                    │
                    ▼
        AccelaroResponse<Res>  returned to caller
```

**Search / read flow** (abbreviated):

```
gRPC Search Request + SearchFilter
  → PredicationResolver.specification(SearchReq, SearchFilter)
  → BaseAccelaeroService.findAll(Specification) / findAll(Specification, PageRequest)
  → BaseAccelaeroRepository.findAll(Specification)
  → AccelaeroMapperContract.convert(List<E>)
  → AccelaroResponse<Res>
```

---

## 5. Extension Guidelines

### 5.1 What a feature module must provide

Every feature module that exposes data over gRPC requires these implementations:

| Contract to implement | One per |
|---|---|
| `BaseEntity<I>` | JPA entity class |
| `BaseAccelaeroRepository<E, I>` | Entity type |
| `BaseAccelaeroService<E, I>` | Entity type |
| `BaseAccelaroMapper<Req, Res, E, I>` | gRPC service |
| `PredicationResolver<SearchReq, E>` | gRPC service |
| `BaseAccelaroGrpcService<…>` (or variant) | gRPC service endpoint |

### 5.2 Choosing the right GrpcService base class

| Scenario | Base class to extend |
|---|---|
| Search request and mutation request are different types | `BaseAccelaroGrpcService` |
| Same request type for search and mutation | `BaseAccelaroBinaryGrpcService` |
| `findAll` requires no search parameters | `BaseWildcardGrpcService` |

### 5.3 Choosing the right Repository base interface

| Scenario | Repository to extend |
|---|---|
| Entity is not tenant-scoped | `BaseAccelaeroRepository` |
| Entity belongs to a tenant | `BaseAccelaeroTenantRepository` — always include a tenant predicate in `PredicationResolver` |

### 5.4 Custom validation

Supply a custom `RequestValidator` to the `BaseAccelaroGrpcService` constructor to override the default null-check behaviour:

```java
super(jpaService, mapper, predicationResolver, new MyDomainRequestValidator());
```

Choose the right validator base interface:

| Interface | When to use |
|---|---|
| `RequestValidator<SearchReq, Req>` | Fully custom — different search and write types |
| `PersitenceRequestValidator<Req>` | Write-only services (`SearchReq` is `Empty`) |
| `BinaryRequestValidator<Req>` | `SearchReq` and `Req` are the same type |

### 5.5 Lifecycle hooks

Do not override core CRUD methods. Use the provided hooks instead:

| Hook | Layer | Override to |
|---|---|---|
| `preCreateProcess(Req)` | gRPC service | Enforce business rules before entity creation |
| `postCreateProcess(Req, E, Res)` | gRPC service | Publish domain events, send notifications |
| `preUpdateProcess(I, Req)` | gRPC service | Validate state transitions |
| `postUpdateProcess(I, Req, Res)` | gRPC service | Publish events, trigger cascade operations |
| `preCreateProcess(E)` | Business service | Pre-persist entity-level validation |
| `postCreateProcess(E, savedE)` | Business service | Post-persist entity-level side effects |
| `preUpdateProcess(Optional<E>, E)` | Business service | Pre-update entity-level checks |
| `postUpdateProcess(Optional<E>, savedE)` | Business service | Post-update entity-level side effects |

### 5.6 Partial updates

Use `partialUpdate(I id, Req request, Set<String> fieldsToUpdate)` when only specific fields should change. The framework loads the existing entity, then uses `AccelaeroUtil.copyNonNullProperties` to merge only the named fields before saving.

---

## 6. Design Principles

### Separation of Concerns
Each layer has exactly one responsibility. The gRPC layer orchestrates flow but never touches the database. The service layer owns transactions. The repository layer owns persistence. Mappers own protocol translation. These boundaries are enforced structurally — each layer holds a reference only to its immediate collaborator.

### Reusability
All standard CRUD and search operations are implemented once in this module. Feature modules inherit complete, production-ready behaviour and contribute only domain-specific logic, eliminating duplicated boilerplate.

### Consistency
Every feature module processes requests through the same lifecycle pipeline. Validation, transaction boundaries, error mapping, and response structure are predictable and uniform across all services.

### Safety by Default
- `BaseAccelaeroTenantRepository` blocks unsafe `findAll()` at runtime, preventing inadvertent cross-tenant data leaks.
- `BaseAccelaeroService.save()` always nulls the entity ID before insert, preventing accidental updates.
- The existing entity is detached from the JPA context before any update, avoiding stale-state merge issues.
- All domain exceptions carry a gRPC `Status`, ensuring errors are always serialisable to the transport layer.

### Open/Closed
Concrete modules add behaviour by implementing interfaces and overriding lifecycle hooks — not by modifying shared code. The `preXxx` / `postXxx` hook pattern provides structured extension points without requiring base class changes.

---

## 7. Conceptual Example

The following is a conceptual walkthrough of how a hypothetical `FlightService` would be wired using this module.

**Domain:** Manage `Flight` entities over gRPC. Flights are tenant-scoped.

```
1. FlightEntity implements BaseTenantEntity<Long, String>
   → Has ID (Long) and tenantId (String)

2. FlightRepository extends BaseAccelaeroTenantRepository<FlightEntity, Long, String>
   → Inherits all Spring Data JPA and Specification queries
   → findAll() is blocked — must call findAll(Specification) with a tenant predicate

3. FlightPredicationResolver implements PredicationResolver<FlightSearchRequest, FlightEntity>
   → Translates FlightSearchRequest fields (flightNumber, date, status)
     into a JPA Predicate that always includes "tenantId = :tenantId"

4. FlightMapper implements BaseAccelaroMapper<FlightRequest, FlightResponse, FlightEntity, Long>
   → byRequest(FlightRequest)  →  proto fields to FlightEntity
   → byEntity(FlightEntity)    →  FlightEntity fields to FlightResponse

5. FlightJpaService extends BaseAccelaeroService<FlightEntity, Long>
   → No extra code needed for standard CRUD
   → Override postCreateProcess() to publish a FlightCreatedEvent if required

6. FlightGrpcService extends BaseAccelaroGrpcService<
       FlightSearchRequest, FlightRequest, FlightResponse, FlightEntity, Long>
   → Constructor injects FlightJpaService, FlightMapper, FlightPredicationResolver
   → Override preCreateProcess() to validate slot availability
   → findAll, findPage, findById, create, update, delete all inherited for free
```

A call to `createFlight(FlightRequest)` flows as:

```
FlightGrpcService.create(FlightRequest)
  → DefaultRequestValidator.validate(request)      [null check]
  → FlightGrpcService.preCreateProcess(request)    [slot availability check]
  → FlightMapper.byRequest(request)                [proto → entity]
  → FlightJpaService.save(entity)                  [@Transactional + JSR-303 validation]
    → FlightRepository.save(entity)                [SQL INSERT]
  → FlightMapper.byEntity(savedEntity)             [entity → proto response]
  → FlightGrpcService.postCreateProcess(...)       [publish FlightCreatedEvent]
  → AccelaroResponse<FlightResponse>
```

---

## Dependencies

| Dependency | Version |
|---|---|
| Spring Boot Data JPA | 2.1.3.RELEASE |
| Spring Boot Validation | 2.1.3.RELEASE |
| gRPC Core | per root project config |
| Protobuf Java Util | per root project config |
| Lombok | 1.18.30 |
| Java | 11 |

---

*Copyright © 2025 ACCELaero — Information Systems Associates (pvt) Ltd. All rights reserved.*
