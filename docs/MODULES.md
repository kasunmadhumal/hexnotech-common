# Modules Overview

Hexnotech Commons is organized into several modules, each providing specific functionality for backend services.

## Module Structure

```
com.hexnotech.commons/
├── constants/          # Constants and enumerations
├── controller/         # Base controller classes
├── exception/          # Custom exception hierarchy
├── ff/                 # Feature flag system
├── jpa/                # JPA utilities and base entities
├── monitoring/         # Job monitoring and execution
├── security/           # Security utilities
├── service/            # Base service classes
├── type/               # Custom data types
└── util/               # General-purpose utilities
```

---

## Constants Module

**Package:** `com.hexnotech.commons.constants`

Provides application-wide constants and enumerations used across services.

### Key Components

- **Status.java** - Common status enumerations (ACTIVE, INACTIVE, PENDING, etc.)
- **Common constants** - HTTP status codes, error messages, default values
- **Enums** - Type-safe enumerations for various domains

### Usage Example

```java
import com.hexnotech.commons.constants.Status;

public class UserService {
    public void activateUser(User user) {
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);
    }
}
```

---

## Controller Module

**Package:** `com.hexnotech.commons.controller`

Provides base controller classes and common controller functionality.

### Key Components

- **BaseController** - Abstract base class for REST controllers
- **Response handling** - Standardized API response format
- **Request mapping** - Common endpoint patterns

### Usage Example

```java
import com.hexnotech.commons.controller.BaseController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController extends BaseController {
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        User user = userService.getUser(id);
        return successResponse(user);
    }
}
```

---

## Exception Module

**Package:** `com.hexnotech.commons.exception`

Provides a hierarchy of custom exceptions for consistent error handling.

### Key Exception Classes

- **BusinessException** - General business logic errors
- **NotFoundException** - Resource not found errors
- **ValidationException** - Input validation errors
- **UnauthorizedException** - Authentication/authorization failures
- **ConflictException** - Resource conflict errors (e.g., duplicate entries)

### Usage Example

```java
import com.hexnotech.commons.exception.NotFoundException;
import com.hexnotech.commons.exception.BusinessException;

public class OrderService {
    public Order getOrder(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Order not found"));
    }
    
    public void cancelOrder(Long id) {
        Order order = getOrder(id);
        if (order.isShipped()) {
            throw new BusinessException("Cannot cancel shipped orders");
        }
        order.setCancelled(true);
        orderRepository.save(order);
    }
}
```

### Exception Handling

Global exception handler automatically converts exceptions to appropriate HTTP responses:

```
BusinessException          → 400 Bad Request
NotFoundException          → 404 Not Found
ValidationException        → 422 Unprocessable Entity
UnauthorizedException      → 401 Unauthorized
ConflictException          → 409 Conflict
Exception (generic)        → 500 Internal Server Error
```

---

## Feature Flags Module

**Package:** `com.hexnotech.commons.ff`

Enables dynamic feature flag management without code changes.

### Key Components

- **FeatureFlag** - Annotation for method-level feature flags
- **FeatureFlagEvaluator** - Runtime feature flag evaluation
- **FeatureFlagProvider** - Strategy for loading flag configurations

### Usage Example

```java
import com.hexnotech.commons.ff.FeatureFlag;
import org.springframework.stereotype.Service;

@Service
public class CheckoutService {
    
    @FeatureFlag("new-checkout-flow")
    public void newCheckoutFlow(Order order) {
        // New checkout implementation
    }
    
    @FeatureFlag("legacy-checkout-flow")
    public void legacyCheckoutFlow(Order order) {
        // Legacy checkout implementation
    }
    
    public void processCheckout(Order order) {
        // Decides which flow to use based on feature flags
        if (featureFlagEvaluator.isEnabled("new-checkout-flow")) {
            newCheckoutFlow(order);
        } else {
            legacyCheckoutFlow(order);
        }
    }
}
```

See [Feature-Flags.md](../src/main/java/com/hexnotech/commons/docs/Feature-Flags.md) for detailed documentation.

---

## JPA Module

**Package:** `com.hexnotech.commons.jpa`

Provides JPA/Hibernate utilities and base entity classes.

### Key Components

- **BaseEntity** - Abstract base class with common fields (id, createdAt, updatedAt)
- **BaseRepository** - Base interface for all repositories
- **Custom Type Handlers** - Type converters for non-standard database types
- **Query Utilities** - Common query patterns and helpers

### Usage Example

```java
import com.hexnotech.commons.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends BaseEntity {
    private String name;
    private String email;
    
    // Getters and setters
}

// Repository
import com.hexnotech.commons.jpa.repository.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends BaseRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
```

### BaseEntity Features

```java
public abstract class BaseEntity {
    private Long id;                    // Primary key
    private Instant createdAt;          // Creation timestamp
    private Instant updatedAt;          // Last update timestamp
    private String createdBy;           // User who created
    private String updatedBy;           // User who last updated
}
```

See [JPA-Type.md](../src/main/java/com/hexnotech/commons/docs/JPA-Type.md) for detailed JPA type documentation.

---

## Monitoring Module

**Package:** `com.hexnotech.commons.monitoring`

Provides job monitoring, execution, and async process handling.

### Key Components

- **JobExecutor** - Executes background jobs with monitoring
- **JobMonitor** - Tracks job progress and status
- **AsyncProcess** - Handles asynchronous processing
- **JobStatus** - Job state management

### Usage Example

```java
import com.hexnotech.commons.monitoring.job.JobExecutor;
import org.springframework.stereotype.Service;

@Service
public class ReportService {
    
    private JobExecutor jobExecutor;
    
    public void generateReport(Long reportId) {
        jobExecutor.executeAsync(() -> {
            // Long-running report generation
            generateComplexReport(reportId);
        }, "report-generation-" + reportId);
    }
}
```

See [JOB_MONITORING.md](../src/main/java/com/hexnotech/commons/docs/JOB_MONITORING.md) for detailed documentation.

---

## Security Module

**Package:** `com.hexnotech.commons.security`

Provides security utilities for authentication and authorization.

### Key Components

- **SecurityContext** - Current user and authentication info
- **SecurityUtils** - Common security operations
- **CarrierSecurity** - Carrier-level security handling
- **Permission handlers** - Role-based access control

### Usage Example

```java
import com.hexnotech.commons.security.SecurityUtils;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    
    public User getCurrentUser() {
        return SecurityUtils.getCurrentUser();
    }
    
    public boolean canAccessUser(Long userId) {
        User currentUser = SecurityUtils.getCurrentUser();
        return currentUser.getId().equals(userId) || 
               currentUser.hasRole("ADMIN");
    }
}
```

See [Carrier-Security.md](../src/main/java/com/hexnotech/commons/docs/Carrier-Security.md) for detailed security documentation.

---

## Service Module

**Package:** `com.hexnotech.commons.service`

Provides base service classes with common patterns.

### Key Components

- **BaseService** - Abstract service with common CRUD operations
- **ServiceTemplate** - Service execution template
- **Validation utilities** - Input validation helpers

### Usage Example

```java
import com.hexnotech.commons.service.BaseService;
import org.springframework.stereotype.Service;

@Service
public class ProductService extends BaseService {
    
    private ProductRepository productRepository;
    
    public Product createProduct(Product product) {
        validateProduct(product);
        return productRepository.save(product);
    }
    
    public Product getProduct(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Product not found"));
    }
}
```

---

## Type Module

**Package:** `com.hexnotech.commons.type`

Provides custom data types and type converters.

### Key Components

- **Common Types** - Frequently used data structures
- **Type Converters** - Conversion utilities between types
- **Generic Wrappers** - Generic containers and holders
- **Custom Serializers** - Jackson serialization customizations

### Usage Example

```java
import com.hexnotech.commons.type.Result;
import com.hexnotech.commons.type.PagedResponse;

public class UserController {
    
    @GetMapping("/users")
    public Result<PagedResponse<User>> getUsers(
            @RequestParam int page,
            @RequestParam int size) {
        PagedResponse<User> response = userService.getUsers(page, size);
        return Result.success(response);
    }
}
```

See [Common-Types.md](../src/main/java/com/hexnotech/commons/docs/Common-Types.md) for detailed type documentation.

---

## Util Module

**Package:** `com.hexnotech.commons.util`

Provides general-purpose utility classes.

### Key Components

- **StringUtils** - String manipulation helpers
- **DateTimeUtils** - Date and time utilities
- **CollectionUtils** - Collection manipulation helpers
- **ValidationUtils** - Input validation utilities
- **LoggerUtil** - Logging utilities

### Usage Example

```java
import com.hexnotech.commons.util.StringUtils;
import com.hexnotech.commons.util.DateTimeUtils;

public class DataProcessor {
    
    public void process(String input) {
        if (StringUtils.isEmpty(input)) {
            throw new ValidationException("Input cannot be empty");
        }
        
        Instant now = DateTimeUtils.now();
        // Process data
    }
}
```

See [Utility-Classes.md](../src/main/java/com/hexnotech/commons/docs/Utility-Classes.md) for detailed utility documentation.

---

## Cross-Module Features

### Logging

The library provides consistent logging across all modules:

See [Accelaero-Logger.md](../src/main/java/com/hexnotech/commons/docs/Accelaero-Logger.md)

```java
import com.hexnotech.commons.util.LoggerUtil;

public class MyService {
    private static final Logger log = LoggerUtil.getLogger(MyService.class);
    
    public void doSomething() {
        log.info("Starting operation");
        // Implementation
        log.debug("Operation completed");
    }
}
```

### Generic Request Flow

For detailed information on request flow patterns:

See [Generic-Request-Flow.md](../src/main/java/com/hexnotech/commons/docs/Generic-Request-Flow.md)

### AI Integration

For AI-related utilities and patterns:

See [AI-Agent-Help.md](../src/main/java/com/hexnotech/commons/docs/AI-Agent-Help.md)

---

## Integration Guide

### Typical Service Structure

```java
import com.hexnotech.commons.controller.BaseController;
import com.hexnotech.commons.service.BaseService;
import com.hexnotech.commons.jpa.entity.BaseEntity;
import com.hexnotech.commons.exception.NotFoundException;

// Entity
@Entity
public class Product extends BaseEntity {
    private String name;
    private BigDecimal price;
}

// Repository
@Repository
public interface ProductRepository extends BaseRepository<Product, Long> {
    Optional<Product> findByName(String name);
}

// Service
@Service
public class ProductService extends BaseService {
    public Product create(Product product) {
        return productRepository.save(product);
    }
    
    public Product getById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Product not found"));
    }
}

// Controller
@RestController
@RequestMapping("/api/products")
public class ProductController extends BaseController {
    
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Product product) {
        Product created = productService.create(product);
        return successResponse(created);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        Product product = productService.getById(id);
        return successResponse(product);
    }
}
```

---

## Next Steps

- Start with [Getting Started](GETTING_STARTED.md)
- Review [Usage Examples](USAGE_EXAMPLES.md)
- Check [Build & Publish](BUILD_AND_PUBLISH.md) for development commands
