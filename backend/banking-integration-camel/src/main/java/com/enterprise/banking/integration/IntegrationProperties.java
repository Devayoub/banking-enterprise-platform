package com.enterprise.banking.integration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param inboundQueue     queue on which the legacy core publishes booked transactions
 * @param deadLetterQueue  queue that receives messages that cannot be parsed or published
 * @param transactionTopic Kafka topic for canonical {@code TransactionEvent}s
 * @param sourceSystem     label written into every event for lineage
 */
@ConfigurationProperties(prefix = "banking.integration")
public record IntegrationProperties(
        String inboundQueue,
        String deadLetterQueue,
        String transactionTopic,
        String sourceSystem) {
}
