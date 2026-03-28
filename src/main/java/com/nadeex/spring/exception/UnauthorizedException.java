package com.nadeex.spring.exception;

import com.nadeex.spring.common.exception.BaseException;
import com.nadeex.spring.common.exception.ErrorCode;

/**
 * Thrown when a request lacks valid authentication credentials.
 *
 * <p>Maps to HTTP 401. Use this when a JWT is missing, malformed,
 * expired, or the user cannot be identified.</p>
 *
 * <pre>{@code
 * throw new UnauthorizedException("JWT token has expired");
 * throw new UnauthorizedException("Authentication required");
 * }</pre>
 */
public class UnauthorizedException extends BaseException {

    public UnauthorizedException(String message) {
        super(message, ErrorCode.UNAUTHORIZED);
    }
}

