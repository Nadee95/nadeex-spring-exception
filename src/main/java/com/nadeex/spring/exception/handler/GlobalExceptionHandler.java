package com.nadeex.spring.exception.handler;

import com.nadeex.spring.common.response.ErrorResponse;
import com.nadeex.spring.exception.BusinessException;
import com.nadeex.spring.exception.ConflictException;
import com.nadeex.spring.exception.ForbiddenException;
import com.nadeex.spring.exception.ResourceNotFoundException;
import com.nadeex.spring.exception.UnauthorizedException;
import com.nadeex.spring.exception.ValidationException;
import com.nadeex.spring.exception.mapper.ExceptionMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Global exception handler for all REST controllers.
 *
 * <p>Intercepts every exception thrown inside a {@code @RestController} and
 * converts it into a standardised {@link ErrorResponse} JSON body with the
 * correct HTTP status code. Consuming applications can override this bean by
 * declaring their own {@code GlobalExceptionHandler} — see
 * {@link com.nadeex.spring.exception.config.ExceptionAutoConfiguration}.</p>
 *
 * <p>Handler priority (most-specific wins, consistent with Spring MVC):</p>
 * <ol>
 *   <li>Per-exception handlers (404, 409, 400, 422, 401, 403)</li>
 *   <li>{@link MethodArgumentNotValidException} — {@code @Valid} on {@code @RequestBody}</li>
 *   <li>{@link ConstraintViolationException} — {@code @Validated} on params or service layer</li>
 *   <li>Generic {@link Exception} fallback — HTTP 500</li>
 * </ol>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // -------------------------------------------------------------------------
    // 404 Not Found
    // -------------------------------------------------------------------------

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        log.warn("Resource not found: {} id={}", ex.getResourceName(), ex.getResourceId());
        ErrorResponse body = ExceptionMapper.from(ex, HttpStatus.NOT_FOUND, request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // -------------------------------------------------------------------------
    // 409 Conflict
    // -------------------------------------------------------------------------

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            ConflictException ex, HttpServletRequest request) {

        log.warn("Conflict: {} id={}", ex.getResourceName(), ex.getResourceId());
        ErrorResponse body = ExceptionMapper.from(ex, HttpStatus.CONFLICT, request);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // -------------------------------------------------------------------------
    // 400 Bad Request — business rule violation
    // -------------------------------------------------------------------------

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(
            BusinessException ex, HttpServletRequest request) {

        log.warn("Business rule violated: {}", ex.getMessage());
        ErrorResponse body = ExceptionMapper.from(ex, HttpStatus.BAD_REQUEST, request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // -------------------------------------------------------------------------
    // 422 Unprocessable Entity — programmatic field validation
    // -------------------------------------------------------------------------

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            ValidationException ex, HttpServletRequest request) {

        log.warn("Validation failed on field '{}': {}", ex.getField(), ex.getMessage());
        ErrorResponse body = ExceptionMapper.from(ex, HttpStatus.UNPROCESSABLE_ENTITY, request);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    /**
     * Handles {@code @Valid} / {@code @Validated} annotation-driven validation failures.
     * Collects all field errors from the {@link BindingResult} into a single response.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        log.warn("Bean validation failed: {} field error(s)", ex.getBindingResult().getFieldErrorCount());
        ErrorResponse body = ExceptionMapper.fromFieldErrors(ex.getBindingResult().getFieldErrors(), request);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    // -------------------------------------------------------------------------
    // 401 Unauthorized
    // -------------------------------------------------------------------------

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            UnauthorizedException ex, HttpServletRequest request) {

        log.warn("Unauthorized access attempt: {}", ex.getMessage());
        ErrorResponse body = ExceptionMapper.from(ex, HttpStatus.UNAUTHORIZED, request);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    // -------------------------------------------------------------------------
    // 403 Forbidden
    // -------------------------------------------------------------------------

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(
            ForbiddenException ex, HttpServletRequest request) {

        log.warn("Forbidden access: {}", ex.getMessage());
        ErrorResponse body = ExceptionMapper.from(ex, HttpStatus.FORBIDDEN, request);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    /**
     * Handles Spring Security method-security denials ({@code @PreAuthorize} / {@code @Secured}).
     * {@code AuthorizationDeniedException} (Spring Security 6) extends {@link AccessDeniedException},
     * so this single handler covers both.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {

        log.warn("Access denied at {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse body = new ErrorResponse();
        body.setStatus(HttpStatus.FORBIDDEN.value());
        body.setError(HttpStatus.FORBIDDEN.getReasonPhrase());
        body.setMessage("Access Denied");
        body.setPath(request.getRequestURI());
        body.setTimestamp(java.time.Instant.now());
        body.setErrorCode("FORBIDDEN");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    // -------------------------------------------------------------------------
    // 422 Unprocessable Entity — @Validated constraint violations
    // (covers @RequestParam, @PathVariable, and service-layer @Validated)
    // -------------------------------------------------------------------------

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        log.warn("Constraint violation: {} violation(s)", ex.getConstraintViolations().size());
        ErrorResponse body = ExceptionMapper.fromConstraintViolations(ex, request);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    // -------------------------------------------------------------------------
    // 400 Bad Request — path/query variable type mismatch (e.g. invalid UUID)
    // -------------------------------------------------------------------------

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String msg = String.format("Invalid value '%s' for parameter '%s'", ex.getValue(), ex.getName());
        log.warn("Type mismatch: {}", msg);
        ErrorResponse body = new ErrorResponse();
        body.setStatus(HttpStatus.BAD_REQUEST.value());
        body.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        body.setMessage(msg);
        body.setPath(request.getRequestURI());
        body.setTimestamp(java.time.Instant.now());
        body.setErrorCode("BAD_REQUEST");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // -------------------------------------------------------------------------
    // 500 Internal Server Error — catch-all fallback
    // -------------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        ErrorResponse body = ExceptionMapper.fromGeneric(ex, request);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}

