package com.spendwise.dao;

import com.spendwise.model.Expense;
import com.spendwise.util.DBUtil;
import com.spendwise.util.DaoException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Every query filters by user_id, so one user can never read or change another user's rows. */
public class ExpenseDao {

    private static final String COLUMNS = "expense_id, user_id, amount, category, description, expense_date";

    public void insert(Expense e) {
        String sql = "INSERT INTO sw_expenses (expense_id, user_id, amount, category, description, expense_date) "
                + "VALUES (sw_expense_seq.NEXTVAL, ?, ?, ?, ?, ?)";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, e.getUserId());
            ps.setBigDecimal(2, e.getAmount());
            ps.setString(3, e.getCategory());
            ps.setString(4, e.getDescription());
            ps.setDate(5, Date.valueOf(e.getExpenseDate()));
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DaoException("Could not add expense", ex);
        }
    }

    /** @return false when no row matched (wrong id or not this user's expense) */
    public boolean update(Expense e) {
        String sql = "UPDATE sw_expenses SET amount = ?, category = ?, description = ?, expense_date = ? "
                + "WHERE expense_id = ? AND user_id = ?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBigDecimal(1, e.getAmount());
            ps.setString(2, e.getCategory());
            ps.setString(3, e.getDescription());
            ps.setDate(4, Date.valueOf(e.getExpenseDate()));
            ps.setLong(5, e.getId());
            ps.setLong(6, e.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DaoException("Could not update expense", ex);
        }
    }

    public boolean delete(long id, long userId) {
        String sql = "DELETE FROM sw_expenses WHERE expense_id = ? AND user_id = ?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DaoException("Could not delete expense", ex);
        }
    }

    public Optional<Expense> findById(long id, long userId) {
        String sql = "SELECT " + COLUMNS + " FROM sw_expenses WHERE expense_id = ? AND user_id = ?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DaoException("Could not load expense", ex);
        }
    }

    /** Expenses with from <= date < to, newest first. category == null means all categories. */
    public List<Expense> findByRange(long userId, LocalDate from, LocalDate to, String category) {
        String sql = "SELECT " + COLUMNS + " FROM sw_expenses WHERE user_id = ? AND expense_date >= ? "
                + "AND expense_date < ?" + (category == null ? "" : " AND category = ?")
                + " ORDER BY expense_date DESC, expense_id DESC";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            if (category != null) {
                ps.setString(4, category);
            }
            return readAll(ps);
        } catch (SQLException ex) {
            throw new DaoException("Could not list expenses", ex);
        }
    }

    /** Latest N expenses in the date range (ROWNUM works on every Oracle version). */
    public List<Expense> findRecent(long userId, LocalDate from, LocalDate to, int limit) {
        String sql = "SELECT * FROM (SELECT " + COLUMNS + " FROM sw_expenses WHERE user_id = ? "
                + "AND expense_date >= ? AND expense_date < ? "
                + "ORDER BY expense_date DESC, expense_id DESC) WHERE ROWNUM <= ?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            ps.setInt(4, limit);
            return readAll(ps);
        } catch (SQLException ex) {
            throw new DaoException("Could not load recent expenses", ex);
        }
    }

    public BigDecimal sumBetween(long userId, LocalDate from, LocalDate to) {
        String sql = "SELECT NVL(SUM(amount), 0) FROM sw_expenses "
                + "WHERE user_id = ? AND expense_date >= ? AND expense_date < ?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1).setScale(2, RoundingMode.HALF_UP);
            }
        } catch (SQLException ex) {
            throw new DaoException("Could not total expenses", ex);
        }
    }

    private List<Expense> readAll(PreparedStatement ps) throws SQLException {
        List<Expense> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    private Expense map(ResultSet rs) throws SQLException {
        Expense e = new Expense();
        e.setId(rs.getLong("expense_id"));
        e.setUserId(rs.getLong("user_id"));
        e.setAmount(rs.getBigDecimal("amount"));
        e.setCategory(rs.getString("category"));
        e.setDescription(rs.getString("description"));
        e.setExpenseDate(rs.getDate("expense_date").toLocalDate());
        return e;
    }
}
