package com.enterprise.banking.workflow.loan;

import com.enterprise.banking.common.api.ApiResponse;
import com.enterprise.banking.common.audit.AuditLogger;
import com.enterprise.banking.common.web.CorrelationId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/loans")
public class LoanController {

    private final RuntimeService runtimeService;

    public LoanController(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    /**
     * Starts the asynchronous loan-approval process and returns immediately (202). The decision is
     * published later on the {@code loan-decision-events} topic.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<LoanApplicationAccepted> apply(@AuthenticationPrincipal Jwt jwt,
                                                      @RequestHeader(value = CorrelationId.HEADER, required = false) String correlationId,
                                                      @Valid @RequestBody LoanApplicationRequest request) {
        String applicationId = UUID.randomUUID().toString();
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(LoanProcess.PROCESS_KEY, applicationId,
                Variables.createVariables()
                        .putValue(LoanProcess.VAR_APPLICANT_ID, jwt.getSubject())
                        .putValue(LoanProcess.VAR_AMOUNT, request.amount().toPlainString())
                        .putValue(LoanProcess.VAR_CURRENCY, request.currency())
                        .putValue(LoanProcess.VAR_TERM_MONTHS, request.termMonths())
                        .putValue(LoanProcess.VAR_MONTHLY_INCOME, request.monthlyIncome().toPlainString())
                        .putValue(LoanProcess.VAR_CORRELATION_ID, correlationId != null ? correlationId : applicationId)
                        .putValue(LoanProcess.VAR_SIMULATE_DISBURSEMENT_FAILURE, false));

        AuditLogger.success(jwt.getSubject(), "LOAN_APPLICATION_SUBMITTED", "loan-application:" + applicationId);
        return ApiResponse.ok(new LoanApplicationAccepted(applicationId, instance.getProcessInstanceId(), "SUBMITTED"));
    }

    public record LoanApplicationRequest(
            @NotNull @DecimalMin("500.00") @DecimalMax("500000.00") @Digits(integer = 9, fraction = 2) BigDecimal amount,
            @NotNull @Pattern(regexp = "^[A-Z]{3}$") String currency,
            @NotNull @Min(6) @Max(360) Integer termMonths,
            @NotNull @DecimalMin("0.00") @Digits(integer = 9, fraction = 2) BigDecimal monthlyIncome) {
    }

    public record LoanApplicationAccepted(String applicationId, String processInstanceId, String status) {
    }
}
