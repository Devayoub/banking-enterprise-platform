package com.enterprise.banking.integration.core;

/** The legacy message cannot be parsed or fails validation. Never retried: it goes straight to the DLQ. */
public class InvalidCoreMessageException extends RuntimeException {

    public InvalidCoreMessageException(String message) {
        super(message);
    }

    public InvalidCoreMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
