package com.spendwise.service;

import com.spendwise.dao.BudgetDao;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;

public class BudgetService {

    private static final BigDecimal MAX = new BigDecimal("9999999999.99");
    private final BudgetDao dao = new BudgetDao();

    public Map<String, String> validate(BigDecimal amount) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 || amount.compareTo(MAX) > 0) {
            errors.put("amount", "Enter a budget greater than 0 (up to 2 decimals).");
        }
        return errors;
    }

    /** Creates the month's budget or updates it; one budget per user per month. */
    public void save(long userId, YearMonth month, BigDecimal amount) {
        dao.save(userId, month.atDay(1), amount);
    }
}
