package com.nadeex.spring.exception;

import com.nadeex.spring.common.exception.BaseException;
import com.nadeex.spring.common.exception.ErrorCode;

/**
 * Thrown when an authenticated user attempts an action they are not permitted to perform.
 *
 * <p>Maps to HTTP 403. The user is known (authenticated) but lacks the required
 * role or permission for the requested operation.</p>
 *
 * <pre>{@code
 * throw new ForbiddenException("Only lawyers can be assigned to cases");
 * throw new ForbiddenException("You do not have access to this tenant's data");
 * }</pre>
 */
public class ForbiddenException extends BaseException {

    public ForbiddenException(String message) {
        super(message, ErrorCode.FORBIDDEN);
    }
}

