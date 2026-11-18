package com.enterprise.banking.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Standard envelope for every REST response exposed by the platform.
 *
 * <pre>
 * { "success": true,  "data": {...}, "error": null, "timestamp": "..." }
 * { "success": false, "data": null,  "error": {"code": "...", "message": "..."}, "timestamp": "..." }
 * </pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, T data, ApiError error, String correlationId, Instant timestamp) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null, Instant.now());
    }

    public static <T> ApiResponse<T> failure(ApiError error, String correlationId) {
        return new ApiResponse<>(false, null, error, correlationId, Instant.now());
    }
}
