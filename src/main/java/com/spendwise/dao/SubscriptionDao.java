package com.spendwise.dao;

import com.spendwise.model.BillingCycle;
import com.spendwise.model.Subscription;
import com.spendwise.model.SubscriptionStatus;
import com.spendwise.util.DBUtil;
import com.spendwise.util.DaoException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Every query filters by user_id (ownership check built into the SQL). */
public class SubscriptionDao {

    private static final String COLUMNS =
            "sub_id, user_id, sub_name, amount, billing_cycle, next_payment_date, status";

    public void insert(Subscription s) {
        String sql = "INSERT INTO sw_subscriptions (sub_id, user_id, sub_name, amount, billing_cycle, "
                + "next_payment_date, status) VALUES (sw_sub_seq.NEXTVAL, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, s.getUserId());
            ps.setString(2, s.getName());
            ps.setBigDecimal(3, s.getAmount());
            ps.setString(4, s.getBillingCycle().name());
            ps.setDate(5, Date.valueOf(s.getNextPaymentDate()));
            ps.setString(6, s.getStatus().name());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DaoException("Could not add subscription", ex);
        }
    }

    public boolean update(Subscription s) {
        String sql = "UPDATE sw_subscriptions SET sub_name = ?, amount = ?, billing_cycle = ?, "
                + "next_payment_date = ?, status = ? WHERE sub_id = ? AND user_id = ?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setBigDecimal(2, s.getAmount());
            ps.setString(3, s.getBillingCycle().name());
            ps.setDate(4, Date.valueOf(s.getNextPaymentDate()));
            ps.setString(5, s.getStatus().name());
            ps.setLong(6, s.getId());
            ps.setLong(7, s.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DaoException("Could not update subscription", ex);
        }
    }

    public boolean updateNextPaymentDate(long id, long userId, LocalDate date) {
        String sql = "UPDATE sw_subscriptions SET next_payment_date = ? WHERE sub_id = ? AND user_id = ?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ps.setLong(2, id);
            ps.setLong(3, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DaoException("Could not update payment date", ex);
        }
    }

    public boolean delete(long id, long userId) {
        String sql = "DELETE FROM sw_subscriptions WHERE sub_id = ? AND user_id = ?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DaoException("Could not delete subscription", ex);
        }
    }

    public Optional<Subscription> findById(long id, long userId) {
        String sql = "SELECT " + COLUMNS + " FROM sw_subscriptions WHERE sub_id = ? AND user_id = ?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DaoException("Could not load subscription", ex);
        }
    }

    /** Active first, then paused, then cancelled; soonest payment first inside each group. */
    public List<Subscription> findAll(long userId) {
        String sql = "SELECT " + COLUMNS + " FROM sw_subscriptions WHERE user_id = ? "
                + "ORDER BY CASE status WHEN 'ACTIVE' THEN 0 WHEN 'PAUSED' THEN 1 ELSE 2 END, "
                + "next_payment_date, sub_id";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            return readAll(ps);
        } catch (SQLException ex) {
            throw new DaoException("Could not list subscriptions", ex);
        }
    }

    public List<Subscription> findActive(long userId) {
        String sql = "SELECT " + COLUMNS + " FROM sw_subscriptions WHERE user_id = ? AND status = 'ACTIVE' "
                + "ORDER BY next_payment_date, sub_id";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            return readAll(ps);
        } catch (SQLException ex) {
            throw new DaoException("Could not list active subscriptions", ex);
        }
    }

    /** Active subscriptions due on or before 'until' (overdue ones included), soonest first. */
    public List<Subscription> findUpcoming(long userId, LocalDate until, int limit) {
        String sql = "SELECT * FROM (SELECT " + COLUMNS + " FROM sw_subscriptions WHERE user_id = ? "
                + "AND status = 'ACTIVE' AND next_payment_date <= ? "
                + "ORDER BY next_payment_date, sub_id) WHERE ROWNUM <= ?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setDate(2, Date.valueOf(until));
            ps.setInt(3, limit);
            return readAll(ps);
        } catch (SQLException ex) {
            throw new DaoException("Could not list upcoming payments", ex);
        }
    }

    private List<Subscription> readAll(PreparedStatement ps) throws SQLException {
        List<Subscription> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    private Subscription map(ResultSet rs) throws SQLException {
        Subscription s = new Subscription();
        s.setId(rs.getLong("sub_id"));
        s.setUserId(rs.getLong("user_id"));
        s.setName(rs.getString("sub_name"));
        s.setAmount(rs.getBigDecimal("amount"));
        s.setBillingCycle(BillingCycle.valueOf(rs.getString("billing_cycle")));
        s.setNextPaymentDate(rs.getDate("next_payment_date").toLocalDate());
        s.setStatus(SubscriptionStatus.valueOf(rs.getString("status")));
        return s;
    }
}
