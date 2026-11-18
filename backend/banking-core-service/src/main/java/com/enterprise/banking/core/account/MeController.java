package com.enterprise.banking.core.account;

import com.enterprise.banking.common.api.ApiResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Debug-friendly endpoint (ticket BEP-7): shows how the platform sees the caller's token. */
@RestController
public class MeController {

    @GetMapping("/api/v1/me")
    public ApiResponse<Me> me(JwtAuthenticationToken authentication) {
        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .toList();
        return ApiResponse.ok(new Me(
                authentication.getToken().getSubject(),
                authentication.getToken().getClaimAsString("preferred_username"),
                authorities));
    }

    public record Me(String subject, String username, List<String> authorities) {
    }
}
