package com.nadeex.spring.exception.mapper;

import com.nadeex.spring.common.exception.BaseException;
import com.nadeex.spring.common.response.ErrorResponse;
import com.nadeex.spring.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;

import java.time.Instant;
import java.util.List;

/**
 * Converts exceptions into standardised {@link ErrorResponse} instances.
 *
 * <p>Kept as a plain utility class (no Spring bean) so it can be used from
 * both {@code GlobalExceptionHandler} and any future error-handling component
 * (e.g. Kafka error handlers, gRPC interceptors) without pulling in the
 * Spring MVC context.</p>
 */
public final class ExceptionMapper {

    private ExceptionMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Maps a {@link BaseException} to an {@link ErrorResponse}.
     *
     * @param ex      the exception to map
     * @param status  the HTTP status to use
     * @param request the current HTTP request (for the path field)
     * @return a fully populated {@link ErrorResponse}
     */
    public static ErrorResponse from(BaseException ex, HttpStatus status, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(status.value());
        response.setError(status.getReasonPhrase());
        response.setMessage(ex.getMessage());
        response.setPath(request.getRequestURI());
        response.setTimestamp(Instant.now());
        response.setErrorCode(ex.getErrorCode().getCode());
        return response;
    }

    /**
     * Maps a generic (unexpected) {@link Exception} to an {@link ErrorResponse}.
     *
     * @param ex      the exception to map
     * @param request the current HTTP request (for the path field)
     * @return a 500 {@link ErrorResponse} with a safe generic message
     */
    public static ErrorResponse fromGeneric(Exception ex, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        response.setMessage("An unexpected error occurred. Please try again later.");
        response.setPath(request.getRequestURI());
        response.setTimestamp(Instant.now());
        return response;
    }

    /**
     * Maps Spring MVC {@link FieldError} list (from {@code @Valid} failures) into
     * the {@link ErrorResponse} with per-field details.
     *
     * @param fieldErrors list of field-level binding errors
     * @param request     the current HTTP request
     * @return a 422 {@link ErrorResponse} containing all field errors
     */
    public static ErrorResponse fromFieldErrors(List<FieldError> fieldErrors, HttpServletRequest request) {
        List<ErrorResponse.ValidationError> validationErrors = fieldErrors.stream()
                .map(fe -> new ErrorResponse.ValidationError(
                        fe.getField(),
                        fe.getDefaultMessage(),
                        fe.getRejectedValue()))
                .toList();

        ErrorResponse response = new ErrorResponse();
        response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
        response.setError(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase());
        response.setMessage("Validation failed for one or more fields");
        response.setPath(request.getRequestURI());
        response.setTimestamp(Instant.now());
        response.setErrorCode("VALIDATION_ERROR");
        response.setValidationErrors(validationErrors);
        return response;
    }
}

