package com.nadeex.spring.exception;

import com.nadeex.spring.common.exception.BaseException;
import com.nadeex.spring.common.exception.ErrorCode;

/**
 * Thrown when a domain event cannot be serialized to JSON for outbox storage.
 * This is always an internal programming error, never a client error.
 */
public class EventSerializationException extends BaseException {

    public EventSerializationException(String message, Throwable cause) {
        super(message, ErrorCode.INTERNAL_SERVER_ERROR, cause);
    }
}
