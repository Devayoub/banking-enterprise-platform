package com.enterprise.banking.core.account;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, UUID> {

    Page<AccountTransaction> findByAccountIdOrderByBookingDateDescCreatedAtDesc(UUID accountId, Pageable pageable);

    boolean existsByTransactionRef(String transactionRef);
}
