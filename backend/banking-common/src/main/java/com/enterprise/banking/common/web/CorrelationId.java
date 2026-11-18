package com.enterprise.banking.common.web;

/** Correlation id propagated across HTTP, Kafka headers, JMS properties and Camunda variables. */
public final class CorrelationId {

    public static final String HEADER = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";

    private CorrelationId() {
    }
}
