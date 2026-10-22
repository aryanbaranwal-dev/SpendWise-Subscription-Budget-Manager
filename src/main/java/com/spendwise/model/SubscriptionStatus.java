package com.spendwise.model;

public enum SubscriptionStatus {
    ACTIVE("Active"),
    PAUSED("Paused"),
    CANCELLED("Cancelled");

    private final String label;

    SubscriptionStatus(String label) {
        this.label = label;
    }

    public String getLabel() { return label; }
    public String getCode() { return name(); }
    public String getCssClass() { return name().toLowerCase(); }

    public static SubscriptionStatus fromCode(String code) {
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
