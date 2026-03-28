package com.nadeex.spring.exception;

import com.nadeex.spring.common.exception.BaseException;
import com.nadeex.spring.common.exception.ErrorCode;

/**
 * Thrown when a requested resource cannot be found.
 *
 * <p>Produces a human-readable message combining resource type and identifier,
 * e.g. {@code "User not found with id: 42"}.</p>
 *
 * <pre>{@code
 * throw new ResourceNotFoundException("User", userId);
 * throw new ResourceNotFoundException("Case", caseId);
 * }</pre>
 */
public class ResourceNotFoundException extends BaseException {

    private final String resourceName;
    private final Object resourceId;

    public ResourceNotFoundException(String resourceName, Object resourceId) {
        super(resourceName + " not found with id: " + resourceId, ErrorCode.RESOURCE_NOT_FOUND);
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

