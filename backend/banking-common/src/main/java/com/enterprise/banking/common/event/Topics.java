package com.enterprise.banking.common.event;

/** Kafka topic names. Topics are created explicitly (auto-creation is disabled on the broker). */
public final class Topics {

    /** Canonical booked-transaction events, keyed by IBAN so all events of one account stay ordered. */
    public static final String ACCOUNT_TRANSACTIONS_EVENTS = "account-transactions-events";
    public static final String ACCOUNT_TRANSACTIONS_EVENTS_DLT = ACCOUNT_TRANSACTIONS_EVENTS + ".DLT";

    /** Loan decisions published by the Camunda loan-approval process, keyed by application id. */
    public static final String LOAN_DECISION_EVENTS = "loan-decision-events";

    private Topics() {
    }
}
