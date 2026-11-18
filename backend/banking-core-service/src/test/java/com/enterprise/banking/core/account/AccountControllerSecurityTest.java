package com.enterprise.banking.core.account;

import com.enterprise.banking.common.api.PageResponse;
import com.enterprise.banking.common.error.GlobalExceptionHandler;
import com.enterprise.banking.core.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Covers the access rules of ticket BEP-7: 401 without token, 403 with the wrong role, 200 for CUSTOMER. */
@WebMvcTest(AccountController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AccountControllerSecurityTest {

    private static final String CUSTOMER_ID = "6f1c2a9e-3b7d-4c55-9a1e-0b8f2d4c7e11";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void rejectsAnonymousCalls() throws Exception {
        mvc.perform(get("/api/v1/accounts")).andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsTokensWithoutCustomerRole() throws Exception {
        mvc.perform(get("/api/v1/accounts")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_TELLER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void returnsTheCallersAccounts() throws Exception {
        when(accountService.accountsOf(CUSTOMER_ID)).thenReturn(List.of(new AccountDtos.AccountDto(
                UUID.randomUUID(), "Everyday current account", "FR76*******************0189",
                Account.AccountType.CURRENT, "EUR", new BigDecimal("4250.75"), Account.AccountStatus.ACTIVE,
                Instant.now())));

        mvc.perform(get("/api/v1/accounts")
                        .with(jwt().jwt(j -> j.subject(CUSTOMER_ID))
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].maskedIban").value("FR76*******************0189"));
    }

    @Test
    void transactionPageUsesTheSubjectFromTheToken() throws Exception {
        UUID accountId = UUID.randomUUID();
        when(accountService.transactionsOf(eq(CUSTOMER_ID), eq(accountId), anyInt(), anyInt()))
                .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0));

        mvc.perform(get("/api/v1/accounts/{id}/transactions", accountId)
                        .with(jwt().jwt(j -> j.subject(CUSTOMER_ID))
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }
}
