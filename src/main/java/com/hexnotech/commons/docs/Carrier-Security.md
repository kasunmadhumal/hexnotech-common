# Enable Carrier base Authorization

Carrier authorization is enforced via the `@AuthorizeCarriers` annotation. It answers:
**Which carriers is this user allowed to touch?**

User privileges follow the pattern `<privilegeKey>.<carrierCode>`.

Example: A user with privileges `["aeroOps.flightWatch.view.G9", "aeroOps.flightWatch.view.3L"]`
is allowed to view flight watch data for carriers `G9` and `3L`.

`@AuthorizeCarriers` extracts the carrier suffix from the user's token privileges and builds
the allowed carrier set for the current request. The resolved set is stored in a `ThreadLocal`
(`CarrierSecurityContext`) so downstream aspects can read it.

## Enable in Your Application

Add `@EnableAccelaeroCommonSecurity` to any `@Configuration` class. This registers both aspects
as Spring beans. Without this annotation, nothing is activated even if the library is on the classpath.

```java
@Configuration
@EnableAccelaeroCommonSecurity
@EnableTransactionManagement(order = 0)   // required — see Carrier base data filter section
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class SecurityConfiguration {
}
```

## `@AuthorizeCarriers` Annotation

**Aspect order: 1** — runs before `@FilterCarriers`.

### Attributes

| Attribute | Required | Description |
|---|---|---|
| `privilege` | One of these two | Single privilege key, e.g. `"aeroOps.flightWatch.view"` |
| `privileges` | One of these two | Multiple privilege keys — allowed carriers are the **union** across all |
| `carrierExpression` | Yes | SpEL expression resolving to a `String` or `List<String>` of requested carriers |
| `operationType` | No (default `READ`) | `READ`, `CREATE`, `UPDATE`, or `DELETE` — affects carrier resolution behaviour |

`privilege` and `privileges` cannot be used together — a runtime exception is thrown if both are set.

### `carrierExpression` Examples

```java
// From a repeated proto field (List)
carrierExpression = "#request.carrierCodeList"

// From a singular proto/DTO field
carrierExpression = "#request.carrierCode"

// Derived via a Spring bean
carrierExpression = "@flightService.getCarrierCodeVal(#request.flightReference)"

// From a plain method argument named 'carrierCode'
carrierExpression = "#carrierCode"
```

Parameters are bound by their Java parameter name (requires `-parameters` compiler flag, standard
in Spring Boot) and always by index (`#arg0`, `#arg1`, ...) as a fallback.

### `operationType` Behaviour

| Operation | Carriers requested | Result |
|---|---|---|
| `READ` | None (empty list) | Returns **all** carriers the user is authorized for (wildcard) |
| `READ` | Some specified | Returns the **intersection** — unauthorized carriers are silently dropped |
| `WRITE` (`CREATE`/`UPDATE`/`DELETE`) | None | **Throws** — carrier must always be specified for mutations |
| `WRITE` | Some specified | **Throws** if any requested carrier is not in the user's authorized set |

### Multiple Privileges

Use `privileges` when access should be granted if the user has a carrier under **any** of the
listed privileges (union):

```java
@AuthorizeCarriers(
    privileges        = {"aeroOps.flight.cancel", "aeroOps.flight.reinstate"},
    carrierExpression = "#request.carrierCode",
    operationType     = OperationType.WRITE
)
```

### Usage Example

```java
@Transactional(readOnly = true)
@AuthorizeCarriers(
    privilege         = "aeroOps.flightWatch.view",
    carrierExpression = "#request.carrierCodeList",
    operationType     = OperationType.READ
)
@FilterCarriers
public MyResponse getMyData(MyRequest request) {
    return repository.findAll();
}
```

---

# Enable Carrier base data filter

Carrier data filtering is enforced via the `@FilterCarriers` annotation backed by a Hibernate
session filter. It answers: **How do we make sure database queries only return data for the
authorized carriers?**

`@FilterCarriers` reads the allowed carriers stored by `@AuthorizeCarriers` in `CarrierSecurityContext`
and enables the Hibernate session filter `carrierFilter`. Every JPQL query and Spring Data derived
query on entities annotated with `@Filter(name = "carrierFilter", ...)` will automatically have
`WHERE carrier_code IN (...)` appended. The filter is disabled in a `finally` block after the
method returns.

## Annotate Your Entity

Any entity that should be carrier-filtered must declare the Hibernate filter:

```java
@Entity
@FilterDef(
    name       = "carrierFilter",
    parameters = @ParamDef(name = "carrierIds", type = "string")
)
@Filter(
    name      = "carrierFilter",
    condition = "carrier_code IN (:carrierIds)"   // adjust column name as needed
)
public class YourEntity {
    @Column(name = "carrier_code")
    private String carrierCode;
    // ...
}

Any entity that already extends BaseTenantEntity then follow:

@Entity
@Filter(
        name      = "carrierFilter",
        condition = "carrier_code IN (:carrierIds)"   // adjust column name as needed
)
public class YourEntity extends BaseTenantEntity<Id, TenantId> {
    @Column(name = "carrier_code")
    private String carrierCode;
    // ...
}
```

The filter name `carrierFilter` and parameter name `carrierIds` are fixed constants
defined in `CarrierSecurityConstant`.

## `@FilterCarriers` Annotation

**Aspect order: 2** — runs after `@AuthorizeCarriers` has populated the context.

`@FilterCarriers` calls `entityManager.unwrap(Session.class)`, which requires an **active
Hibernate session** at the time it runs. A session is opened when a transaction begins.

Because `@FilterCarriers` is at order 2 and the transaction advisor must run **before** it,
`@EnableTransactionManagement(order = 0)` must be declared in your configuration. The
`@Transactional` annotation on the service method provides the session:

```java
@Transactional(readOnly = true)   // order 0 — opens session first
@AuthorizeCarriers(...)            // order 1 — resolves allowed carriers
@FilterCarriers                    // order 2 — enables filter on the open session
public MyResponse getMyData(...) { ... }
```

> If `@Transactional` is absent on the service method, `@FilterCarriers` will throw at runtime
> because there is no session to enable the filter on.

## Native SQL and Criteria API Queries

The Hibernate session filter only applies to JPQL and Spring Data derived queries. For native SQL
or Criteria API, read the allowed carriers from `CarrierSecurityContext` directly:

**Criteria API (Specifications):**
```java
predicates.add(root.get("carrierCode").in(CarrierSecurityContext.getAllowedCarriers()));
```

**Native SQL (`@Query`):**
```java
@Query(value = "SELECT * FROM aero_ops.t_flight WHERE carrier_code IN :carrierIds", nativeQuery = true)
List<Flight> findFlights(@Param("carrierIds") Set<String> carrierIds);

// In the service method:
repository.findFlights(CarrierSecurityContext.getAllowedCarriers());
```

---

## Aspect Execution Order Summary

```
Incoming request
        │
        ▼  order = 0
@Transactional          → Opens Hibernate session / joins existing transaction
        │
        ▼  order = 1
@AuthorizeCarriers      → Validates user privileges
                          Resolves allowed carriers
                          Stores result in CarrierSecurityContext (ThreadLocal)
        │
        ▼  order = 2
@FilterCarriers         → Reads CarrierSecurityContext
                          Calls session.enableFilter("carrierFilter")
                          Sets carrierIds = [G9, 3L, ...]
        │
        ▼
  Method body           → All repository.findAll() / JPQL queries automatically
                          include WHERE carrier_code IN ('G9', '3L', ...)
        │
        ▼  (finally)
@FilterCarriers         → session.disableFilter("carrierFilter")
@AuthorizeCarriers      → CarrierSecurityContext.clear()
```

## Complete Example

### Entity

```java
@Entity
@FilterDef(name = "carrierFilter", parameters = @ParamDef(name = "carrierIds", type = "string"))
@Filter(name = "carrierFilter", condition = "carrier_code IN (:carrierIds)")
@Table(name = "t_flight", schema = "aero_ops")
public class Flight {
    @Column(name = "carrier_code")
    private String carrierCode;
    // ...
}
```

### Repository

```java
public interface FlightRepository extends JpaRepository<Flight, Long> {
    // findAll() inherits from JpaRepository — filter is applied automatically
    // when called inside a @FilterCarriers-annotated service method
}
```

### Service

```java
@Service
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;

    @Override
    @Transactional(readOnly = true)
    @AuthorizeCarriers(
        privilege         = "aeroOps.flightWatch.view",
        carrierExpression = "#request.carrierCodeList",
        operationType     = OperationType.READ
    )
    @FilterCarriers
    public FlightListResponse getFlights(FlightRequest request) {
        List<Flight> flights = flightRepository.findAll();
        // flights is already scoped to the caller's authorized carriers
        return buildResponse(flights);
    }
}
```

### Configuration

```java
@Configuration
@EnableAccelaeroCommonSecurity
@EnableTransactionManagement(order = 0)
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class SecurityConfiguration {
}
```

## Common Mistakes

| Mistake | Symptom | Fix |
|---|---|---|
| Missing `@EnableAccelaeroCommonSecurity` | No authorization or filtering happens | Add it to a `@Configuration` class |
| Missing `@EnableTransactionManagement(order = 0)` | `@FilterCarriers` throws because no session exists | Add it alongside `@EnableAccelaeroCommonSecurity` |
| `@FilterCarriers` without `@AuthorizeCarriers` on the same call path | `AccelaeroUnauthorizedException`: context is empty | Always run `@AuthorizeCarriers` first |
| `@FilterCarriers` without `@Transactional` on the method | Runtime error unwrapping session | Add `@Transactional` (or `@Transactional(readOnly=true)`) to the method |
| Entity missing `@FilterDef` / `@Filter` | Queries return unfiltered data silently | Add both annotations to the entity class |
| Using native SQL without manual carrier filtering | Queries bypass the Hibernate filter | Pass `CarrierSecurityContext.getAllowedCarriers()` as a query parameter |
