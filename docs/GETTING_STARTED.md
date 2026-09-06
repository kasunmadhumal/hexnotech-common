# Getting Started with Hexnotech Commons

This guide will help you get up and running with Hexnotech Commons in your project.

## Prerequisites

Before you start, ensure you have:

- **Java 21** or higher installed
- **Gradle 7.0** or higher
- **Git** for cloning the repository
- A Hexnotech project (Spring Boot microservice)

Verify your Java version:
```bash
java -version
```

Verify Gradle is installed:
```bash
gradle --version
```

## Installation

### Step 1: Add Dependency

Add Hexnotech Commons to your project's `build.gradle`:

```gradle
dependencies {
    implementation 'com.hexnotech:hexnotech-commons:1.0.0'
}
```

### Step 2: Enable Spring Boot Auto-Configuration

Hexnotech Commons provides Spring Boot auto-configuration. Add this to your `application.yml` or `application.properties`:

```yaml
# Optional: Configure library features
hexnotech:
  commons:
    enabled: true
```

### Step 3: Verify Installation

Create a simple test class to verify the library is working:

```java
import com.hexnotech.commons.exception.BusinessException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

## Basic Configuration

### Application Properties

Configure library behavior in `application.yml`:

```yaml
spring:
  application:
    name: my-service

hexnotech:
  commons:
    # Feature flags configuration
    feature-flags:
      enabled: true
    
    # Monitoring configuration
    monitoring:
      enabled: true
      
    # Logging configuration
    logging:
      level: INFO
```

### Spring Beans

The library auto-registers common beans. Access them via dependency injection:

```java
import com.hexnotech.commons.service.BaseService;
import org.springframework.stereotype.Service;

@Service
public class MyService extends BaseService {
    // Your service implementation
}
```

## Common Patterns

### Using Base Entity

```java
import com.hexnotech.commons.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;

@Entity
public class User extends BaseEntity {
    private String name;
    private String email;
    
    // Getters and setters
}
```

### Using Custom Exceptions

```java
import com.hexnotech.commons.exception.BusinessException;
import com.hexnotech.commons.exception.NotFoundException;

public class UserService {
    public User getUser(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found"));
    }
    
    public void createUser(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("Email already exists");
        }
    }
}
```

### Using Feature Flags

```java
import com.hexnotech.commons.ff.FeatureFlag;
import org.springframework.stereotype.Service;

@Service
public class FeatureService {
    
    @FeatureFlag("new-user-flow")
    public void newUserFlow() {
        // Only executed if feature flag is enabled
    }
    
    public void conditionalFeature() {
        if (featureFlagEvaluator.isEnabled("beta-feature")) {
            // Execute beta feature
        }
    }
}
```

## Project Structure

Your project structure should look like this:

```
my-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/mycompany/myservice/
│   │   │       ├── Application.java
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── repository/
│   │   │       └── entity/
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── java/
├── build.gradle
└── README.md
```

## Next Steps

1. Review [Modules Overview](MODULES.md) to understand available components
2. Check [Usage Examples](USAGE_EXAMPLES.md) for code patterns
3. See [Build & Publish](BUILD_AND_PUBLISH.md) for development commands

## Troubleshooting

### Dependency Resolution Issues

If Gradle can't find the library:

```bash
# Clear Gradle cache
gradle clean

# Refresh dependencies
gradle --refresh-dependencies build
```

### Spring Auto-Configuration Not Working

Ensure your main application class has `@SpringBootApplication`:

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### Missing Java Classes

Verify Java 21 is being used:

```bash
gradle --version
java -version
```

## Support

For issues or questions:
1. Check the [Modules Overview](MODULES.md)
2. Review [Usage Examples](USAGE_EXAMPLES.md)
3. Refer to the existing documentation in `src/main/java/com/hexnotech/commons/docs/`
