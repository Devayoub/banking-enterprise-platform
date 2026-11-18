package com.enterprise.banking.workflow.loan.delegate;

import com.enterprise.banking.workflow.loan.LoanProcess;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Compensation handler for "Reserve funds". Must be idempotent: the engine may run it again after a crash.
 */
@Component
public class ReleaseReservationDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(ReleaseReservationDelegate.class);

    @Override
    public void execute(DelegateExecution execution) {
        String reservationId = (String) execution.getVariable(LoanProcess.VAR_RESERVATION_ID);
        // TODO(BEP-10): send a release command to the core banking system (no-op if already released).
        log.warn("Compensating: reservation released applicationId={} reservationId={}",
                execution.getProcessBusinessKey(), reservationId);
    }
}
