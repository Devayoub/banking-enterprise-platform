package com.enterprise.banking.core.ledger;

import com.enterprise.banking.common.audit.PiiMasker;
import com.enterprise.banking.common.event.Topics;
import com.enterprise.banking.common.event.TransactionEvent;
import com.enterprise.banking.core.account.Account;
import com.enterprise.banking.core.account.AccountRepository;
import com.enterprise.banking.core.account.AccountTransaction;
import com.enterprise.banking.core.account.AccountTransactionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Projects booked transactions from Kafka into the local ledger tables.
 *
 * <p>Delivery is at-least-once, so consumption must be idempotent: a transaction reference that was already
 * stored is skipped, and the unique constraint on {@code transaction_ref} protects against concurrent duplicates.
 * The offset is committed only after the DB transaction commits.
 */
@Component
public class TransactionEventListener {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventListener.class);

    private final ObjectMapper objectMapper;
    private final AccountRepository accounts;
    private final AccountTransactionRepository transactions;

    public TransactionEventListener(ObjectMapper objectMapper, AccountRepository accounts,
                                    AccountTransactionRepository transactions) {
        this.objectMapper = objectMapper;
        this.accounts = accounts;
        this.transactions = transactions;
    }

    @KafkaListener(topics = Topics.ACCOUNT_TRANSACTIONS_EVENTS, groupId = "core-service-ledger")
    @Transactional
    public void onTransactionBooked(String payload) throws JsonProcessingException {
        TransactionEvent event = objectMapper.readValue(payload, TransactionEvent.class);

        if (transactions.existsByTransactionRef(event.transactionRef())) {
            log.info("Duplicate event ignored transactionRef={}", event.transactionRef());
            return;
        }

        Account account = accounts.findByIban(event.iban())
                .orElseThrow(() -> new UnknownAccountException(
                        "No account for IBAN " + PiiMasker.maskIban(event.iban())));

        account.apply(event.direction(), event.amount(), event.currency());
        transactions.save(AccountTransaction.booked(account, event));
        log.info("Transaction booked transactionRef={} account={} direction={}",
                event.transactionRef(), account.getId(), event.direction());
    }
}
