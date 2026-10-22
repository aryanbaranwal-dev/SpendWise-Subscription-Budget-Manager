package com.spendwise.service;

import com.spendwise.dao.UserDao;
import com.spendwise.model.User;
import com.spendwise.util.DaoException;
import com.spendwise.util.PasswordUtil;
import com.spendwise.util.ValidationUtil;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class AuthService {

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private final UserDao userDao = new UserDao();

    /** @return field -> message map; empty means the account was created. */
    public Map<String, String> register(String fullName, String email, String password, String confirm) {
        Map<String, String> errors = new LinkedHashMap<>();
        String name = ValidationUtil.trim(fullName);
        String mail = normalizeEmail(email);
        String pw = password == null ? "" : password;

        if (name.length() < 2 || name.length() > 100) {
            errors.put("fullName", "Enter your name (2 to 100 characters).");
        }
        if (mail.length() > 150 || !EMAIL.matcher(mail).matches()) {
            errors.put("email", "Enter a valid email address.");
        }
        if (pw.length() < 8 || pw.length() > 128 || !pw.matches(".*[A-Za-z].*") || !pw.matches(".*\\d.*")) {
            errors.put("password", "Use 8 or more characters with at least one letter and one number.");
        }
        if (!pw.equals(confirm)) {
            errors.put("confirm", "The two passwords do not match.");
        }
        if (!errors.isEmpty()) {
            return errors;
        }
        if (userDao.findByEmail(mail).isPresent()) {
            errors.put("email", "That email is already registered. Try signing in.");
            return errors;
        }
        User u = new User();
        u.setFullName(name);
        u.setEmail(mail);
        u.setPasswordHash(PasswordUtil.hash(pw));
        try {
            userDao.insert(u);
        } catch (DaoException e) {
            if (e.isUniqueViolation()) { // two people registering the same email at once
                errors.put("email", "That email is already registered. Try signing in.");
                return errors;
            }
            throw e;
        }
        return errors;
    }

    /** Returns the user (password hash removed) when the credentials are right. */
    public Optional<User> login(String email, String password) {
        String mail = normalizeEmail(email);
        String pw = password == null ? "" : password;
        Optional<User> found = userDao.findByEmail(mail);
        if (!found.isPresent()) {
            PasswordUtil.verifyDummy(pw);
            return Optional.empty();
        }
        User u = found.get();
        if (!PasswordUtil.verify(pw, u.getPasswordHash())) {
            return Optional.empty();
        }
        u.setPasswordHash(null);
        return Optional.of(u);
    }

    private String normalizeEmail(String email) {
        return ValidationUtil.trim(email).toLowerCase();
    }
}
