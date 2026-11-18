package com.enterprise.banking.workflow.loan;

import java.math.BigDecimal;
import java.time.Instant;

/** Published on {@code loan-decision-events}, keyed by application id. */
public record LoanDecisionEvent(
        int schemaVersion,
        String applicationId,
        String applicantId,
        Decision decision,
        BigDecimal amount,
        String currency,
        Integer creditScore,
        String correlationId,
        Instant decidedAt) {

    public enum Decision { APPROVED, REJECTED, FAILED }
}
