package com.enterprise.banking.common.audit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PiiMaskerTest {

    @Test
    void keepsCountryCodeCheckDigitsAndLastFourOfIban() {
        assertThat(PiiMasker.maskIban("FR76 3000 6000 0112 3456 7890 189"))
                .isEqualTo("FR76*******************0189");
    }

    @Test
    void masksLocalPartOfEmail() {
        assertThat(PiiMasker.maskEmail("sara.demo@bank.local")).isEqualTo("s***@bank.local");
    }

    @Test
    void handlesNullsAndShortValues() {
        assertThat(PiiMasker.maskIban(null)).isNull();
        assertThat(PiiMasker.maskIban("FR76")).isEqualTo("****");
        assertThat(PiiMasker.maskEmail("not-an-email")).isEqualTo("***");
    }
}
