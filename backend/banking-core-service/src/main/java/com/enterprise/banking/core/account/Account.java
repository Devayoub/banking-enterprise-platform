package com.enterprise.banking.core.account;

import com.enterprise.banking.common.error.BusinessException;
import com.enterprise.banking.common.error.ErrorCode;
import com.enterprise.banking.common.event.TransactionEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    private UUID id;

    /** Keycloak subject ({@code sub}) of the account holder. */
    @Column(name = "owner_id", nullable = false, updatable = false)
    private String ownerId;

    @Column(nullable = false, unique = true, updatable = false, length = 34)
    private String iban;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @Column(nullable = false, length = 3, updatable = false)
    private String currency;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Optimistic locking: concurrent balance updates fail fast instead of overwriting each other. */
    @Version
    private long version;

    protected Account() {
        // for JPA
    }

    /**
     * Applies a booked transaction to the balance. The core banking system is the system of record,
     * so this projection does not reject overdrafts; it only guards against currency mismatches.
     */
    public void apply(TransactionEvent.Direction direction, BigDecimal amount, String eventCurrency) {
        if (!currency.equals(eventCurrency)) {
            throw new IllegalArgumentException("Currency mismatch for account " + id);
        }
        if (status == AccountStatus.CLOSED) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "Account is closed.");
        }
        balance = direction == TransactionEvent.Direction.CREDIT ? balance.add(amount) : balance.subtract(amount);
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getIban() {
        return iban;
    }

    public String getDisplayName() {
        return displayName;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public enum AccountType { CURRENT, SAVINGS, CARD }

    public enum AccountStatus { ACTIVE, BLOCKED, CLOSED }
}
