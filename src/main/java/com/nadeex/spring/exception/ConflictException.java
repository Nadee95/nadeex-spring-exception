package com.nadeex.spring.exception;

import com.nadeex.spring.common.exception.BaseException;
import com.nadeex.spring.common.exception.ErrorCode;

/**
 * Thrown when a resource already exists and a duplicate would be created.
 *
 * <p>Maps to HTTP 409. Produces a message combining resource type and identifier,
 * e.g. {@code "User already exists with id: john.doe@example.com"}.</p>
 *
 * <pre>{@code
 * throw new ConflictException("User", "john.doe@example.com");
 * throw new ConflictException("Case reference", "CASE-2026-001");
 * }</pre>
 */
public class ConflictException extends BaseException {

    private final String resourceName;
    private final Object resourceId;

    public ConflictException(String resourceName, Object resourceId) {
        super(resourceName + " already exists with id: " + resourceId, ErrorCode.RESOURCE_ALREADY_EXISTS);
        this.resourceName = resourceName;
        this.resourceId = resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public Object getResourceId() {
        return resourceId;
    }
}

