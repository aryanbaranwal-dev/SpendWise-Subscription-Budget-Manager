package com.spendwise.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/** How often a subscription is charged, and how to express it per month. */
public enum BillingCycle {
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly");

    private static final BigDecimal WEEKS_PER_YEAR = BigDecimal.valueOf(52);
    private static final BigDecimal MONTHS_PER_YEAR = BigDecimal.valueOf(12);

    private final String label;

    BillingCycle(String label) {
        this.label = label;
    }

    public String getLabel() { return label; }
    public String getCode() { return name(); }

    /**
     * Monthly-equivalent cost, rounded to 2 decimals (HALF_UP).
     *   weekly  : amount x 52 / 12
     *   monthly : amount
     *   yearly  : amount / 12
     */
    public BigDecimal toMonthly(BigDecimal amount) {
        switch (this) {
            case WEEKLY:
                return amount.multiply(WEEKS_PER_YEAR).divide(MONTHS_PER_YEAR, 2, RoundingMode.HALF_UP);
            case YEARLY:
                return amount.divide(MONTHS_PER_YEAR, 2, RoundingMode.HALF_UP);
            default:
                return amount.setScale(2, RoundingMode.HALF_UP);
        }
    }

    /** The payment date that follows the payment on 'date'. */
    public LocalDate nextAfter(LocalDate date) {
        switch (this) {
            case WEEKLY:
                return date.plusWeeks(1);
            case YEARLY:
                return date.plusYears(1);
            default:
                return date.plusMonths(1);
        }
    }

    /** Null for unknown text instead of throwing. */
    public static BillingCycle fromCode(String code) {
        if (code == null) {
            return null;
        }
        try {
            return valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
