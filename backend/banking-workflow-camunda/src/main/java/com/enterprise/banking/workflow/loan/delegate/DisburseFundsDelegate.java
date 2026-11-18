package com.enterprise.banking.workflow.loan.delegate;

import com.enterprise.banking.workflow.loan.LoanProcess;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Step 2 of the saga: disburse the loan. A business failure raises the BPMN error
 * {@value LoanProcess#ERROR_DISBURSEMENT_FAILED}, which the model catches to trigger compensation.
 * Technical failures (exceptions) are retried by the job executor and end up as incidents in Cockpit.
 */
@Component
public class DisburseFundsDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(DisburseFundsDelegate.class);

    @Override
    public void execute(DelegateExecution execution) {
        if (Boolean.TRUE.equals(execution.getVariable(LoanProcess.VAR_SIMULATE_DISBURSEMENT_FAILURE))) {
            throw new BpmnError(LoanProcess.ERROR_DISBURSEMENT_FAILED, "Core banking rejected the disbursement");
        }
        // TODO(BEP-10): send the disbursement command to the core banking system.
        log.info("Funds disbursed applicationId={} reservationId={}", execution.getProcessBusinessKey(),
                execution.getVariable(LoanProcess.VAR_RESERVATION_ID));
    }
}
