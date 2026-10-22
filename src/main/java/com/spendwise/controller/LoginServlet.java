package com.spendwise.controller;

import com.spendwise.filter.CsrfFilter;
import com.spendwise.model.User;
import com.spendwise.service.AuthService;
import com.spendwise.util.SessionKeys;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

@WebServlet("/login")
public class LoginServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final transient AuthService auth = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (currentUser(req) != null) {
            redirect(req, resp, "/dashboard");
            return;
        }
        if ("1".equals(req.getParameter("loggedOut"))) {
            req.setAttribute("flashSuccess", "You have been signed out.");
        }
        forward(req, resp, "login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = param(req, "email");
        Optional<User> user = auth.login(email, req.getParameter("password"));
        if (!user.isPresent()) {
            req.setAttribute("email", email);
            req.setAttribute("loginError", "Incorrect email or password.");
            forward(req, resp, "login.jsp");
            return;
        }
        HttpSession session = req.getSession();
        req.changeSessionId(); // new session id after login (prevents session fixation)
        session.setAttribute(SessionKeys.CSRF, CsrfFilter.newToken());
        session.setAttribute(SessionKeys.USER, user.get());
        redirect(req, resp, "/dashboard");
    }
}
