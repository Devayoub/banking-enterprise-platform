package com.enterprise.banking.core.account;

import com.enterprise.banking.common.api.PageResponse;
import com.enterprise.banking.common.audit.AuditLogger;
import com.enterprise.banking.common.error.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AccountService {

    static final int MAX_PAGE_SIZE = 100;

    private final AccountRepository accounts;
    private final AccountTransactionRepository transactions;

    public AccountService(AccountRepository accounts, AccountTransactionRepository transactions) {
        this.accounts = accounts;
        this.transactions = transactions;
    }

    public List<AccountDtos.AccountDto> accountsOf(String ownerId) {
        List<AccountDtos.AccountDto> result = accounts.findByOwnerIdOrderByAccountTypeAscDisplayNameAsc(ownerId).stream()
                .map(AccountDtos.AccountDto::from)
                .toList();
        AuditLogger.success(ownerId, "ACCOUNT_LIST_VIEWED", "account:*");
        return result;
    }

    /**
     * Ownership is enforced in the query itself. An account that exists but belongs to someone else returns
     * 404, not 403, so the API never confirms that another customer's account id exists.
     */
    public PageResponse<AccountDtos.TransactionDto> transactionsOf(String ownerId, UUID accountId, int page, int size) {
        Account account = accounts.findByIdAndOwnerId(accountId, ownerId).orElseThrow(() -> {
            AuditLogger.denied(ownerId, "ACCOUNT_TRANSACTIONS_VIEWED", "account:" + accountId);
            return BusinessException.notFound("Account");
        });

        int safeSize = Math.clamp(size, 1, MAX_PAGE_SIZE);
        Page<AccountTransaction> result = transactions.findByAccountIdOrderByBookingDateDescCreatedAtDesc(
                account.getId(), PageRequest.of(Math.max(page, 0), safeSize));

        AuditLogger.success(ownerId, "ACCOUNT_TRANSACTIONS_VIEWED", "account:" + accountId);
        return new PageResponse<>(
                result.getContent().stream().map(AccountDtos.TransactionDto::from).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }
}
