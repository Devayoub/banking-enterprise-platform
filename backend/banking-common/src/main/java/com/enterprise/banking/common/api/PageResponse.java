package com.enterprise.banking.common.api;

import java.util.List;

/** Framework-neutral page wrapper so API consumers never depend on Spring Data's Page JSON shape. */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
}
