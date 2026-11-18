package com.enterprise.banking.core.account;

import com.enterprise.banking.common.api.ApiResponse;
import com.enterprise.banking.common.api.PageResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@PreAuthorize("hasRole('CUSTOMER')")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /** Accounts of the authenticated customer. The owner always comes from the token, never from the request. */
    @GetMapping
    public ApiResponse<List<AccountDtos.AccountDto>> myAccounts(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(accountService.accountsOf(jwt.getSubject()));
    }

    @GetMapping("/{accountId}/transactions")
    public ApiResponse<PageResponse<AccountDtos.TransactionDto>> transactions(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(accountService.transactionsOf(jwt.getSubject(), accountId, page, size));
    }
}
