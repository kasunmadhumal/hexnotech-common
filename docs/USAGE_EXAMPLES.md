# Usage Examples

Practical code examples demonstrating how to use Hexnotech Commons components.

## Table of Contents

1. [Entity & Repository Setup](#entity--repository-setup)
2. [Service Layer](#service-layer)
3. [REST Controller](#rest-controller)
4. [Exception Handling](#exception-handling)
5. [Feature Flags](#feature-flags)
6. [Security](#security)
7. [Async Processing & Jobs](#async-processing--jobs)
8. [Type Conversions](#type-conversions)
9. [Validation](#validation)
10. [Logging](#logging)

---

## Entity & Repository Setup

### Define an Entity

```java
package com.example.service.entity;

import com.hexnotech.commons.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends BaseEntity {
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;
    
    @Column
    private String phone;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerStatus status;
}
```

### Create a Repository

```java
package com.example.service.repository;

import com.hexnotech.commons.jpa.repository.BaseRepository;
import com.example.service.entity.Customer;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface CustomerRepository extends BaseRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    List<Customer> findByStatus(CustomerStatus status);
    boolean existsByEmail(String email);
}
```

---

## Service Layer

### Basic Service Implementation

```java
package com.example.service.service;

import com.hexnotech.commons.service.BaseService;
import com.hexnotech.commons.exception.BusinessException;
import com.hexnotech.commons.exception.NotFoundException;
import com.example.service.entity.Customer;
import com.example.service.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService extends BaseService {
    
    private final CustomerRepository customerRepository;
    
    /**
     * Create a new customer
     */
    @Transactional
    public Customer createCustomer(Customer customer) {
        // Validate email doesn't exist
        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new BusinessException("Email already exists: " + customer.getEmail());
        }
        
        customer.setStatus(CustomerStatus.ACTIVE);
        return customerRepository.save(customer);
    }
    
    /**
     * Get customer by ID
     */
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Customer not found with id: " + id));
    }
    
    /**
     * Get customer by email
     */
    public Customer getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("Customer not found with email: " + email));
    }
    
    /**
     * Update customer
     */
    @Transactional
    public Customer updateCustomer(Long id, Customer updateData) {
        Customer customer = getCustomerById(id);
        
        if (updateData.getFirstName() != null) {
            customer.setFirstName(updateData.getFirstName());
        }
        if (updateData.getLastName() != null) {
            customer.setLastName(updateData.getLastName());
        }
        if (updateData.getPhone() != null) {
            customer.setPhone(updateData.getPhone());
        }
        
        return customerRepository.save(customer);
    }
    
    /**
     * Delete customer
     */
    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = getCustomerById(id);
        customerRepository.delete(customer);
    }
    
    /**
     * Get all active customers
     */
    public List<Customer> getActiveCustomers() {
        return customerRepository.findByStatus(CustomerStatus.ACTIVE);
    }
    
    /**
     * Deactivate customer
     */
    @Transactional
    public Customer deactivateCustomer(Long id) {
        Customer customer = getCustomerById(id);
        customer.setStatus(CustomerStatus.INACTIVE);
        return customerRepository.save(customer);
    }
}
```

---

## REST Controller

### Customer Controller

```java
package com.example.service.controller;

import com.hexnotech.commons.controller.BaseController;
import com.example.service.dto.CustomerRequest;
import com.example.service.dto.CustomerResponse;
import com.example.service.entity.Customer;
import com.example.service.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController extends BaseController {
    
    private final CustomerService customerService;
    
    /**
     * Create a new customer
     * POST /api/v1/customers
     */
    @PostMapping
    public ResponseEntity<?> createCustomer(@Valid @RequestBody CustomerRequest request) {
        Customer customer = Customer.builder()
            .email(request.getEmail())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .phone(request.getPhone())
            .build();
        
        Customer created = customerService.createCustomer(customer);
        return successResponse(toResponse(created), HttpStatus.CREATED);
    }
    
    /**
     * Get customer by ID
     * GET /api/v1/customers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomer(@PathVariable Long id) {
        Customer customer = customerService.getCustomerById(id);
        return successResponse(toResponse(customer));
    }
    
    /**
     * Get customer by email
     * GET /api/v1/customers/email/{email}
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getCustomerByEmail(@PathVariable String email) {
        Customer customer = customerService.getCustomerByEmail(email);
        return successResponse(toResponse(customer));
    }
    
    /**
     * Update customer
     * PUT /api/v1/customers/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {
        Customer updateData = Customer.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .phone(request.getPhone())
            .build();
        
        Customer updated = customerService.updateCustomer(id, updateData);
        return successResponse(toResponse(updated));
    }
    
    /**
     * Delete customer
     * DELETE /api/v1/customers/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return successResponse(null);
    }
    
    /**
     * Get all active customers
     * GET /api/v1/customers
     */
    @GetMapping
    public ResponseEntity<?> getActiveCustomers() {
        List<Customer> customers = customerService.getActiveCustomers();
        return successResponse(customers.stream().map(this::toResponse).toList());
    }
    
    /**
     * Deactivate customer
     * PATCH /api/v1/customers/{id}/deactivate
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateCustomer(@PathVariable Long id) {
        Customer customer = customerService.deactivateCustomer(id);
        return successResponse(toResponse(customer));
    }
    
    private CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
            .id(customer.getId())
            .email(customer.getEmail())
            .firstName(customer.getFirstName())
            .lastName(customer.getLastName())
            .phone(customer.getPhone())
            .status(customer.getStatus())
            .createdAt(customer.getCreatedAt())
            .updatedAt(customer.getUpdatedAt())
            .build();
    }
}
```

---

## Exception Handling

### Using Built-in Exceptions

```java
package com.example.service.service;

import com.hexnotech.commons.exception.*;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    
    public Order placeOrder(OrderRequest request) {
        // NotFoundException example
        Customer customer = customerRepository.findById(request.getCustomerId())
            .orElseThrow(() -> new NotFoundException("Customer not found"));
        
        // BusinessException example
        if (customer.getStatus() == CustomerStatus.INACTIVE) {
            throw new BusinessException("Cannot place order for inactive customer");
        }
        
        // ValidationException example
        if (request.getItems().isEmpty()) {
            throw new ValidationException("Order must contain at least one item");
        }
        
        // ConflictException example
        if (orderRepository.existsByCustomerAndDate(customer, LocalDate.now())) {
            throw new ConflictException("Customer already has an order today");
        }
        
        Order order = new Order();
        order.setCustomer(customer);
        order.setItems(request.getItems());
        return orderRepository.save(order);
    }
}
```

### Global Exception Handler (Auto-configured)

The library automatically provides a global exception handler that converts exceptions to HTTP responses:

```
BusinessException          → 400 Bad Request
NotFoundException          → 404 Not Found
ValidationException        → 422 Unprocessable Entity
UnauthorizedException      → 401 Unauthorized
ConflictException          → 409 Conflict
```

Example response:
```json
{
    "success": false,
    "message": "Customer not found",
    "error": "NotFoundException",
    "timestamp": "2024-01-15T10:30:00Z"
}
```

---

## Feature Flags

### Method-Level Feature Flags

```java
package com.example.service.service;

import com.hexnotech.commons.ff.FeatureFlag;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    
    /**
     * New payment processing flow - feature flagged
     */
    @FeatureFlag("new-payment-flow")
    public PaymentResult processPaymentNew(Payment payment) {
        // New implementation with advanced features
        return processWithNewLogic(payment);
    }
    
    /**
     * Legacy payment processing - fallback
     */
    @FeatureFlag("legacy-payment-flow")
    public PaymentResult processPaymentLegacy(Payment payment) {
        // Legacy implementation for backward compatibility
        return processWithLegacyLogic(payment);
    }
    
    /**
     * Process payment - intelligently chooses implementation
     */
    public PaymentResult processPayment(Payment payment) {
        if (featureFlagEvaluator.isEnabled("new-payment-flow")) {
            return processPaymentNew(payment);
        } else if (featureFlagEvaluator.isEnabled("legacy-payment-flow")) {
            return processPaymentLegacy(payment);
        } else {
            throw new BusinessException("No payment processor available");
        }
    }
}
```

### Conditional Logic with Feature Flags

```java
@Service
public class NotificationService {
    
    private final FeatureFlagEvaluator featureFlagEvaluator;
    
    public void sendOrderNotification(Order order) {
        // Basic notification - always sent
        sendBasicNotification(order);
        
        // Enhanced notification - feature flagged
        if (featureFlagEvaluator.isEnabled("enhanced-notifications")) {
            sendEnhancedNotification(order);
        }
        
        // SMS notification - experimental
        if (featureFlagEvaluator.isEnabled("sms-notifications", true)) {  // true = default
            sendSmsNotification(order);
        }
    }
}
```

---

## Security

### Using Security Utilities

```java
package com.example.service.service;

import com.hexnotech.commons.security.SecurityUtils;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {
    
    /**
     * Get current user's profile
     */
    public UserProfile getCurrentUserProfile() {
        User currentUser = SecurityUtils.getCurrentUser();
        return userRepository.findById(currentUser.getId()).orElse(null);
    }
    
    /**
     * Update profile with authorization check
     */
    public UserProfile updateProfile(Long userId, UserProfile updateData) {
        User currentUser = SecurityUtils.getCurrentUser();
        
        // Verify user can only update their own profile
        if (!currentUser.getId().equals(userId) && !currentUser.hasRole("ADMIN")) {
            throw new UnauthorizedException("Cannot update another user's profile");
        }
        
        UserProfile profile = userRepository.findById(userId).orElseThrow();
        profile.setName(updateData.getName());
        profile.setEmail(updateData.getEmail());
        return userRepository.save(profile);
    }
    
    /**
     * Admin-only operation
     */
    public void deleteUserAccount(Long userId) {
        User currentUser = SecurityUtils.getCurrentUser();
        
        if (!currentUser.hasRole("ADMIN")) {
            throw new UnauthorizedException("Only administrators can delete accounts");
        }
        
        userRepository.deleteById(userId);
    }
}
```

### Role-Based Access in Controller

```java
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController extends BaseController {
    
    @GetMapping("/users")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<?> getAllUsers() {
        // Only accessible to ADMIN role
        List<User> users = userService.getAllUsers();
        return successResponse(users);
    }
    
    @PostMapping("/config")
    @Secured("ROLE_SUPER_ADMIN")
    public ResponseEntity<?> updateSystemConfig(
            @RequestBody SystemConfig config) {
        // Only accessible to SUPER_ADMIN role
        SystemConfig updated = configService.updateConfig(config);
        return successResponse(updated);
    }
}
```

---

## Async Processing & Jobs

### Using Job Executor

```java
package com.example.service.service;

import com.hexnotech.commons.monitoring.job.JobExecutor;
import org.springframework.stereotype.Service;

@Service
public class ReportService {
    
    private final JobExecutor jobExecutor;
    private final ReportRepository reportRepository;
    
    /**
     * Generate report asynchronously
     */
    public void generateReportAsync(Long reportId) {
        jobExecutor.executeAsync(
            () -> generateReport(reportId),
            "report-generation-" + reportId
        );
    }
    
    /**
     * Actual report generation - runs in background
     */
    private void generateReport(Long reportId) {
        Report report = reportRepository.findById(reportId).orElseThrow();
        
        try {
            // Long-running operation
            report.setData(collectReportData());
            report.setStatus(ReportStatus.COMPLETED);
        } catch (Exception e) {
            report.setStatus(ReportStatus.FAILED);
            report.setErrorMessage(e.getMessage());
        }
        
        reportRepository.save(report);
    }
    
    private List<ReportData> collectReportData() {
        // Time-consuming operation
        return generateData();
    }
}
```

### Using Job Monitor

```java
@Service
public class DataProcessingService {
    
    private final JobMonitor jobMonitor;
    
    public void processLargeDataset(Long datasetId) {
        String jobId = "process-dataset-" + datasetId;
        
        try {
            jobMonitor.startJob(jobId, "Processing large dataset");
            
            Dataset dataset = datasetRepository.findById(datasetId).orElseThrow();
            List<DataRecord> records = dataset.getRecords();
            
            int total = records.size();
            for (int i = 0; i < total; i++) {
                processRecord(records.get(i));
                
                // Update progress
                jobMonitor.updateProgress(jobId, i + 1, total);
            }
            
            jobMonitor.completeJob(jobId, "Dataset processing completed");
        } catch (Exception e) {
            jobMonitor.failJob(jobId, e.getMessage());
        }
    }
}
```

---

## Type Conversions

### Using Generic Result Type

```java
package com.example.service.dto;

import com.hexnotech.commons.type.Result;

public class ApiResponse {
    
    /**
     * Success response
     */
    public static <T> Result<T> success(T data) {
        return Result.success(data);
    }
    
    /**
     * Error response
     */
    public static <T> Result<T> error(String message) {
        return Result.error(message);
    }
}
```

### Using Paged Response

```java
package com.example.service.controller;

import com.hexnotech.commons.type.PagedResponse;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
    
    @GetMapping
    public ResponseEntity<?> listCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        List<Customer> customers = customerService.getCustomers(page, size);
        long total = customerService.getTotalCustomers();
        
        PagedResponse<CustomerResponse> response = PagedResponse.<CustomerResponse>builder()
            .data(customers.stream().map(this::toResponse).toList())
            .page(page)
            .pageSize(size)
            .total(total)
            .totalPages((int) Math.ceil((double) total / size))
            .build();
        
        return successResponse(response);
    }
}
```

---

## Validation

### Input Validation

```java
package com.example.service.dto;

import jakarta.validation.constraints.*;

public class CustomerRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be 2-50 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be 2-50 characters")
    private String lastName;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number is invalid")
    private String phone;
    
    // Getters and setters
}
```

### Custom Validation

```java
package com.example.service.validator;

import com.hexnotech.commons.exception.ValidationException;

public class CustomerValidator {
    
    public static void validateCustomer(Customer customer) {
        if (customer.getEmail() == null || customer.getEmail().isEmpty()) {
            throw new ValidationException("Email cannot be empty");
        }
        
        if (customer.getFirstName() == null || customer.getFirstName().length() < 2) {
            throw new ValidationException("First name must be at least 2 characters");
        }
        
        if (customer.getLastName() == null || customer.getLastName().length() < 2) {
            throw new ValidationException("Last name must be at least 2 characters");
        }
    }
}
```

---

## Logging

### Using Logging Utilities

```java
package com.example.service.service;

import com.hexnotech.commons.util.LoggerUtil;
import org.slf4j.Logger;

@Service
public class OrderService {
    
    private static final Logger log = LoggerUtil.getLogger(OrderService.class);
    
    public Order createOrder(OrderRequest request) {
        log.info("Creating order for customer: {}", request.getCustomerId());
        
        try {
            Customer customer = customerService.getCustomer(request.getCustomerId());
            log.debug("Customer found: {} ({})", customer.getId(), customer.getEmail());
            
            Order order = new Order();
            order.setCustomer(customer);
            order.setItems(request.getItems());
            
            Order saved = orderRepository.save(order);
            log.info("Order created successfully with id: {}", saved.getId());
            
            return saved;
        } catch (NotFoundException e) {
            log.error("Customer not found for order creation", e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while creating order", e);
            throw new BusinessException("Failed to create order");
        }
    }
}
```

---

## Complete Example: Customer Management Service

Here's a complete, production-ready example combining multiple components:

```java
// Entity
@Entity
@Table(name = "customers")
@Data
public class Customer extends BaseEntity {
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    private CustomerStatus status;
}

// Repository
@Repository
public interface CustomerRepository extends BaseRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
}

// Service
@Service
@RequiredArgsConstructor
public class CustomerService extends BaseService {
    
    private final CustomerRepository repository;
    private static final Logger log = LoggerUtil.getLogger(CustomerService.class);
    
    @Transactional
    public Customer create(CustomerRequest request) {
        log.info("Creating customer: {}", request.getEmail());
        
        if (repository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists");
        }
        
        Customer customer = Customer.builder()
            .email(request.getEmail())
            .name(request.getName())
            .status(CustomerStatus.ACTIVE)
            .build();
        
        Customer saved = repository.save(customer);
        log.info("Customer created: {}", saved.getId());
        return saved;
    }
    
    public Customer getById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Customer not found"));
    }
}

// Controller
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController extends BaseController {
    
    private final CustomerService service;
    
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CustomerRequest request) {
        Customer customer = service.create(request);
        return successResponse(customer, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        Customer customer = service.getById(id);
        return successResponse(customer);
    }
}
```

---

## Next Steps

- Review [Modules Overview](MODULES.md) for more details
- Check [Getting Started](GETTING_STARTED.md) for setup instructions
- See [Build & Publish](BUILD_AND_PUBLISH.md) for deployment commands
