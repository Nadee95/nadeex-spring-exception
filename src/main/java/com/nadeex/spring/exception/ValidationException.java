package com.nadeex.spring.exception;

import com.nadeex.spring.common.exception.BaseException;
import com.nadeex.spring.common.exception.ErrorCode;

/**
 * Thrown when a single input field fails validation outside the standard
 * Bean Validation ({@code @Valid}) flow — useful for programmatic/service-layer checks.
 *
 * <p>The {@code field} property is surfaced in the {@code ErrorResponse} so clients
 * know exactly which field caused the problem.</p>
 *
 * <pre>{@code
 * throw new ValidationException("email", "Email address is already registered");
 * throw new ValidationException("startDate", "Start date must be before end date");
 * }</pre>
 */
public class ValidationException extends BaseException {

    private final String field;

    public ValidationException(String field, String message) {
        super(message, ErrorCode.VALIDATION_ERROR);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}

