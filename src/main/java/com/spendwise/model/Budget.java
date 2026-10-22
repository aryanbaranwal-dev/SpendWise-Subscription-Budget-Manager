package com.spendwise.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Budget {
    private long id;
    private long userId;
    private LocalDate month; // always the first day of the month
    private BigDecimal amount;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public LocalDate getMonth() { return month; }
    public void setMonth(LocalDate month) { this.month = month; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
