package com.spendwise.dao;

import com.spendwise.model.Budget;
import com.spendwise.util.DBUtil;
import com.spendwise.util.DaoException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class BudgetDao {

    public Optional<Budget> findByMonth(long userId, LocalDate firstOfMonth) {
        String sql = "SELECT budget_id, user_id, budget_month, amount FROM sw_budgets "
                + "WHERE user_id = ? AND budget_month = ?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setDate(2, Date.valueOf(firstOfMonth));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Budget b = new Budget();
                    b.setId(rs.getLong("budget_id"));
                    b.setUserId(rs.getLong("user_id"));
                    b.setMonth(rs.getDate("budget_month").toLocalDate());
                    b.setAmount(rs.getBigDecimal("amount"));
                    return Optional.of(b);
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DaoException("Could not load budget", ex);
        }
    }

    /**
     * Set-or-update: try UPDATE first; if no row exists, INSERT.
     * The UNIQUE (user_id, budget_month) constraint guarantees no duplicates even if two
     * requests race; in that case the insert fails with ORA-00001 and we update instead.
     */
    public void save(long userId, LocalDate firstOfMonth, BigDecimal amount) {
        try (Connection c = DBUtil.getConnection()) {
            if (update(c, userId, firstOfMonth, amount) == 0) {
                try {
                    insert(c, userId, firstOfMonth, amount);
                } catch (SQLException e) {
                    if (e.getErrorCode() == 1) {
                        update(c, userId, firstOfMonth, amount);
                    } else {
                        throw e;
                    }
                }
            }
        } catch (SQLException ex) {
            throw new DaoException("Could not save budget", ex);
        }
    }

    private int update(Connection c, long userId, LocalDate month, BigDecimal amount) throws SQLException {
        String sql = "UPDATE sw_budgets SET amount = ? WHERE user_id = ? AND budget_month = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBigDecimal(1, amount);
            ps.setLong(2, userId);
            ps.setDate(3, Date.valueOf(month));
            return ps.executeUpdate();
        }
    }

    private void insert(Connection c, long userId, LocalDate month, BigDecimal amount) throws SQLException {
        String sql = "INSERT INTO sw_budgets (budget_id, user_id, budget_month, amount) "
                + "VALUES (sw_budget_seq.NEXTVAL, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setDate(2, Date.valueOf(month));
            ps.setBigDecimal(3, amount);
            ps.executeUpdate();
        }
    }
}
