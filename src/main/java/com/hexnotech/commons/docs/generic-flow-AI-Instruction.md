# AI Agent Implementation Instruction — Generic Feature Flow

Use this document as the starting prompt when asking an AI agent to implement a new CRUD feature in `aero-ops-manager-service`.

---

## Before You Start — Read These First

The AI agent must read and understand the following documents before generating any code. They are the authoritative source of truth for this codebase.

| Document | Location | What it covers |
|---|---|---|
| `Generic-Request-Flow.md` | `aero-ops-common/docs/` | Full architecture: base classes, layer responsibilities, lifecycle hooks, extension rules |
| `proto-definition-guideline.md` | `aero-ops-common/docs/` | Proto file conventions — URL naming, request/response patterns, enum rules |
| `Common-Types.md` | `aero-ops-common/docs/` | Shared utilities: `AccelaeroValidator`, `MergeList`, `ComparisonProvider`, `Range`, async types |
| `JPA-Type.md` | `aero-ops-common/docs/` | JPA utilities: `JpaPredicateBuilder`, `PredicationResolver`, `TupleProcessor`, `EntityGraphProcessor` |
| `Utility-Classes.md` | `aero-ops-common/docs/` | General-purpose utilities: `AccelaeroUtil`, `AccelaeroDateTimeUtil`, `PredicateUtil`, `ValidatorUtil`, constants |
| `Async-Process.md` | `aero-ops-common/docs/` | Parallel execution: `AsyncProcessor`, `AsyncDataProvider`, `AsyncExecutor` |
| `Carrier-Security.md` | `aero-ops-common/docs/` | Carrier-based authorization: `@AuthorizeCarriers`, `@FilterCarriers` |
| `Feature-Flags.md` | `aero-ops-common/docs/` | Feature toggling: `@FeatureEvaluate`, `FeatureFlagContextSupplier` |

---

## Input: What to Provide to the AI Agent

1. **The proto file** — located in `aero-ops-manager-api/proto/<Feature>.proto`. The proto is the primary specification. All implementation decisions flow from it.
2. **This instruction document.**
3. **Any domain-specific rules** — validation constraints, business rules, or special behaviours not expressible in the proto (e.g. "crew type must be unique per carrier").

---

## Step 1 — Analyse the Proto File

Before writing any code, derive the following from the proto:

### 1.1 Identify the messages

- **Request message** — the input for create/update RPCs. Note all fields and their types.
- **Response message** — the entity returned by create/update/get RPCs.
- **List response message** — the wrapper for the get-all/search RPC.
- **Enums** — any proto enums that need a corresponding Java enum in `model/type/`.
- **Repeated fields** — fields that map to `List<T>` in Java, potentially PostgreSQL arrays in the DB.

### 1.2 Identify the RPCs and choose the base class

Inspect the service block and apply this decision table:

| Proto service has... | Extend |
|---|---|
| `getAll` with `Empty` input and no search filtering needed | `BaseWildcardGrpcService` |
| Separate search request type (`<Entity>SearchRequest`) different from write request | `BaseAccelaroGrpcService` |
| Same request type used for both search and write | `BaseAccelaroBinaryGrpcService` |

### 1.3 Choose the repository base

| Entity has a `carrierCode` / tenant concept? | Repository base |
|---|---|
| No | `BaseAccelaeroRepository<Model, Id>` |
| Yes | `BaseAccelaeroTenantRepository<Model, Id, TenantId>` — always include a tenant predicate in `PredicationResolver` |

### 1.4 Choose the validator base

| Search request type | Validator base |
|---|---|
| `google.protobuf.Empty` (wildcard / write-only) | `PersistenceRequestValidator<Req>` |
| Same type as write request | `BinaryRequestValidator<Req>` |
| Separate search and write request types | `RequestValidator<SearchReq, Req>` |

---

## Step 2 — Files to Create

For a feature derived from the proto, create the following files. Package paths are relative to `aero-ops-manager-service/src/main/java/com/accelaero/ops/manager/`.

```
aero-ops-manager-api/proto/
  <Feature>.proto                           ← PROVIDED — do not generate

aero-ops-manager-service/src/main/java/com/accelaero/ops/manager/
  model/
    <Entity>Model.java                      ← JPA entity
    type/
      <EnumName>Type.java                   ← only if proto has enums
  data/
    <Entity>Repository.java                 ← extends the chosen repository base
    <Entity>PredicationResolver.java        ← implements com.accelaero.common.jpa.type.PredicationResolver (only if search is required)
  service/
    impl/
      <Entity>Service.java                  ← extends BaseAccelaeroServiceV2
    grpc/
      <Entity>GrpcService.java              ← extends the chosen GrpcService base
    mapper/
      <Entity>Mapper.java                   ← implements BaseAccelaroMapper
    validator/
      <Entity>Validator.java                ← implements the chosen validator base
  grpc/
    <Entity>Grpc.java                       ← @GRpcService endpoint, extends generated ImplBase

aero-ops-manager-service/src/main/resources/db/migration/
  V<next>__create_<table>_table.sql

Constants to update (do not create new files — add to existing):
  constants/OpsPrivilegs.java               ← add new inner class with VIEW / UPDATE constants
  constants/ErrorMessageFieldConstant.java  ← add field name string constants for the validator
  constants/AeroOpsConstants.java           ← add length constants used in model columns / validator
```

---

## Step 3 — Implementation Rules (by Layer)

These are rules, not templates. Adapt every implementation to the actual proto fields.

### JPA Model (`<Entity>Model`)

- Implements `BaseEntity<Long>` (or `BaseTenantEntity` if tenant-scoped).
- Annotate with `@EntityListeners(AuditingEntityListener.class)` for Spring Data auditing.
- Map each proto field to a column. Use the same business name but follow DB naming conventions (snake_case columns, `aero_ops` schema, `t_<entity>` table name).
- Proto enum fields → Java enum in `model/type/`, stored as `@Enumerated(EnumType.STRING)`.
- Proto `repeated string` fields → `List<String>`, column type `varchar[]` with `@Type(type = "list-array")`.
- Audit fields: `createdBy`, `createdDate`, `lastUpdated`, `lastUpdatedBy` annotated with Spring Data auditing annotations.
- Use `Instant` for all timestamps.
- Derive column constraints (`nullable`, `length`) from the proto field semantics and validator rules.

### Repository (`<Entity>Repository`)

- Extends the chosen base. No code needed for standard CRUD.
- Add custom query methods only if the feature requires them (e.g. `findByCrewType`, duplicate checks).
- For Specification-based search, implement a `PredicationResolver` — see `JPA-Type.md`.

### JPA Service (`<Entity>Service`)

- Extends `BaseAccelaeroServiceV2<Model, Long>`.
- No code needed for standard CRUD — all operations are inherited.
- Override lifecycle hooks (`preCreateProcess`, `postCreateProcess`, etc.) only if the feature has domain-specific pre/post logic.

### Mapper (`<Entity>Mapper`)

- Implements `BaseAccelaroMapper<Req, Res, Model, Long>`.
- `byRequest(Req)` — maps proto request → JPA entity. Use `AccelaeroUtil.nvlTrim()` for strings, `AccelaeroUtil.getByValue()` for enum conversion, `AccelaeroDateTimeUtil.parseCommonDateOrThrow()` / `parseCommonTimeOrThrow()` for date-time string fields. See `Utility-Classes.md`.
- `byEntity(Model)` — maps JPA entity → proto response. Use `AccelaeroUtil.nvl()` for null safety, `AccelaeroUtil.nvlToTypeList()` for list fields, `.toString()` on `Instant` for date fields, `Collections.emptyList()` as default for lists. See `Utility-Classes.md`.
- Never set the `id` on the entity inside `byRequest` — the service layer handles ID assignment.

### Validator (`<Entity>Validator`)

- Implements the chosen validator base.
- Use `AccelaeroValidator.of()` chain from `Common-Types.md`.
- Validate all required fields, max lengths, format constraints, and enum validity.
- For date/time string fields, use `AccelaeroDateTimeUtil.parseCommonDateOrThrow()` / `parseCommonTimeOrThrow()` — these throw `AccelaeroValidationException` with a field-aware message automatically. See `Utility-Classes.md`.
- Use `ValidatorUtil.validate(object)` when the request or a sub-object carries JSR-303 annotations (`@NotNull`, `@Size`, etc.) and needs programmatic validation outside Spring's pipeline. See `Utility-Classes.md`.
- Use `AccelaeroConstant.DateTimeFormat` pattern constants when a custom format is needed; avoid raw string literals. See `Utility-Classes.md`.
- Use constants from `ErrorMessageFieldConstant` for field names and `AeroOpsConstants.Length` for length limits — add new constants as needed.
- Do not duplicate DB constraints in the validator (e.g. uniqueness) — let the DB throw and handle the exception at the service layer if needed.

### GrpcService (`<Entity>GrpcService`)

- Extends the chosen base class.
- Constructor injects: `<Entity>Service`, `<Entity>Mapper`, `<Entity>Validator`.
- If `BaseAccelaroGrpcService` is used (separate search type), also inject a `PredicationResolver`.
- Override lifecycle hooks only for domain-specific cross-cutting concerns (e.g. event publishing, cache invalidation).

### Grpc Endpoint (`<Entity>Grpc`)

- Annotated `@GRpcService`. Extends the generated `<Entity>ServiceGrpc.<Entity>ServiceImplBase`.
- One method per RPC in the proto service block.
- Each method follows the same pattern: call the appropriate `GrpcService` method → build the proto response → `onNext` + `onCompleted`.
- **create**: `grpcService.create(request).getData()`
- **update**: `grpcService.update(request.getId(), request).getData()`
- **delete**: `grpcService.deleteOrThrowById(id)` → respond with `UpdateResponse.Success`
- **getAll**: `grpcService.findAll(Empty.getDefaultInstance(), SearchFilter.empty())` → build list response with `.addAllXxx(result.getDataList())`
- **search** (if applicable): `grpcService.findAll(request, SearchFilter.of(...))` → build list response
- Secure every method with `@PreAuthorize("hasAnyAuthority('" + OpsPrivilegs.XxxEntity.VIEW/UPDATE + "')")`.

### DB Migration

- Use the next Flyway version number (check existing migrations to determine `V<n>`).
- Table in schema `aero_ops`, name `t_<entity>`.
- Primary key: `BIGSERIAL` named `id`.
- Column types must match the JPA model exactly.
- Add `UNIQUE` constraints for any fields that must be unique per the domain rules.
- Add audit columns: `created_by`, `created_date`, `last_updated`, `last_updated_by`.

---

## Step 4 — Optional Capabilities (Enable When Needed)

Apply these only when the feature explicitly requires them. Refer to the linked docs for full usage.

| Capability | When to apply | Doc to read |
|---|---|---|
| Carrier-based authorization | Entity data is scoped by carrier | `Carrier-Security.md` |
| Parallel data loading | Service method assembles data from multiple sources | `Async-Process.md` |
| Field-change tracking | Audit log needs to record which fields changed | `Common-Types.md` → `ComparisonProvider` |
| List merge (add/update/remove) | Request contains a child list that replaces an existing list | `Common-Types.md` → `MergeList` |
| Range overlap validation | Entity has from/to boundaries | `Common-Types.md` → `Range` |
| Lazy-association initialization | Service detaches entities and accesses lazy fields | `JPA-Type.md` → `EntityGraphProcessor` |
| Feature gating | Feature is behind a flag | `Feature-Flags.md` |
| Date / time string parsing | Request fields carry date or time as strings | `Utility-Classes.md` → `AccelaeroDateTimeUtil` |
| JSON serialization / deserialization | Service reads or writes raw JSON payloads | `Utility-Classes.md` → `AccelaeroUtil` (JSON section) |
| PDF report generation | Feature produces a downloadable PDF from HTML | `Utility-Classes.md` → `AccelaeroUtil.generatePdfFromHtml` |
| Null-safe collection / map operations | Mapping or aggregating nullable lists/maps from DB or request | `Utility-Classes.md` → `AccelaeroUtil` |
| Annotated field inspection | Dynamic processing based on field annotations at runtime | `Utility-Classes.md` → `AccelaeroReflectionUtil` |
| Custom JPA predicates (low-level) | Need a single predicate (like, between, inOrAll) outside `JpaPredicateBuilder` | `Utility-Classes.md` → `PredicateUtil` |
| Grouped / deduplicated query results | DB query returns multiple rows per entity (e.g. joins) and needs one record per key | `Common-Types.md` → `GroupedList` |
| Scheduled job execution tracking | Feature includes a `@Scheduled` method that needs execution history in the DB | `JOB_MONITORING.md` |

---

## Step 5 — Checklist Before Finishing

- [ ] Every proto RPC has a corresponding method in `<Entity>Grpc.java`.
- [ ] Every method in `<Entity>Grpc.java` is secured with `@PreAuthorize`.
- [ ] `OpsPrivilegs` has a new inner class with `VIEW` and `UPDATE` constants.
- [ ] All validator field names come from `ErrorMessageFieldConstant` constants (not raw strings).
- [ ] All length limits in the validator come from `AeroOpsConstants.Length` constants.
- [ ] The DB migration file is the next version number and creates the table in `aero_ops` schema.
- [ ] Enum types in the proto have a matching Java enum in `model/type/`.
- [ ] `byEntity` handles null audit fields safely (they will be null on first-read if auditing is not yet committed).
