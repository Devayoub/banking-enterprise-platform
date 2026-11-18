package com.enterprise.banking.common.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Maps Keycloak realm roles ({@code realm_access.roles}) to Spring Security authorities ({@code ROLE_<NAME>}).
 * Only the platform's known roles are mapped, so Keycloak internals such as {@code offline_access} or
 * {@code default-roles-*} never become authorities.
 */
public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final Set<String> KNOWN_ROLES = Set.of(
            SecurityRoles.CUSTOMER, SecurityRoles.TELLER, SecurityRoles.UNDERWRITER, SecurityRoles.ADMIN);

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Object realmAccess = jwt.getClaims().get("realm_access");
        if (!(realmAccess instanceof Map<?, ?> realmAccessMap)) {
            return List.of();
        }
        Object roles = realmAccessMap.get("roles");
        if (!(roles instanceof Collection<?> roleCollection)) {
            return List.of();
        }
        return roleCollection.stream()
                .map(String::valueOf)
                .filter(KNOWN_ROLES::contains)
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }

    /** Servlet/reactive-neutral converter: wrap with {@code ReactiveJwtAuthenticationConverterAdapter} in WebFlux. */
    public static JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return converter;
    }
}
