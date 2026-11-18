package com.enterprise.banking.common.security;

/**
 * Keycloak realm roles of {@code banking-realm}. Spring authorities are these names with the {@code ROLE_} prefix,
 * so use {@code hasRole(SecurityRoles.CUSTOMER)} or {@code hasAuthority("ROLE_CUSTOMER")}.
 */
public final class SecurityRoles {

    public static final String CUSTOMER = "CUSTOMER";
    public static final String TELLER = "TELLER";
    public static final String UNDERWRITER = "UNDERWRITER";
    public static final String ADMIN = "ADMIN";

    private SecurityRoles() {
    }
}
