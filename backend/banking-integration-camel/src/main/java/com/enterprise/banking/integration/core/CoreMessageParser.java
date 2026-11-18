package com.enterprise.banking.integration.core;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Content-based parsing of legacy core messages: XML or JSON, detected from the payload itself. */
@Component
public class CoreMessageParser {

    private final ObjectMapper xmlMapper = XmlMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    private final ObjectMapper jsonMapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    public LegacyCoreTransaction parse(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) {
            throw new InvalidCoreMessageException("Empty message");
        }
        String payload = rawMessage.strip();
        try {
            return payload.startsWith("<")
                    ? xmlMapper.readValue(payload, LegacyCoreTransaction.class)
                    : jsonMapper.readValue(payload, LegacyCoreTransaction.class);
        } catch (IOException e) {
            throw new InvalidCoreMessageException("Unparseable core banking message", e);
        }
    }
}
