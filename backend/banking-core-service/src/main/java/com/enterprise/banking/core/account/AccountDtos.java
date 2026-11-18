package com.enterprise.banking.core.account;

import com.enterprise.banking.common.audit.PiiMasker;
import com.enterprise.banking.common.event.TransactionEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** API read models. Entities are never serialized directly. */
public final class AccountDtos {

    private AccountDtos() {
    }

    public record AccountDto(
            UUID id,
            String displayName,
            String maskedIban,
            Account.AccountType type,
            String currency,
            BigDecimal balance,
            Account.AccountStatus status,
            Instant lastUpdated) {

        static AccountDto from(Account account) {
            return new AccountDto(account.getId(), account.getDisplayName(), PiiMasker.maskIban(account.getIban()),
                    account.getAccountType(), account.getCurrency(), account.getBalance(), account.getStatus(),
                    account.getUpdatedAt());
        }
    }

    public record TransactionDto(
            UUID id,
            String reference,
            TransactionEvent.Direction direction,
            BigDecimal amount,
            String currency,
            String description,
            LocalDate bookingDate,
            LocalDate valueDate) {

        static TransactionDto from(AccountTransaction tx) {
            return new TransactionDto(tx.getId(), tx.getTransactionRef(), tx.getDirection(), tx.getAmount(),
                    tx.getCurrency(), tx.getDescription(), tx.getBookingDate(), tx.getValueDate());
        }
    }
}
