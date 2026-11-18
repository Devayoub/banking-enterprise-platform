package com.enterprise.banking.common.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakRealmRoleConverterTest {

    private final KeycloakRealmRoleConverter converter = new KeycloakRealmRoleConverter();

    @Test
    void mapsOnlyKnownRealmRolesToSpringAuthorities() {
        Jwt jwt = jwt(Map.of("realm_access", Map.of("roles",
                List.of("CUSTOMER", "TELLER", "offline_access", "default-roles-banking-realm"))));

        List<String> authorities = converter.convert(jwt).stream().map(GrantedAuthority::getAuthority).toList();

        assertThat(authorities).containsExactlyInAnyOrder("ROLE_CUSTOMER", "ROLE_TELLER");
    }

    @Test
    void returnsNoAuthoritiesWhenRealmAccessClaimIsMissing() {
        Jwt jwt = jwt(Map.of("sub", "6f1c2a9e-3b7d-4c55-9a1e-0b8f2d4c7e11"));

        assertThat(converter.convert(jwt)).isEmpty();
    }

    private static Jwt jwt(Map<String, Object> claims) {
        Jwt.Builder builder = Jwt.withTokenValue("token").header("alg", "RS256");
        claims.forEach(builder::claim);
        return builder.build();
    }
}
