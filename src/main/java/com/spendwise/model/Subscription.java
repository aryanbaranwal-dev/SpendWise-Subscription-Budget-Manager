package com.spendwise.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class Subscription {
    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    private long id;
    private long userId;
    private String name;
    private BigDecimal amount;
    private BillingCycle billingCycle;
    private LocalDate nextPaymentDate;
    private SubscriptionStatus status;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BillingCycle getBillingCycle() { return billingCycle; }
    public void setBillingCycle(BillingCycle billingCycle) { this.billingCycle = billingCycle; }
    public LocalDate getNextPaymentDate() { return nextPaymentDate; }
    public void setNextPaymentDate(LocalDate nextPaymentDate) { this.nextPaymentDate = nextPaymentDate; }
    public SubscriptionStatus getStatus() { return status; }
    public void setStatus(SubscriptionStatus status) { this.status = status; }

    public BigDecimal getMonthlyEquivalent() {
        return billingCycle.toMonthly(amount);
    }

    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE;
    }

    public String getNextPaymentDisplay() {
        return nextPaymentDate == null ? "" : nextPaymentDate.format(DISPLAY);
    }

    /** Negative when the payment date has already passed. */
    public long getDaysUntilDue() {
        return ChronoUnit.DAYS.between(LocalDate.now(), nextPaymentDate);
    }

    public boolean isOverdue() {
        return isActive() && getDaysUntilDue() < 0;
    }

    public String getDueText() {
        long d = getDaysUntilDue();
        if (d < 0) {
            return "Overdue by " + (-d) + (d == -1 ? " day" : " days");
        }
        if (d == 0) {
            return "Due today";
        }
        return d == 1 ? "Due tomorrow" : "Due in " + d + " days";
    }
}
