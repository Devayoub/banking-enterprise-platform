package com.enterprise.banking.workflow.config;

import com.enterprise.banking.common.security.KeycloakRealmRoleConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Two security chains:
 * <ol>
 *   <li>{@code /api/**}: platform API, Keycloak JWT required (same rules as every other service).</li>
 *   <li>Everything else: the Camunda webapps (Cockpit, Tasklist, Admin) under {@code /camunda/**}, which use
 *       Camunda's own login and CSRF filter. Replace with Keycloak SSO via the Camunda Keycloak identity
 *       provider plugin before any shared environment.</li>
 * </ol>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WorkflowSecurityConfig {

    @Bean
    @Order(1)
    SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(KeycloakRealmRoleConverter.jwtAuthenticationConverter())));
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain camundaWebappSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // Camunda webapps ship their own CSRF prevention filter and authentication.
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers("/camunda/**", "/").permitAll()
                        .anyRequest().denyAll());
        return http.build();
    }
}
