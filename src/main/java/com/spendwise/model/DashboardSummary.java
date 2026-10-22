package com.spendwise.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Figures for the dashboard and budget page.
 *   remaining   = budget - recorded expenses     (subscriptions are NOT subtracted)
 *   utilization = expenses / budget x 100
 * monthlySubscriptionCost is an estimate shown separately.
 */
public class DashboardSummary {
    private boolean budgetSet;
    private BigDecimal budget = BigDecimal.ZERO.setScale(2);
    private BigDecimal totalExpenses = BigDecimal.ZERO.setScale(2);
    private BigDecimal remaining = BigDecimal.ZERO.setScale(2);
    private BigDecimal utilizationPercent = BigDecimal.ZERO.setScale(1);
    private int progressWidth;
    private boolean overBudget;
    private boolean nearLimit;
    private BigDecimal monthlySubscriptionCost = BigDecimal.ZERO.setScale(2);
    private int activeSubscriptionCount;
    private List<Expense> recentExpenses = new ArrayList<>();
    private List<Subscription> upcomingSubscriptions = new ArrayList<>();

    public boolean isBudgetSet() { return budgetSet; }
    public void setBudgetSet(boolean budgetSet) { this.budgetSet = budgetSet; }
    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }
    public BigDecimal getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(BigDecimal totalExpenses) { this.totalExpenses = totalExpenses; }
    public BigDecimal getRemaining() { return remaining; }
    public void setRemaining(BigDecimal remaining) { this.remaining = remaining; }
    public BigDecimal getUtilizationPercent() { return utilizationPercent; }
    public void setUtilizationPercent(BigDecimal utilizationPercent) { this.utilizationPercent = utilizationPercent; }
    public int getProgressWidth() { return progressWidth; }
    public void setProgressWidth(int progressWidth) { this.progressWidth = progressWidth; }
    public boolean isOverBudget() { return overBudget; }
    public void setOverBudget(boolean overBudget) { this.overBudget = overBudget; }
    public boolean isNearLimit() { return nearLimit; }
    public void setNearLimit(boolean nearLimit) { this.nearLimit = nearLimit; }
    public BigDecimal getMonthlySubscriptionCost() { return monthlySubscriptionCost; }
    public void setMonthlySubscriptionCost(BigDecimal v) { this.monthlySubscriptionCost = v; }
    public int getActiveSubscriptionCount() { return activeSubscriptionCount; }
    public void setActiveSubscriptionCount(int c) { this.activeSubscriptionCount = c; }
    public List<Expense> getRecentExpenses() { return recentExpenses; }
    public void setRecentExpenses(List<Expense> l) { this.recentExpenses = l; }
    public List<Subscription> getUpcomingSubscriptions() { return upcomingSubscriptions; }
    public void setUpcomingSubscriptions(List<Subscription> l) { this.upcomingSubscriptions = l; }
}
