package com.enterprise.banking.workflow.loan;

/** Names shared between the BPMN model ({@code bpmn/loan-approval.bpmn}) and the Java code. */
public final class LoanProcess {

    public static final String PROCESS_KEY = "loan-approval";

    public static final String VAR_APPLICANT_ID = "applicantId";
    /** Money is stored as a string: Camunda would otherwise Java-serialize BigDecimal, which is disabled. */
    public static final String VAR_AMOUNT = "amount";
    public static final String VAR_CURRENCY = "currency";
    public static final String VAR_TERM_MONTHS = "termMonths";
    public static final String VAR_MONTHLY_INCOME = "monthlyIncome";
    public static final String VAR_CORRELATION_ID = "correlationId";
    public static final String VAR_CREDIT_SCORE = "creditScore";
    public static final String VAR_APPROVED = "approved";
    public static final String VAR_RESERVATION_ID = "reservationId";
    /** Test hook: set to true to exercise the compensation path in Cockpit. */
    public static final String VAR_SIMULATE_DISBURSEMENT_FAILURE = "simulateDisbursementFailure";

    public static final String ERROR_DISBURSEMENT_FAILED = "DISBURSEMENT_FAILED";

    private LoanProcess() {
    }
}
