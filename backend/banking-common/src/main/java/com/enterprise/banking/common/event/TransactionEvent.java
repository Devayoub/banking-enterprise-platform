package com.enterprise.banking.common.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Canonical "transaction booked" event published on {@link Topics#ACCOUNT_TRANSACTIONS_EVENTS}.
 * This is the contract between the integration layer and every consumer: version it, don't break it.
 *
 * @param eventId        deterministic id derived from the source reference, used for idempotent consumption
 * @param transactionRef reference assigned by the core banking system
 * @param iban           account IBAN (also the Kafka record key)
 * @param direction      CREDIT or DEBIT
 * @param amount         always positive; the sign is carried by {@code direction}
 * @param currency       ISO 4217 code
 */
public record TransactionEvent(
        int schemaVersion,
        String eventId,
        String transactionRef,
        String iban,
        Direction direction,
        BigDecimal amount,
        String currency,
        LocalDate bookingDate,
        LocalDate valueDate,
        String description,
        String sourceSystem,
        Instant occurredAt) {

    public static final int CURRENT_SCHEMA_VERSION = 1;

    public enum Direction { CREDIT, DEBIT }
}
