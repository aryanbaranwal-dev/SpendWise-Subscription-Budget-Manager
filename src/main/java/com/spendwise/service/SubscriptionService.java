package com.spendwise.service;

import com.spendwise.dao.SubscriptionDao;
import com.spendwise.model.Subscription;
import com.spendwise.util.ValidationUtil;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SubscriptionService {

    private final SubscriptionDao dao = new SubscriptionDao();

    public Map<String, String> validate(Subscription s) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (s.getName() == null || s.getName().isEmpty() || s.getName().length() > 100) {
            errors.put("name", "Enter a name (up to 100 characters).");
        }
        if (s.getAmount() == null || s.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            errors.put("amount", "Enter an amount greater than 0 (up to 2 decimals).");
        }
        if (s.getBillingCycle() == null) {
            errors.put("billingCycle", "Choose a billing cycle.");
        }
        if (s.getStatus() == null) {
            errors.put("status", "Choose a status.");
        }
        if (s.getNextPaymentDate() == null || !ValidationUtil.yearInRange(s.getNextPaymentDate())) {
            errors.put("nextPaymentDate", "Enter a valid next payment date.");
        }
        return errors;
    }

    public List<Subscription> listAll(long userId) { return dao.findAll(userId); }
    public Optional<Subscription> find(long id, long userId) { return dao.findById(id, userId); }
    public void create(Subscription s) { dao.insert(s); }
    public boolean update(Subscription s) { return dao.update(s); }
    public boolean delete(long id, long userId) { return dao.delete(id, userId); }

    /** Sum of monthly-equivalent cost of ACTIVE subscriptions only. */
    public BigDecimal monthlyTotal(List<Subscription> subs) {
        BigDecimal sum = BigDecimal.ZERO.setScale(2);
        for (Subscription s : subs) {
            if (s.isActive()) {
                sum = sum.add(s.getMonthlyEquivalent());
            }
        }
        return sum;
    }

    /**
     * "Mark as paid": moves the next payment date forward by one billing cycle.
     * It does NOT create an expense, so nothing is counted twice.
     * @return the new date, or empty if the subscription is not this user's
     */
    public Optional<LocalDate> markPaid(long id, long userId) {
        Optional<Subscription> found = dao.findById(id, userId);
        if (!found.isPresent()) {
            return Optional.empty();
        }
        Subscription s = found.get();
        LocalDate next = s.getBillingCycle().nextAfter(s.getNextPaymentDate());
        return dao.updateNextPaymentDate(id, userId, next) ? Optional.of(next) : Optional.empty();
    }
}
