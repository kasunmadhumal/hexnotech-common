# Feature Flag Evaluator

The feature flag system provides AOP-driven, annotation-based feature toggling at the method or class level. Flags can be scoped globally, per-tenant, or per-carrier, with a configurable fallback when a flag is missing.

---

## How It Works

When a method annotated with `@FeatureEvaluate` (or inside a class annotated with it) is invoked, `FeatureEvaluatorAspect` intercepts the call and runs an evaluation before the method body executes.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          Incoming Method Call                               │
└────────────────────────────────┬────────────────────────────────────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │  FeatureEvaluatorAspect  │  (@Around advice)
                    └────────────┬────────────┘
                                 │
              ┌──────────────────▼──────────────────┐
              │        byPass == true?               │
              └──────────────────┬──────────────────┘
                     YES         │         NO
                ┌────────────────┘         └────────────────────┐
                │                                               │
    ┌───────────▼───────────┐               ┌──────────────────▼──────────────────┐
    │  proceed() → execute  │               │         shouldProcess()              │
    └───────────────────────┘               │                                      │
                                            │  ┌──────────────────────────────┐   │
                                            │  │ isGlobal == true?            │   │
                                            │  │   → globalFeatureMap lookup  │   │
                                            │  └──────────────┬───────────────┘   │
                                            │                 │                    │
                                            │  ┌──────────────▼───────────────┐   │
                                            │  │ considerCarrier == true?     │   │
                                            │  │   → carrierFeatureMap lookup │   │
                                            │  │   (⚠ not yet implemented)    │   │
                                            │  └──────────────┬───────────────┘   │
                                            │                 │                    │
                                            │  ┌──────────────▼───────────────┐   │
                                            │  │ tenantFeatureMap lookup       │   │
                                            │  │ (always evaluated)            │   │
                                            │  └──────────────┬───────────────┘   │
                                            │                 │                    │
                                            │        global OR carrier OR tenant   │
                                            └──────────────────┬──────────────────┘
                                                               │
                                          ┌────────────────────▼────────────────────┐
                                          │              enabled?                    │
                                          └────────────────────┬────────────────────┘
                                                   YES         │       NO
                                          ┌──────────────────  │  ──────────────────┐
                                          │                    │                    │
                              ┌───────────▼──────────┐  ┌──────▼─────────────────────────────────┐
                              │  proceed() → execute  │  │     disabledMessage non-empty?          │
                              └──────────────────────┘  └──────┬───────────────────────┬───────────┘
                                                              YES                      NO
                                                               │                        │
                                          ┌────────────────────▼──────────┐  ┌─────────▼───────────────┐
                                          │  throw                         │  │  return null + log warn │
                                          │  FeatureDisabledException(msg) │  └─────────────────────────┘
                                          └───────────────────────────────┘
```

---

## Components

| Class | Role |
|---|---|
| `@EnableFeatureEvaluator` | Import annotation — add to your `FeatureFlagContextSupplier` implementation to activate the system |
| `@FeatureEvaluate` | Method/class annotation — marks what to gate and how to evaluate |
| `FeatureEvaluatorAspect` | AOP around-advice that intercepts annotated calls |
| `FeatureEvaluatorService` | Evaluation logic — delegates to `FeatureFlagHolder` |
| `FeatureFlagHolder` | In-memory index of flags, keyed by global / tenant / carrier |
| `FeatureFlagContextSupplier` | Interface your application implements to provide flags and the current tenant |
| `FeatureFlag` | Data class holding flag name, enabled state, and optional tenant/carrier scope |
| `FeatureFlagConfig` | Spring config — registers beans when imported via `@EnableFeatureEvaluator` |

---

## Enable in Your Application

### 1. Implement `FeatureFlagContextSupplier`

Create a `@Service` that implements the interface and add `@EnableFeatureEvaluator` to it. This registers the aspect beans and supplies the flag values.

```java
@Service
@EnableFeatureEvaluator
public class AppFeatureFlagContextSupplier implements FeatureFlagContextSupplier {

    @Value("${app.ff.some-feature:true}")
    private boolean isSomeFeatureEnabled;

    @Value("${aero.ops.tenant:}")
    private String tenantId;

    @Override
    public String getCurrentTenant() {
        return tenantId;
    }

    @Override
    public List<FeatureFlag> getFeatureFlags() {
        return List.of(
            new FeatureFlag("SOME_FEATURE", isSomeFeatureEnabled)
        );
    }
}
```

### 2. Define flag name constants

Keep flag names in a constants class to avoid typos:

```java
public static final class Features {
    public static final String SOME_FEATURE = "SOME_FEATURE";
}
```

---

## `@FeatureEvaluate` Annotation

| Attribute | Type | Default | Description |
|---|---|---|---|
| `featureName` | `String` | *(required)* | Name of the feature flag to evaluate |
| `byPass` | `boolean` | `false` | Skip evaluation entirely — method always executes |
| `ifMissing` | `boolean` | `false` | Return value when no flag is found for the given scope |
| `isGlobal` | `boolean` | `false` | Include the global flag map in the evaluation |
| `considerCarrier` | `boolean` | `false` | Include carrier-scoped flags — **not yet implemented** |
| `carrierExpression` | `String` | `""` | SpEL expression to extract carrier from method args — **not yet implemented** |
| `disabledMessage` | `String` | `""` | When non-empty and the feature is disabled, throws `AccelaeroFeatureDisabledException` with this message instead of returning `null` |

### Disabled Behaviour

When the feature is evaluated as disabled, the aspect reacts based on `disabledMessage`:

| `disabledMessage` | Behaviour |
|---|---|
| `""` (default) | Logs a `WARN` and returns `null` from the intercepted method |
| non-empty string | Throws `AccelaeroFeatureDisabledException` with that message |

`AccelaeroFeatureDisabledException` maps to gRPC status `PERMISSION_DENIED`, so callers receive a meaningful error rather than a silent `null`.

```java
// Disabled → returns null silently
@FeatureEvaluate(featureName = Features.SOME_FEATURE)
public SomeDto getItem(String id) { ... }

// Disabled → throws AccelaeroFeatureDisabledException("This feature is not available for your tenant")
@FeatureEvaluate(
    featureName = Features.SOME_FEATURE,
    disabledMessage = "This feature is not available for your tenant"
)
public SomeDto getItem(String id) { ... }
```

Use `disabledMessage` whenever the caller needs to distinguish a disabled feature from a legitimate `null` result, or when a missing result would cause a downstream `NullPointerException`.

### Evaluation logic (OR chain)

```
enabled = (isGlobal && globalFlagValue)
       || carrierFlagValue            ← always false until implemented
       || tenantFlagValue             ← always evaluated; falls back to ifMissing if not found
```

> Flags registered without a tenant or carrier (i.e. `new FeatureFlag(name, enabled)`) are stored in the global map only. To evaluate them via the aspect, `isGlobal = true` must be set on the annotation — otherwise the tenant lookup finds nothing and returns `ifMissing` (default `false`).

---

## Usage Examples

### Gate an entire service class

Every method in the class is blocked when the flag is disabled. Returns `null` if disabled.

```java
@Service
@FeatureEvaluate(featureName = Features.SOME_FEATURE, isGlobal = true)
public class SomeServiceImpl implements SomeService {
    // all methods are gated
}
```

### Override a method — bypass the class-level gate

When both the class and a method carry `@FeatureEvaluate`, the method annotation takes precedence.

```java
@Override
@FeatureEvaluate(featureName = Features.SOME_FEATURE, byPass = true)
public SomeResult getConfig() {
    // always executes regardless of the class-level annotation
}
```

### Gate with a safe default when the flag is absent

```java
@FeatureEvaluate(featureName = Features.SOME_FEATURE, isGlobal = true, ifMissing = true)
public void doSomething() {
    // runs if flag is enabled OR if flag is not configured at all
}
```

---

## Flag Scoping

Flags are registered from `FeatureFlagContextSupplier.getFeatureFlags()` at startup. The scope is determined by which fields are set on the `FeatureFlag` object:

| Constructor | Populated maps |
|---|---|
| `new FeatureFlag(name, enabled)` | global only |
| `new FeatureFlag(tenant, null, name, enabled)` | global + tenant |
| `new FeatureFlag(null, carrier, name, enabled)` | global + carrier |
| `new FeatureFlag(tenant, carrier, name, enabled)` | global + tenant + carrier |

Every flag always lands in the global map. Tenant and carrier maps are additional indexes on top.

---

## application.yml Configuration

```yaml
aero:
  ops:
    tenant: ${AERO_OPS_TENANT:}
    ff:
      some-feature: ${AERO_OPS_FF_SOME_FEATURE:true}
      another-feature: ${AERO_OPS_FF_ANOTHER_FEATURE:false}
```

Environment variable overrides at deployment time control the flag state per environment.

---

## Known Limitations

### 1. Carrier evaluation is not implemented

`considerCarrier = true` is silently ignored — `carrierStatus` is hardcoded to `false` in `FeatureEvaluatorAspect.shouldProcess()`.

**Impact:** Any annotation that relies solely on `considerCarrier = true` (with `isGlobal = false`) will always evaluate to disabled, regardless of the flag value.

**Workaround:** Add `isGlobal = true` alongside `considerCarrier = true` until carrier evaluation is implemented.

### 2. `null` returned for disabled non-void methods

When a feature is disabled and `disabledMessage` is empty, the aspect returns `null`. For `void` methods this is harmless. For methods returning a `List`, `Map`, or any object, callers that do not null-check will throw `NullPointerException`.

**Recommendation:** Set `disabledMessage` to a non-empty string on any annotation where `null` would be dangerous — the aspect will then throw `AccelaeroFeatureDisabledException` (gRPC `PERMISSION_DENIED`) instead. Otherwise callers must handle `null` return values.

### 3. Flags are a startup snapshot

`FeatureFlagHolder` is populated once at startup from the values supplied by `FeatureFlagContextSupplier`. Changing an environment variable at runtime has no effect — a restart is required. There is currently no refresh mechanism.

### 4. `isGlobal` does not suppress tenant evaluation

Setting `isGlobal = true` does not prevent the tenant check from also running. The evaluation is always `global OR carrier OR tenant`. If a tenant-scoped entry exists for the same flag name, it can independently enable the feature even when `isGlobal = false`. This may cause unexpected behaviour once tenant-scoped flags are introduced.
