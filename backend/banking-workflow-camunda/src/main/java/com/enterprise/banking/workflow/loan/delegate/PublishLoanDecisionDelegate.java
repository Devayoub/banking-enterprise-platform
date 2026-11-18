package com.enterprise.banking.workflow.loan.delegate;

import com.enterprise.banking.common.event.Topics;
import com.enterprise.banking.common.web.CorrelationId;
import com.enterprise.banking.workflow.loan.LoanDecisionEvent;
import com.enterprise.banking.workflow.loan.LoanProcess;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Publishes the loan decision to Kafka. The decision comes from the task's {@code decision} input parameter
 * in the BPMN model (APPROVED / REJECTED / FAILED).
 *
 * <p>The send is synchronous and the task is {@code asyncBefore}, so a Kafka outage makes the job fail and
 * retry instead of losing the event. Consumers must be idempotent (key = application id): a retry after a
 * successful send can publish the same decision twice. See docs/ARCHITECTURE.md for the outbox alternative.
 */
@Component
public class PublishLoanDecisionDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(PublishLoanDecisionDelegate.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public PublishLoanDecisionDelegate(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String applicationId = execution.getProcessBusinessKey();
        String correlationId = (String) execution.getVariable(LoanProcess.VAR_CORRELATION_ID);
        LoanDecisionEvent event = new LoanDecisionEvent(
                1,
                applicationId,
                (String) execution.getVariable(LoanProcess.VAR_APPLICANT_ID),
                LoanDecisionEvent.Decision.valueOf((String) execution.getVariable("decision")),
                new BigDecimal((String) execution.getVariable(LoanProcess.VAR_AMOUNT)),
                (String) execution.getVariable(LoanProcess.VAR_CURRENCY),
                (Integer) execution.getVariable(LoanProcess.VAR_CREDIT_SCORE),
                correlationId,
                Instant.now());

        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(
                Topics.LOAN_DECISION_EVENTS, applicationId, objectMapper.writeValueAsString(event));
        if (correlationId != null) {
            producerRecord.headers().add(CorrelationId.HEADER, correlationId.getBytes(StandardCharsets.UTF_8));
        }
        kafkaTemplate.send(producerRecord).get(10, TimeUnit.SECONDS);
        log.info("Loan decision published applicationId={} decision={}", applicationId, event.decision());
    }
}
