package com.enterprise.banking.workflow.loan.delegate;

import com.enterprise.banking.workflow.loan.LoanProcess;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Step 1 of the saga: reserve the loan amount in the core banking system.
 * Undone by {@link ReleaseReservationDelegate} (BPMN compensation) if a later step fails.
 */
@Component
public class ReserveFundsDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(ReserveFundsDelegate.class);

    @Override
    public void execute(DelegateExecution execution) {
        // TODO(BEP-10): send a reservation command to the core via the Camel/MQ adapter and await the reply.
        String reservationId = "RSV-" + UUID.randomUUID();
        execution.setVariable(LoanProcess.VAR_RESERVATION_ID, reservationId);
        log.info("Funds reserved applicationId={} reservationId={}", execution.getProcessBusinessKey(), reservationId);
    }
}
