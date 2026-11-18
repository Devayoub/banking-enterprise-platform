package com.enterprise.banking.core.account;

import com.enterprise.banking.common.event.TransactionEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** A booked transaction. Rows are immutable: corrections are new reversing transactions, never updates. */
@Entity
@Table(name = "account_transactions")
public class AccountTransaction {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, updatable = false)
    private Account account;

    /** Reference from the core banking system; unique, which makes event consumption idempotent. */
    @Column(name = "transaction_ref", nullable = false, unique = true, updatable = false, length = 64)
    private String transactionRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, updatable = false)
    private TransactionEvent.Direction direction;

    @Column(nullable = false, precision = 19, scale = 4, updatable = false)
    private BigDecimal amount;

    @Column(nullable = false, length = 3, updatable = false)
    private String currency;

    @Column(length = 140, updatable = false)
    private String description;

    @Column(name = "booking_date", nullable = false, updatable = false)
    private LocalDate bookingDate;

    @Column(name = "value_date", nullable = false, updatable = false)
    private LocalDate valueDate;

    @Column(name = "source_system", nullable = false, length = 32, updatable = false)
    private String sourceSystem;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AccountTransaction() {
        // for JPA
    }

    public static AccountTransaction booked(Account account, TransactionEvent event) {
        AccountTransaction tx = new AccountTransaction();
        tx.id = UUID.randomUUID();
        tx.account = account;
        tx.transactionRef = event.transactionRef();
        tx.direction = event.direction();
        tx.amount = event.amount();
        tx.currency = event.currency();
        tx.description = event.description();
        tx.bookingDate = event.bookingDate();
        tx.valueDate = event.valueDate();
        tx.sourceSystem = event.sourceSystem();
        tx.createdAt = Instant.now();
        return tx;
    }

    public UUID getId() {
        return id;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public TransactionEvent.Direction getDirection() {
        return direction;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public LocalDate getValueDate() {
        return valueDate;
    }
}
