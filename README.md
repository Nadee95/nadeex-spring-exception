# nadeex-spring-exception

Structured exception handling and standardized error responses for Spring Boot applications.

## Overview

`nadeex-spring-exception` is a plug-and-play library that provides:

- **Typed domain exceptions** — purpose-built exceptions that map directly to HTTP status codes
- **`GlobalExceptionHandler`** — a `@RestControllerAdvice` that intercepts every exception and returns a consistent `ErrorResponse` JSON body
- **`ExceptionMapper`** — converts exception instances, `@Valid` field errors, and `@Validated` constraint violations into `ErrorResponse` objects
- **`ExceptionAutoConfiguration`** — Spring Boot auto-configuration for zero-config setup
- Built on top of `nadeex-spring-common` for shared types (`ErrorResponse`, `ErrorCode`, `BaseException`)

## Requirements

- Java 21+
- Spring Boot 3.2+
- `nadeex-spring-common:0.1.0`

## Installation

Add the dependency to your `build.gradle.kts`:

```kotlin
repositories {
    mavenLocal()  // for local dev without a token
    maven {
        name = "GitHubPackages-Exception"
        url = uri("https://maven.pkg.github.com/Nadee95/nadeex-spring-exception")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("com.nadeex.spring:exception:0.1.0")
}
```

Or in `pom.xml`:

```xml
<dependency>
    <groupId>com.nadeex.spring</groupId>
    <artifactId>exception</artifactId>
    <version>0.1.0</version>
</dependency>
```

> **Note:** This library depends on `com.nadeex.spring:common:0.1.0`. Make sure it is available in your local Maven repository or configured remote repository.

## Exception Types

### Client exceptions

| Exception                    | HTTP Status                   | Description                                          |
|------------------------------|-------------------------------|------------------------------------------------------|
| `BusinessException`          | `400 Bad Request`             | Business rule or domain constraint violation         |
| `ValidationException`        | `422 Unprocessable Entity`    | Programmatic field-level validation failure          |
| `ResourceNotFoundException`  | `404 Not Found`               | Requested resource does not exist                    |
| `ConflictException`          | `409 Conflict`                | Resource already exists or state conflict            |
| `UnauthorizedException`      | `401 Unauthorized`            | Authentication required or credentials invalid       |
| `ForbiddenException`         | `403 Forbidden`               | Authenticated user lacks permission                  |

### Internal exception

| Exception                     | HTTP Status                   | Description                                          |
|-------------------------------|-------------------------------|------------------------------------------------------|
| `EventSerializationException` | `500 Internal Server Error`   | Outbox/event payload cannot be serialized to JSON — always a programming error, never a client error |

## Usage

No configuration is required. Once the dependency is on the classpath, `ExceptionAutoConfiguration` registers
`GlobalExceptionHandler` automatically via
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` (Spring Boot 3 style).

### Throwing exceptions

```java
// Business rule violation → 400
throw new BusinessException("Case is already closed and cannot be modified");

// Missing resource → 404
throw new ResourceNotFoundException("Case", caseId);

// Conflict → 409
throw new ConflictException("User", userId);

// Unauthorized → 401
throw new UnauthorizedException("Invalid credentials");

// Forbidden → 403
throw new ForbiddenException("You are not allowed to access this resource");

// Programmatic field validation → 422
throw new ValidationException("email", "must be a valid email address");
```

### Error response format

Every handled exception produces the following JSON body (using `ErrorResponse` from `nadeex-spring-common`):

```json
{
  "timestamp": "2026-04-18T10:15:30Z",
  "status": 404,
  "error": "Not Found",
  "errorCode": "RESOURCE_NOT_FOUND",
  "message": "Case not found with id: 42",
  "path": "/api/cases/42"
}
```

> **Note:** The field name is `errorCode` (not `code`).

### Bean validation (`@Valid`) support

`GlobalExceptionHandler` handles `MethodArgumentNotValidException` — fires when `@Valid` is used on `@RequestBody`:

```java
@PostMapping("/cases")
public ResponseEntity<CaseDto> createCase(@Valid @RequestBody CreateCaseRequest request) {
    // if @Valid fails → GlobalExceptionHandler returns HTTP 422 automatically
}
```

Response includes a `validationErrors` array with per-field details:

```json
{
  "status": 422,
  "errorCode": "VALIDATION_ERROR",
  "message": "Validation failed for one or more fields",
  "validationErrors": [
    { "field": "title", "message": "must not be blank", "rejectedValue": "" }
  ]
}
```

### Constraint violation (`@Validated`) support

`GlobalExceptionHandler` also handles `ConstraintViolationException` — fires when `@Validated` is used on
a controller class for `@RequestParam` / `@PathVariable` validation, or in the service layer:

```java
@RestController
@Validated
public class CaseController {

    @GetMapping("/cases/{id}")
    public ResponseEntity<CaseDto> getCase(@PathVariable @NotNull UUID id) {
        // if id is null → GlobalExceptionHandler returns HTTP 422 automatically
    }
}
```

### Overriding the handler

To replace the built-in handler entirely, exclude the auto-configuration in your `application.yml`:

```yaml
spring:
  autoconfigure:
    exclude: com.nadeex.spring.exception.config.ExceptionAutoConfiguration
```

Then declare your own `@RestControllerAdvice`. Alternatively, if you only want to add extra handlers,
extend `GlobalExceptionHandler` in your application.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for contribution guidelines.

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for a history of changes.
