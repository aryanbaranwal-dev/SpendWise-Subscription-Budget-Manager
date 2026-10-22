package com.spendwise.controller;

import com.spendwise.service.AuthService;
import com.spendwise.util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@WebServlet("/register")
public class RegisterServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final transient AuthService auth = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (currentUser(req) != null) {
            redirect(req, resp, "/dashboard");
            return;
        }
        forward(req, resp, "register.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String fullName = param(req, "fullName");
        String email = param(req, "email");
        Map<String, String> errors = auth.register(fullName, email,
                req.getParameter("password"), req.getParameter("confirm"));
        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            req.setAttribute("fullName", fullName);
            req.setAttribute("email", email);
            forward(req, resp, "register.jsp");
            return;
        }
        Flash.success(req, "Account created. Sign in to continue.");
        redirect(req, resp, "/login");
    }
}
