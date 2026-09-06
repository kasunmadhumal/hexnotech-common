# Build & Publish Guide

This guide covers building, testing, and publishing the Hexnotech Commons library.

## Prerequisites

- Java 21 or higher
- Gradle 7.0 or higher
- Git (for version control)
- GitHub account (for publishing to GitHub Packages)

## Build Commands

### Clean Build

Remove all build artifacts and start fresh:

```bash
gradle clean
```

### Compile Source Code

Compile Java source files without running tests:

```bash
gradle classes
```

### Build the Library

Build the library with all jar files (source jar and javadoc jar):

```bash
gradle build
```

Output files will be in `build/libs/`:
- `hexnotech-commons-1.0.0.jar` - Main library JAR
- `hexnotech-commons-1.0.0-sources.jar` - Source code JAR
- `hexnotech-commons-1.0.0-javadoc.jar` - Javadoc JAR

### Build Without Tests

Build the library skipping all tests:

```bash
gradle build -x test
```

## Testing

### Run All Tests

Execute all unit and integration tests:

```bash
gradle test
```

### Run Tests with Coverage

Generate test coverage report:

```bash
gradle test --info
```

Test results are available in: `build/reports/tests/test/index.html`

### Run Specific Test Class

```bash
gradle test --tests com.hexnotech.commons.ExceptionHandlerTest
```

### Run Specific Test Method

```bash
gradle test --tests com.hexnotech.commons.ExceptionHandlerTest.testBusinessException
```

### Rebuild Tests

Clean test artifacts and rebuild:

```bash
gradle cleanTest test
```

## Documentation

### Generate Javadoc

Generate API documentation:

```bash
gradle javadoc
```

Javadoc will be available at: `build/docs/javadoc/index.html`

### Generate Reports

Generate build reports:

```bash
gradle build --info
```

## Dependency Management

### List Dependencies

View all project dependencies:

```bash
gradle dependencies
```

### List Dependency Tree

Display hierarchical dependency tree:

```bash
gradle dependencies --configuration compileClasspath
```

### Resolve Dependency Conflicts

Refresh dependencies and resolve conflicts:

```bash
gradle --refresh-dependencies build
```

## Publishing

### Publish to Maven Local

Publish to local Maven repository (`~/.m2/repository/`):

```bash
gradle publish
```

This creates artifacts that can be used by other local projects:

```gradle
repositories {
    mavenLocal()
}

dependencies {
    implementation 'com.hexnotech:hexnotech-commons:1.0.0'
}
```

### Publish to GitHub Packages

Configure GitHub credentials:

```bash
export GITHUB_ACTOR=<your-github-username>
export GITHUB_TOKEN=<your-github-personal-access-token>
```

Generate a GitHub Personal Access Token with `read:packages` and `write:packages` permissions at:
https://github.com/settings/tokens

Then publish:

```bash
gradle publish
```

### Configure GitHub Packages in build.gradle

To enable GitHub Packages publishing, uncomment the GitHub Packages repository section in `build.gradle`:

```gradle
publishing {
    repositories {
        maven {
            name = 'GitHubPackages'
            url = uri('https://maven.pkg.github.com/hexnotech/hexnotech-commons')
            credentials {
                username = System.getenv('GITHUB_ACTOR')
                password = System.getenv('GITHUB_TOKEN')
            }
        }
    }
}
```

## Version Management

### View Current Version

```bash
grep "version = " build.gradle
```

Current version: **1.0.0**

### Update Version

Edit `build.gradle` and update the version:

```gradle
version = '1.0.1'  // Update this line
```

Then publish with the new version.

## Gradle Wrapper

### Use Gradle Wrapper (Recommended)

The project includes Gradle wrapper scripts:

**On macOS/Linux:**
```bash
./gradlew build
./gradlew test
./gradlew publish
```

**On Windows:**
```bash
gradlew.bat build
gradlew.bat test
gradlew.bat publish
```

### Update Gradle Wrapper

Update to a newer Gradle version:

```bash
gradle wrapper --gradle-version=8.0
```

## IDE Integration

### IntelliJ IDEA

1. Open the project in IntelliJ
2. Import as Gradle project
3. IntelliJ will automatically download dependencies
4. Run configurations are available in the IDE

### Eclipse

1. Install Gradle IDE plugin
2. Import as existing Gradle project
3. Dependencies will be resolved automatically

### VS Code

1. Install "Extension Pack for Java"
2. Open the project folder
3. VS Code will recognize it as a Gradle project

## Troubleshooting

### Build Failures

Clear cache and rebuild:

```bash
gradle clean build --refresh-dependencies
```

### Gradle Permission Denied

Make wrapper executable:

```bash
chmod +x gradlew
```

### Dependency Download Issues

Check internet connection and Gradle cache:

```bash
gradle build --refresh-dependencies
```

### Memory Issues

Increase Gradle heap size:

```bash
export GRADLE_OPTS="-Xmx2048m"
gradle build
```

### Test Failures

Run tests with verbose output:

```bash
gradle test --info
```

## Continuous Integration

For GitHub Actions CI/CD, create `.github/workflows/build.yml`:

```yaml
name: Build

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      - run: ./gradlew build
      - run: ./gradlew publish
        if: startsWith(github.ref, 'refs/tags/')
        env:
          GITHUB_ACTOR: ${{ github.actor }}
          GITHUB_TOKEN: ${{ github.token }}
```

## Common Tasks Summary

| Task | Command |
|------|---------|
| Build library | `gradle build` |
| Run tests | `gradle test` |
| Generate docs | `gradle javadoc` |
| Publish locally | `gradle publish` |
| Clean build | `gradle clean build` |
| View dependencies | `gradle dependencies` |

## Next Steps

- Review [Modules Overview](MODULES.md)
- Check [Usage Examples](USAGE_EXAMPLES.md)
- See [Getting Started](GETTING_STARTED.md) for configuration
