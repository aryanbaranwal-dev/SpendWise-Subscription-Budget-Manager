package com.spendwise.dao;

import com.spendwise.model.User;
import com.spendwise.util.DBUtil;
import com.spendwise.util.DaoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserDao {

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT user_id, full_name, email, password_hash FROM sw_users WHERE email = ?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User();
                    u.setId(rs.getLong("user_id"));
                    u.setFullName(rs.getString("full_name"));
                    u.setEmail(rs.getString("email"));
                    u.setPasswordHash(rs.getString("password_hash"));
                    return Optional.of(u);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Could not look up user", e);
        }
    }

    /** Throws DaoException (isUniqueViolation) if the email already exists. */
    public void insert(User u) {
        String sql = "INSERT INTO sw_users (user_id, full_name, email, password_hash) "
                + "VALUES (sw_user_seq.NEXTVAL, ?, ?, ?)";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getFullName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPasswordHash());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Could not create user", e);
        }
    }
}
