package com.spendwise.service;

import com.spendwise.dao.ExpenseDao;
import com.spendwise.model.Expense;
import com.spendwise.util.Categories;
import com.spendwise.util.ValidationUtil;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ExpenseService {

    private final ExpenseDao dao = new ExpenseDao();

    /** A null amount/date means the form text could not be parsed. */
    public Map<String, String> validate(Expense e) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (e.getAmount() == null || e.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            errors.put("amount", "Enter an amount greater than 0 (up to 2 decimals).");
        }
        if (!Categories.ALL.contains(e.getCategory())) {
            errors.put("category", "Choose a category.");
        }
        if (e.getDescription() == null || e.getDescription().isEmpty() || e.getDescription().length() > 255) {
            errors.put("description", "Enter a description (up to 255 characters).");
        }
        if (e.getExpenseDate() == null || !ValidationUtil.yearInRange(e.getExpenseDate())) {
            errors.put("expenseDate", "Enter a valid date.");
        } else if (e.getExpenseDate().isAfter(LocalDate.now())) {
            errors.put("expenseDate", "The date cannot be in the future.");
        }
        return errors;
    }

    public List<Expense> list(long userId, YearMonth month, String categoryOrNull) {
        return dao.findByRange(userId, month.atDay(1), month.plusMonths(1).atDay(1), categoryOrNull);
    }

    public BigDecimal total(List<Expense> expenses) {
        BigDecimal sum = BigDecimal.ZERO.setScale(2);
        for (Expense e : expenses) {
            sum = sum.add(e.getAmount());
        }
        return sum;
    }

    public Optional<Expense> find(long id, long userId) { return dao.findById(id, userId); }
    public void create(Expense e) { dao.insert(e); }
    public boolean update(Expense e) { return dao.update(e); }
    public boolean delete(long id, long userId) { return dao.delete(id, userId); }
}
