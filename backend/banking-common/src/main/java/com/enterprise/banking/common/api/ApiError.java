package com.enterprise.banking.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Error payload. {@code code} is a stable, machine-readable identifier (see {@link com.enterprise.banking.common.error.ErrorCode});
 * {@code message} is safe to show to end users and never contains PII, stack traces or SQL.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(String code, String message, List<FieldViolation> details) {

    public ApiError(String code, String message) {
        this(code, message, List.of());
    }

    public record FieldViolation(String field, String message) {
    }
}
