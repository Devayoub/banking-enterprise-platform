package com.enterprise.banking.core.ledger;

/** The event references an IBAN this service does not hold. Not retryable: the record goes to the DLT. */
public class UnknownAccountException extends RuntimeException {

    public UnknownAccountException(String message) {
        super(message);
    }
}
