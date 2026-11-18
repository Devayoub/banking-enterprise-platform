package com.enterprise.banking.core.config;

import com.enterprise.banking.common.error.BusinessException;
import com.enterprise.banking.common.event.Topics;
import com.enterprise.banking.core.ledger.UnknownAccountException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

/**
 * Consumer error handling: transient failures (DB down, optimistic lock conflict) are retried with backoff;
 * poison messages (bad JSON, unknown account, currency mismatch) go straight to the dead-letter topic.
 * Spring Boot wires this {@link DefaultErrorHandler} into the default listener container factory.
 */
@Configuration
public class KafkaConfig {

    @Bean
    DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<?, ?> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                // A negative partition lets Kafka pick the DLT partition, so the DLT may have fewer partitions.
                (failedRecord, ex) -> new TopicPartition(Topics.ACCOUNT_TRANSACTIONS_EVENTS_DLT, -1));

        ExponentialBackOff backOff = new ExponentialBackOff(500L, 2.0);
        backOff.setMaxElapsedTime(10_000L);

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);
        handler.addNotRetryableExceptions(
                JsonProcessingException.class,
                UnknownAccountException.class,
                IllegalArgumentException.class,
                BusinessException.class);
        return handler;
    }
}
