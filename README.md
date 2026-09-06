# Hexnotech Commons

A shared library providing annotations, utilities, base classes, exception handlers, and infrastructure components for all Hexnotech backend services.

## Overview

Hexnotech Commons is a foundational library that provides reusable components to standardize and streamline development across all Hexnotech microservices. It includes:

- **JPA & Database Utilities** - Base entities, custom type handlers, and query helpers
- **Exception Handling** - Centralized exception management and custom exception classes
- **Feature Flags** - Feature flag management and evaluation
- **Monitoring & Job Management** - Job execution, monitoring, and async process handling
- **Security** - Security utilities and carrier-level security handling
- **Type System** - Common data types and type conversions
- **Controllers & Services** - Base controller and service classes
- **Logging** - Custom logging utilities

## Quick Start

### Installation

Add this library as a dependency in your `build.gradle`:

```gradle
dependencies {
    implementation 'com.hexnotech:hexnotech-commons:1.0.0'
}
```

### Basic Usage

```java
// Use base entity classes
@Entity
public class MyEntity extends BaseEntity {
    // Your properties
}

// Use custom exceptions
throw new BusinessException("Error message");

// Use feature flags
@FeatureFlag("my-feature")
public void myFeature() {
    // Feature-flagged code
}
```

## Documentation

- [Getting Started](docs/GETTING_STARTED.md) - Setup and initial configuration
- [Build & Publish](docs/BUILD_AND_PUBLISH.md) - Build, test, and publishing commands
- [Modules Overview](docs/MODULES.md) - Detailed module descriptions
- [Usage Examples](docs/USAGE_EXAMPLES.md) - Code examples for common tasks

## Project Structure

```
hexnotech-common/
├── src/main/java/com/hexnotech/commons/
│   ├── constants/          # Constants and enums
│   ├── controller/         # Base controller classes
│   ├── exception/          # Custom exceptions
│   ├── ff/                 # Feature flag implementation
│   ├── jpa/                # JPA entities and utilities
│   ├── monitoring/         # Job monitoring and execution
│   ├── security/           # Security utilities
│   ├── service/            # Base service classes
│   ├── type/               # Custom types and converters
│   └── util/               # General utilities
├── docs/                   # Documentation files
└── build.gradle            # Gradle build configuration
```

## Requirements

- **Java 21+**
- **Gradle 7.0+**
- **Spring Boot 3.4.5+**

## Building

```bash
# Build the library
gradle build

# Build with tests
gradle test build

# Generate documentation
gradle javadoc
```

## Publishing

The library is configured to publish to Maven local and GitHub Packages:

```bash
# Publish to local Maven repository
gradle publish
```

For GitHub Packages publishing, set environment variables:
```bash
export GITHUB_ACTOR=<your-username>
export GITHUB_TOKEN=<your-token>
gradle publish
```

## Contributing

See [Contributing Guide](docs/CONTRIBUTING.md) for development guidelines.

## License

Proprietary - Hexnotech

## Version

Current version: **1.0.0**

For detailed changes, see [CHANGELOG](docs/CHANGELOG.md).
