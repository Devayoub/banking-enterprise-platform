package com.enterprise.banking.common.audit;

import java.time.Instant;
import java.util.Map;

/**
 * Immutable audit record. Written to the dedicated {@code AUDIT} logger, which should be shipped to
 * append-only (WORM) storage in non-local environments for PCI-DSS requirement 10.
 *
 * @param actor      subject (Keycloak {@code sub}) or technical client id that performed the action
 * @param action     stable action name, e.g. {@code ACCOUNT_LIST_VIEWED}
 * @param resource   resource type and id, e.g. {@code account:3f2a...}
 * @param outcome    SUCCESS / DENIED / FAILURE
 * @param attributes extra context; values must already be masked (see {@link PiiMasker})
 */
public record AuditEvent(
        Instant timestamp,
        String actor,
        String action,
        String resource,
        Outcome outcome,
        String correlationId,
        Map<String, String> attributes) {

    public enum Outcome { SUCCESS, DENIED, FAILURE }
}
