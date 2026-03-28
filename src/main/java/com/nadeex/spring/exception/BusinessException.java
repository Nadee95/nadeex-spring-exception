package com.nadeex.spring.exception;

import com.nadeex.spring.common.exception.BaseException;
import com.nadeex.spring.common.exception.ErrorCode;

/**
 * Thrown when a business rule or domain constraint is violated.
 *
 * <p>Examples: attempting to close an already-closed case,
 * assigning a lawyer to a case they are already assigned to.</p>
 *
 * <pre>{@code
 * throw new BusinessException("Case is already closed and cannot be modified");
 * }</pre>
 */
public class BusinessException extends BaseException {

    public BusinessException(String message) {
        super(message, ErrorCode.BUSINESS_RULE_VIOLATION);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, ErrorCode.BUSINESS_RULE_VIOLATION, cause);
    }
}

