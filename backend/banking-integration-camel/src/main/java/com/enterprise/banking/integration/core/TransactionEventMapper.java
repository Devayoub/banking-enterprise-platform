package com.enterprise.banking.integration.core;

import com.enterprise.banking.common.event.TransactionEvent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Currency;
import java.util.UUID;
import java.util.regex.Pattern;

/** Message Translator (EIP): legacy core format to the canonical {@link TransactionEvent}. */
@Component
public class TransactionEventMapper {

    private static final Pattern IBAN = Pattern.compile("^[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}$");

    private final Clock clock;

    public TransactionEventMapper() {
        this(Clock.systemUTC());
    }

    TransactionEventMapper(Clock clock) {
        this.clock = clock;
    }

    public TransactionEvent toEvent(LegacyCoreTransaction source, String sourceSystem) {
        String ref = required(source.getTxnRef(), "TxnRef");
        String iban = required(source.getIban(), "Iban").replace(" ", "").toUpperCase();
        if (!IBAN.matcher(iban).matches()) {
            throw new InvalidCoreMessageException("Invalid IBAN format for TxnRef " + ref);
        }

        TransactionEvent.Direction direction = switch (required(source.getDrCr(), "DrCr").toUpperCase()) {
            case "C" -> TransactionEvent.Direction.CREDIT;
            case "D" -> TransactionEvent.Direction.DEBIT;
            default -> throw new InvalidCoreMessageException("Unknown DrCr code for TxnRef " + ref);
        };

        BigDecimal amount = parseAmount(required(source.getAmount(), "Amount"), ref);
        String currency = parseCurrency(required(source.getCurrency(), "Ccy"), ref);
        LocalDate bookingDate = parseDate(required(source.getBookingDate(), "BookingDate"), "BookingDate", ref);
        LocalDate valueDate = source.getValueDate() == null || source.getValueDate().isBlank()
                ? bookingDate
                : parseDate(source.getValueDate(), "ValueDate", ref);

        return new TransactionEvent(
                TransactionEvent.CURRENT_SCHEMA_VERSION,
                eventIdFor(sourceSystem, ref),
                ref,
                iban,
                direction,
                amount,
                currency,
                bookingDate,
                valueDate,
                truncate(source.getNarrative(), 140),
                sourceSystem,
                Instant.now(clock));
    }

    /** Deterministic: redelivering the same core message always yields the same event id. */
    static String eventIdFor(String sourceSystem, String transactionRef) {
        return UUID.nameUUIDFromBytes((sourceSystem + ":" + transactionRef).getBytes(StandardCharsets.UTF_8)).toString();
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new InvalidCoreMessageException("Missing field " + field);
        }
        return value.strip();
    }

    private static BigDecimal parseAmount(String raw, String ref) {
        try {
            BigDecimal amount = new BigDecimal(raw);
            if (amount.signum() <= 0) {
                throw new InvalidCoreMessageException("Amount must be positive for TxnRef " + ref);
            }
            if (amount.scale() > 4) {
                throw new InvalidCoreMessageException("Amount has more than 4 decimals for TxnRef " + ref);
            }
            return amount;
        } catch (NumberFormatException e) {
            throw new InvalidCoreMessageException("Invalid amount for TxnRef " + ref, e);
        }
    }

    private static String parseCurrency(String raw, String ref) {
        try {
            return Currency.getInstance(raw.toUpperCase()).getCurrencyCode();
        } catch (IllegalArgumentException e) {
            throw new InvalidCoreMessageException("Invalid ISO 4217 currency for TxnRef " + ref, e);
        }
    }

    private static LocalDate parseDate(String raw, String field, String ref) {
        try {
            return LocalDate.parse(raw.strip());
        } catch (DateTimeParseException e) {
            throw new InvalidCoreMessageException("Invalid " + field + " for TxnRef " + ref, e);
        }
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String stripped = value.strip();
        return stripped.length() <= maxLength ? stripped : stripped.substring(0, maxLength);
    }
}
