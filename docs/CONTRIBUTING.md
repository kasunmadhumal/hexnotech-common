# Contributing to Hexnotech Commons

Thank you for your interest in contributing to Hexnotech Commons! This guide will help you get started with development and contribution guidelines.

## Development Setup

### Prerequisites

- Java 21+
- Gradle 7.0+
- Git
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

### Clone the Repository

```bash
git clone https://github.com/hexnotech/hexnotech-commons.git
cd hexnotech-commons
```

### Build the Project

```bash
./gradlew build
```

### Run Tests

```bash
./gradlew test
```

### IDE Setup

#### IntelliJ IDEA
1. Open the project: `File → Open`
2. Select the `hexnotech-commons` directory
3. IntelliJ will automatically import Gradle configuration
4. Run tests: `Right-click → Run 'Tests'`

#### Eclipse
1. Import project: `File → Import → Existing Gradle Project`
2. Select the `hexnotech-commons` directory
3. Eclipse will download dependencies automatically

#### VS Code
1. Install "Extension Pack for Java"
2. Open the folder containing `hexnotech-commons`
3. VS Code will detect the Gradle project

## Code Style

### Naming Conventions

- **Classes**: PascalCase (e.g., `CustomerService`, `UserEntity`)
- **Methods**: camelCase (e.g., `getCustomer`, `createUser`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_RETRY_ATTEMPTS`)
- **Variables**: camelCase (e.g., `firstName`, `isActive`)
- **Packages**: lowercase (e.g., `com.hexnotech.commons.service`)

### Java Conventions

```java
// Good: Clear, concise, well-formatted
@Service
@RequiredArgsConstructor
public class CustomerService extends BaseService {
    
    private final CustomerRepository repository;
    
    public Customer getById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Customer not found"));
    }
}
```

### Formatting

- Use spaces (not tabs)
- 4-space indentation
- Max line length: 120 characters
- Use Lombok for boilerplate code reduction

### Comments

Only write comments for the "WHY", not the "WHAT":

```java
// Good: Explains why
// Retry with exponential backoff to handle transient failures
int delayMs = 1000 * (int) Math.pow(2, attempt);

// Avoid: Redundant comment
// Increment attempt count
attempt++;
```

## Making Changes

### 1. Create a Feature Branch

```bash
git checkout -b feature/new-feature
# or
git checkout -b fix/bug-description
```

Branch naming convention:
- `feature/` - new features
- `fix/` - bug fixes
- `docs/` - documentation updates
- `refactor/` - code refactoring
- `test/` - test additions

### 2. Make Your Changes

```bash
# Edit files
vim src/main/java/com/hexnotech/commons/...

# Verify changes
./gradlew build
./gradlew test
```

### 3. Commit Your Changes

```bash
git add .
git commit -m "Description of changes"
```

Commit message format:
- First line: 50 characters max, imperative mood
- Blank line
- Detailed description if needed

```
Add customer validation utility

- Add email format validation
- Add name length validation
- Add phone number validation
- Include comprehensive unit tests
```

### 4. Push and Create Pull Request

```bash
git push origin feature/new-feature
```

Then create a pull request on GitHub with:
- Clear title describing the change
- Description of what changed and why
- Reference to related issues (#123)

## Testing

### Write Tests

All new features must include tests:

```java
public class CustomerServiceTest {
    
    @Mock
    private CustomerRepository repository;
    
    @InjectMocks
    private CustomerService service;
    
    @Test
    void testGetCustomerById_Success() {
        // Arrange
        Customer customer = new Customer();
        customer.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(customer));
        
        // Act
        Customer result = service.getById(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }
    
    @Test
    void testGetCustomerById_NotFound() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(NotFoundException.class, () -> service.getById(1L));
    }
}
```

### Run Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests CustomerServiceTest

# Run specific test method
./gradlew test --tests CustomerServiceTest.testGetCustomerById_Success
```

### Test Coverage

Aim for >80% code coverage:

```bash
./gradlew test --info
# Check results in build/reports/tests/test/index.html
```

## Documentation

### JavaDoc

Add JavaDoc to public classes and methods:

```java
/**
 * Service for managing customers.
 * Provides CRUD operations and business logic for customer management.
 */
@Service
public class CustomerService extends BaseService {
    
    /**
     * Retrieve a customer by their ID.
     * 
     * @param id the customer ID
     * @return the customer if found
     * @throws NotFoundException if customer not found
     */
    public Customer getById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Customer not found"));
    }
}
```

### Update Documentation

If your change affects user-facing functionality:

1. Update relevant `.md` files in `docs/`
2. Update code examples in `docs/USAGE_EXAMPLES.md`
3. Update module descriptions in `docs/MODULES.md`

## Pull Request Process

1. **Format Code**: Ensure code follows style guidelines
2. **Write Tests**: Add comprehensive tests
3. **Update Docs**: Document your changes
4. **Run Full Build**: `./gradlew clean build`
5. **Create PR**: Submit with clear description
6. **Code Review**: Respond to review feedback
7. **Merge**: Once approved, merge to main

## Version Numbering

We follow Semantic Versioning (SemVer):
- `MAJOR.MINOR.PATCH`
- `1.0.0` - First release
- `1.1.0` - New features (backward compatible)
- `1.1.1` - Bug fixes (backward compatible)
- `2.0.0` - Breaking changes

Update version in `build.gradle`:

```gradle
version = '1.1.0'  // Update this line
```

## Common Issues & Solutions

### Build Fails with "Java Version"

Ensure Java 21 is installed:

```bash
java -version
# Update JAVA_HOME if needed
export JAVA_HOME=/path/to/java21
```

### Gradle Build Cache Issues

Clear cache:

```bash
rm -rf .gradle
./gradlew clean build --refresh-dependencies
```

### Tests Failing Unexpectedly

Run with verbose output:

```bash
./gradlew test --info --debug
```

## Getting Help

- Check [Modules Overview](MODULES.md)
- Review [Usage Examples](USAGE_EXAMPLES.md)
- Open an issue on GitHub
- Ask in team discussions

## Code Review Checklist

Before requesting review, ensure:

- [ ] Code follows style guidelines
- [ ] Tests are comprehensive and passing
- [ ] JavaDoc is complete for public methods
- [ ] No hardcoded values or secrets
- [ ] Documentation is updated
- [ ] Commit messages are clear
- [ ] No unnecessary dependencies added

## Merging Guidelines

Only maintainers can merge. Requirements:

1. ✅ All tests passing
2. ✅ Code review approved
3. ✅ CI/CD pipeline successful
4. ✅ Documentation updated
5. ✅ No conflicts with main

## Release Process

1. Update version in `build.gradle`
2. Update [CHANGELOG.md](CHANGELOG.md)
3. Create a pull request
4. Get approval
5. Merge to main
6. Tag release: `git tag v1.0.0`
7. Push tag: `git push origin v1.0.0`
8. GitHub Actions will build and publish

## Questions?

- Review existing documentation in `docs/`
- Check issue discussions on GitHub
- Ask in the team channel

Thank you for contributing! 🎉
