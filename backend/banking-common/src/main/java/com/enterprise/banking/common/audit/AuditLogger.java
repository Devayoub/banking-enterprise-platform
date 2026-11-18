package com.enterprise.banking.common.audit;

import com.enterprise.banking.common.web.CorrelationId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Writes structured audit events to the {@code AUDIT} logger.
 * Keep it separate from application logs so it can be routed, retained and protected independently.
 */
public final class AuditLogger {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private AuditLogger() {
    }

    public static void success(String actor, String action, String resource) {
        log(actor, action, resource, AuditEvent.Outcome.SUCCESS, Map.of());
    }

    public static void denied(String actor, String action, String resource) {
        log(actor, action, resource, AuditEvent.Outcome.DENIED, Map.of());
    }

    public static void log(String actor, String action, String resource,
                           AuditEvent.Outcome outcome, Map<String, String> attributes) {
        AuditEvent event = new AuditEvent(Instant.now(), actor, action, resource, outcome,
                MDC.get(CorrelationId.MDC_KEY), Map.copyOf(attributes));
        AUDIT.info("audit timestamp={} actor={} action={} resource={} outcome={} correlationId={} attributes={}",
                event.timestamp(), event.actor(), event.action(), event.resource(), event.outcome(),
                event.correlationId(), format(event.attributes()));
    }

    private static String format(Map<String, String> attributes) {
        return attributes.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .sorted()
                .collect(Collectors.joining(",", "{", "}"));
    }
}
