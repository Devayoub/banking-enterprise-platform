package com.enterprise.banking.core.account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    List<Account> findByOwnerIdOrderByAccountTypeAscDisplayNameAsc(String ownerId);

    Optional<Account> findByIdAndOwnerId(UUID id, String ownerId);

    Optional<Account> findByIban(String iban);
}
