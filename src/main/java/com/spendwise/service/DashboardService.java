package com.spendwise.service;

import com.spendwise.dao.BudgetDao;
import com.spendwise.dao.ExpenseDao;
import com.spendwise.dao.SubscriptionDao;
import com.spendwise.model.Budget;
import com.spendwise.model.DashboardSummary;
import com.spendwise.model.Subscription;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public class DashboardService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal WARNING_PERCENT = BigDecimal.valueOf(80);
    private static final int UPCOMING_DAYS = 30;

    private final BudgetDao budgetDao = new BudgetDao();
    private final ExpenseDao expenseDao = new ExpenseDao();
    private final SubscriptionDao subscriptionDao = new SubscriptionDao();
    private final SubscriptionService subscriptionService = new SubscriptionService();

    /** Budget, expenses, remaining, utilization and the separate subscription estimate. */
    public DashboardSummary summarize(long userId, YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.plusMonths(1).atDay(1);

        DashboardSummary s = new DashboardSummary();
        Optional<Budget> budget = budgetDao.findByMonth(userId, from);
        BigDecimal budgetAmount = budget.isPresent() ? budget.get().getAmount() : BigDecimal.ZERO.setScale(2);
        BigDecimal spent = expenseDao.sumBetween(userId, from, to);


s.setBudgetSet(budget.isPresent());
s.setBudget(budgetAmount);
s.setTotalExpenses(spent);

List<Subscription> active = subscriptionDao.findActive(userId);
BigDecimal monthlySubscriptions = subscriptionService.monthlyTotal(active);
s.setMonthlySubscriptionCost(monthlySubscriptions);
s.setActiveSubscriptionCount(active.size());

// Remaining budget includes expenses and active subscription commitments.
BigDecimal totalCommitted = spent.add(monthlySubscriptions);
s.setRemaining(budgetAmount.subtract(totalCommitted));

if (budgetAmount.compareTo(BigDecimal.ZERO) > 0) {
    BigDecimal pct = totalCommitted.multiply(HUNDRED)
            .divide(budgetAmount, 1, RoundingMode.HALF_UP);
    s.setUtilizationPercent(pct);
    s.setProgressWidth(Math.min(100, pct.intValue()));
    s.setOverBudget(totalCommitted.compareTo(budgetAmount) > 0);
    s.setNearLimit(!s.isOverBudget()
            && pct.compareTo(WARNING_PERCENT) >= 0);
}

        return s;
    }

    /** summarize() plus the two lists shown on the dashboard. */
    public DashboardSummary buildDashboard(long userId, YearMonth month) {
        DashboardSummary s = summarize(userId, month);
        s.setRecentExpenses(expenseDao.findRecent(userId, month.atDay(1), month.plusMonths(1).atDay(1), 5));
        s.setUpcomingSubscriptions(
                subscriptionDao.findUpcoming(userId, LocalDate.now().plusDays(UPCOMING_DAYS), 5));
        return s;
    }
}
