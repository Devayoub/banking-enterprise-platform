package com.enterprise.banking.integration.route;

import com.enterprise.banking.common.event.TransactionEvent;
import com.enterprise.banking.common.web.CorrelationId;
import com.enterprise.banking.integration.IntegrationProperties;
import com.enterprise.banking.integration.core.CoreMessageParser;
import com.enterprise.banking.integration.core.InvalidCoreMessageException;
import com.enterprise.banking.integration.core.LegacyCoreTransaction;
import com.enterprise.banking.integration.core.TransactionEventMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.spi.IdempotentRepository;
import org.apache.camel.support.processor.idempotent.MemoryIdempotentRepository;
import org.springframework.stereotype.Component;

/**
 * Legacy core MQ to Kafka bridge (ticket BEP-9).
 *
 * <pre>
 *  JMS queue (transacted)            Message Translator                Idempotent Consumer        Kafka
 *  CORE.BANKING.TRANSACTIONS.OUT --> XML/JSON -> TransactionEvent --> skip duplicate eventId --> account-transactions-events
 *            |                                                                                    (key = IBAN)
 *            +-- invalid message --------------------------------------> CORE.BANKING.TRANSACTIONS.DLQ (no retry)
 *            +-- Kafka unavailable: 5 retries, exponential backoff ----> CORE.BANKING.TRANSACTIONS.DLQ
 * </pre>
 *
 * The JMS consumer is transacted: if the JVM dies before Kafka acknowledges, the broker redelivers the message.
 * Duplicates are harmless because the event id is deterministic and consumers are idempotent.
 */
@Component
public class JmsToKafkaRoute extends RouteBuilder {

    static final String ROUTE_ID = "core-mq-to-kafka";
    static final String HEADER_EVENT_ID = "bankingEventId";
    static final String HEADER_TRANSACTION_REF = "bankingTransactionRef";
    static final String HEADER_ERROR_REASON = "bankingErrorReason";

    private final IntegrationProperties properties;
    private final CoreMessageParser parser;
    private final TransactionEventMapper mapper;
    private final ObjectMapper objectMapper;

    /**
     * In-memory store for local development. In production use a shared store
     * (e.g. camel-sql {@code JdbcMessageIdRepository}) so all instances see the same keys.
     */
    private final IdempotentRepository idempotentRepository = MemoryIdempotentRepository.memoryIdempotentRepository(50_000);

    public JmsToKafkaRoute(IntegrationProperties properties, CoreMessageParser parser,
                           TransactionEventMapper mapper, ObjectMapper objectMapper) {
        this.properties = properties;
        this.parser = parser;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public void configure() {
        String deadLetterUri = "jms:queue:" + properties.deadLetterQueue();

        // Transient failures (Kafka down, broker hiccup): retry, then park the ORIGINAL message on the DLQ.
        errorHandler(deadLetterChannel(deadLetterUri)
                .useOriginalMessage()
                .maximumRedeliveries(5)
                .redeliveryDelay(500)
                .useExponentialBackOff()
                .backOffMultiplier(2)
                .maximumRedeliveryDelay(10_000)
                .retryAttemptedLogLevel(LoggingLevel.WARN)
                .logExhausted(true));

        // Poison messages: retrying cannot help, so send them to the DLQ immediately with the reason attached.
        onException(InvalidCoreMessageException.class)
                .maximumRedeliveries(0)
                .handled(true)
                .useOriginalMessage()
                .setHeader(HEADER_ERROR_REASON, simple("${exception.message}"))
                .log(LoggingLevel.WARN, "Rejected core banking message: ${exception.message}")
                .to(deadLetterUri);

        from("jms:queue:" + properties.inboundQueue() + "?transacted=true")
                .routeId(ROUTE_ID)
                .process(this::translate)
                .removeHeaders("JMS*")
                .idempotentConsumer(header(HEADER_EVENT_ID), idempotentRepository)
                    .skipDuplicate(true)
                    .to("kafka:" + properties.transactionTopic())
                    .log("Published transactionRef=${header." + HEADER_TRANSACTION_REF + "} to "
                            + properties.transactionTopic())
                .end();
    }

    private void translate(Exchange exchange) throws Exception {
        String raw = exchange.getIn().getBody(String.class);
        LegacyCoreTransaction legacy = parser.parse(raw);
        TransactionEvent event = mapper.toEvent(legacy, properties.sourceSystem());

        String jmsCorrelationId = exchange.getIn().getHeader("JMSCorrelationID", String.class);
        exchange.getIn().setHeader(CorrelationId.HEADER,
                jmsCorrelationId != null && !jmsCorrelationId.isBlank() ? jmsCorrelationId : event.eventId());
        exchange.getIn().setHeader(KafkaConstants.KEY, event.iban());
        exchange.getIn().setHeader(HEADER_EVENT_ID, event.eventId());
        exchange.getIn().setHeader(HEADER_TRANSACTION_REF, event.transactionRef());
        exchange.getIn().setBody(objectMapper.writeValueAsString(event));
    }
}
