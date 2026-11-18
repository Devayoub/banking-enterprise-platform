package com.enterprise.banking.common.audit;

/** Masks personal and financial identifiers before they reach logs, audit trails or error messages. */
public final class PiiMasker {

    private PiiMasker() {
    }

    /** {@code FR7630006000011234567890189} becomes {@code FR76*******************0189}. */
    public static String maskIban(String iban) {
        if (iban == null) {
            return null;
        }
        String compact = iban.replace(" ", "");
        if (compact.length() <= 8) {
            return "*".repeat(compact.length());
        }
        return compact.substring(0, 4)
                + "*".repeat(compact.length() - 8)
                + compact.substring(compact.length() - 4);
    }

    /** {@code sara.demo@bank.local} becomes {@code s***@bank.local}. */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email == null ? null : "***";
        }
        int at = email.indexOf('@');
        return email.charAt(0) + "***" + email.substring(at);
    }
}
