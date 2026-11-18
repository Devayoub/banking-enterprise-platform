package com.enterprise.banking.common.error;

/**
 * Global, stable error catalogue. Codes are part of the public API contract:
 * never rename an existing code, only add new ones.
 */
public enum ErrorCode {

    VALIDATION_FAILED(400, "The request contains invalid data."),
    UNAUTHENTICATED(401, "Authentication is required."),
    ACCESS_DENIED(403, "You are not allowed to perform this operation."),
    RESOURCE_NOT_FOUND(404, "The requested resource was not found."),
    CONFLICT(409, "The resource was modified concurrently. Please retry."),
    BUSINESS_RULE_VIOLATION(422, "The operation violates a business rule."),
    INSUFFICIENT_FUNDS(422, "Insufficient funds for this operation."),
    INTEGRATION_FAILURE(502, "A downstream banking system is unavailable."),
    INTERNAL_ERROR(500, "An unexpected error occurred.");

    private final int httpStatus;
    private final String defaultMessage;

    ErrorCode(int httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public int httpStatus() {
        return httpStatus;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
