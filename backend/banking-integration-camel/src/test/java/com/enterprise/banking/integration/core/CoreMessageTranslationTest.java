package com.enterprise.banking.integration.core;

import com.enterprise.banking.common.event.TransactionEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CoreMessageTranslationTest {

    private static final String XML = """
            <CoreTransaction>
              <TxnRef>CORE-20261117-000123</TxnRef>
              <Iban>FR76 3000 6000 0112 3456 7890 189</Iban>
              <DrCr>C</DrCr>
              <Amount>125.50</Amount>
              <Ccy>eur</Ccy>
              <BookingDate>2026-11-17</BookingDate>
              <Narrative>Card refund</Narrative>
              <LegacyBranchCode>0042</LegacyBranchCode>
            </CoreTransaction>
            """;

    private static final String JSON = """
            {"TxnRef":"CORE-20261117-000124","Iban":"DE89370400440532013000","DrCr":"D",
             "Amount":"64.25","Ccy":"EUR","BookingDate":"2026-11-17","ValueDate":"2026-11-18"}
            """;

    private final CoreMessageParser parser = new CoreMessageParser();
    private final TransactionEventMapper mapper = new TransactionEventMapper(
            Clock.fixed(Instant.parse("2026-11-17T10:00:00Z"), ZoneOffset.UTC));

    @Test
    void translatesXmlCreditIntoCanonicalEvent() {
        TransactionEvent event = mapper.toEvent(parser.parse(XML), "LEGACY_CORE");

        assertThat(event.transactionRef()).isEqualTo("CORE-20261117-000123");
        assertThat(event.iban()).isEqualTo("FR7630006000011234567890189");
        assertThat(event.direction()).isEqualTo(TransactionEvent.Direction.CREDIT);
        assertThat(event.amount()).isEqualByComparingTo(new BigDecimal("125.50"));
        assertThat(event.currency()).isEqualTo("EUR");
        assertThat(event.valueDate()).isEqualTo(LocalDate.of(2026, 11, 17));
        assertThat(event.occurredAt()).isEqualTo(Instant.parse("2026-11-17T10:00:00Z"));
    }

    @Test
    void translatesJsonDebit() {
        TransactionEvent event = mapper.toEvent(parser.parse(JSON), "LEGACY_CORE");

        assertThat(event.direction()).isEqualTo(TransactionEvent.Direction.DEBIT);
        assertThat(event.valueDate()).isEqualTo(LocalDate.of(2026, 11, 18));
    }

    @Test
    void eventIdIsDeterministicSoRedeliveriesCanBeDeduplicated() {
        TransactionEvent first = mapper.toEvent(parser.parse(XML), "LEGACY_CORE");
        TransactionEvent redelivered = mapper.toEvent(parser.parse(XML), "LEGACY_CORE");

        assertThat(redelivered.eventId()).isEqualTo(first.eventId());
    }

    @Test
    void rejectsNegativeAmounts() {
        String negative = JSON.replace("\"64.25\"", "\"-64.25\"");

        assertThatThrownBy(() -> mapper.toEvent(parser.parse(negative), "LEGACY_CORE"))
                .isInstanceOf(InvalidCoreMessageException.class)
                .hasMessageContaining("positive");
    }

    @Test
    void rejectsUnparseablePayloads() {
        assertThatThrownBy(() -> parser.parse("<CoreTransaction><TxnRef>broken"))
                .isInstanceOf(InvalidCoreMessageException.class);
    }
}
