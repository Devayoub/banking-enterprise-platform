package com.enterprise.banking.workflow.loan.delegate;

import com.enterprise.banking.workflow.loan.LoanProcess;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Placeholder credit scoring based on affordability (35% of income over the loan term vs the amount).
 * Replace with the credit bureau / internal scoring model integration.
 * Scores below 500 are rejected automatically by the next gateway; the rest go to an underwriter.
 */
@Component
public class CreditScoringDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(CreditScoringDelegate.class);
    private static final BigDecimal AFFORDABILITY_SHARE = new BigDecimal("0.35");

    @Override
    public void execute(DelegateExecution execution) {
        BigDecimal amount = new BigDecimal((String) execution.getVariable(LoanProcess.VAR_AMOUNT));
        BigDecimal monthlyIncome = new BigDecimal((String) execution.getVariable(LoanProcess.VAR_MONTHLY_INCOME));
        int termMonths = (Integer) execution.getVariable(LoanProcess.VAR_TERM_MONTHS);

        BigDecimal capacity = monthlyIncome.multiply(AFFORDABILITY_SHARE).multiply(BigDecimal.valueOf(termMonths));
        double ratio = capacity.divide(amount, MathContext.DECIMAL64).doubleValue();
        int score = (int) Math.round(300 + Math.min(550, ratio * 300));

        execution.setVariable(LoanProcess.VAR_CREDIT_SCORE, score);
        log.info("Credit score computed applicationId={} score={}", execution.getProcessBusinessKey(), score);
    }
}
