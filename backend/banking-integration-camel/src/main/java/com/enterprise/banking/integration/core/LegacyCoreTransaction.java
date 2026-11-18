package com.enterprise.banking.integration.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * Message format emitted by the legacy core banking system, in XML or JSON with the same field names:
 *
 * <pre>{@code
 * <CoreTransaction>
 *   <TxnRef>CORE-20261117-000123</TxnRef>
 *   <Iban>FR7630006000011234567890189</Iban>
 *   <DrCr>C</DrCr>
 *   <Amount>125.50</Amount>
 *   <Ccy>EUR</Ccy>
 *   <BookingDate>2026-11-17</BookingDate>
 *   <ValueDate>2026-11-17</ValueDate>
 *   <Narrative>Card refund</Narrative>
 * </CoreTransaction>
 * }</pre>
 *
 * Fields stay as raw strings here; validation and typing happen in {@link TransactionEventMapper}.
 */
@JacksonXmlRootElement(localName = "CoreTransaction")
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegacyCoreTransaction {

    @JsonProperty("TxnRef")
    private String txnRef;

    @JsonProperty("Iban")
    private String iban;

    /** {@code C} = credit, {@code D} = debit. */
    @JsonProperty("DrCr")
    private String drCr;

    @JsonProperty("Amount")
    private String amount;

    @JsonProperty("Ccy")
    private String currency;

    @JsonProperty("BookingDate")
    private String bookingDate;

    @JsonProperty("ValueDate")
    private String valueDate;

    @JsonProperty("Narrative")
    private String narrative;

    public String getTxnRef() {
        return txnRef;
    }

    public void setTxnRef(String txnRef) {
        this.txnRef = txnRef;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getDrCr() {
        return drCr;
    }

    public void setDrCr(String drCr) {
        this.drCr = drCr;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getValueDate() {
        return valueDate;
    }

    public void setValueDate(String valueDate) {
        this.valueDate = valueDate;
    }

    public String getNarrative() {
        return narrative;
    }

    public void setNarrative(String narrative) {
        this.narrative = narrative;
    }
}
