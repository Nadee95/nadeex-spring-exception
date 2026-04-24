# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.2.0] - 2026-04-24

### Added
- `EventSerializationException` — thrown when a domain event cannot be serialized to JSON for
  outbox storage; wraps `JsonProcessingException` and maps to `ErrorCode.INTERNAL_SERVER_ERROR` (HTTP 500)
- `GlobalExceptionHandler` — two new `@ExceptionHandler` methods:
  - `ConstraintViolationException` (Jakarta Validation) → HTTP 422 Unprocessable Entity;
    covers `@RequestParam`, `@PathVariable`, and service-layer `@Validated` violations;
    collects all constraint messages via `ExceptionMapper.fromConstraintViolations()`
  - `MethodArgumentTypeMismatchException` (Spring MVC) → HTTP 400 Bad Request;
    covers invalid path/query variable types (e.g. non-UUID string passed to a `UUID` parameter)
- `ExceptionMapper` — `fromConstraintViolations()` helper that converts `ConstraintViolation`
  set into a flat list of `ErrorResponse.ValidationError` entries

## [0.1.0] - 2026-04-03

### Added
- `BusinessException` — for general business rule violations; maps to HTTP 400
- `ResourceNotFoundException` — for missing resources; maps to HTTP 404
- `ValidationException` — for input validation failures; maps to HTTP 422
- `UnauthorizedException` — for authentication failures; maps to HTTP 401
- `ForbiddenException` — for authorization failures; maps to HTTP 403
- `ConflictException` — for resource conflict scenarios; maps to HTTP 409
- `GlobalExceptionHandler` — `@ControllerAdvice` that intercepts all custom exceptions and produces a standardized `ErrorResponse`
- `ExceptionMapper` — maps exception type to HTTP status and `ErrorCode`
- `ExceptionAutoConfiguration` — Spring Boot auto-configuration; zero-config setup
- Depends on `nadeex-spring-common:0.1.0` for `ErrorResponse`, `ErrorCode`, `BaseException`
- Gradle Kotlin DSL build with `java-library` + `maven-publish` plugins
- Local Maven and GitHub Packages publishing
- JaCoCo test coverage reporting
- GitHub Actions CI and publish workflows
