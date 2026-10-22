package com.spendwise.controller;

import com.spendwise.model.User;
import com.spendwise.util.Flash;
import com.spendwise.util.SessionKeys;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.YearMonth;

/** Shared helpers for all controllers. */
public abstract class BaseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected User currentUser(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s == null ? null : (User) s.getAttribute(SessionKeys.USER);
    }

    /** Only call on URLs protected by AuthFilter. */
    protected long userId(HttpServletRequest req) {
        return currentUser(req).getId();
    }

    protected void forward(HttpServletRequest req, HttpServletResponse resp, String view)
            throws ServletException, IOException {
        Flash.expose(req);
        req.getRequestDispatcher("/WEB-INF/views/" + view).forward(req, resp);
    }

    protected void redirect(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
        resp.sendRedirect(req.getContextPath() + path);
    }

    protected String param(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? "" : v.trim();
    }

    /** Adds month, monthLabel, prevMonth and nextMonth attributes used by the month navigator. */
    protected void putMonthAttributes(HttpServletRequest req, YearMonth ym) {
        req.setAttribute("month", ym.toString());
        req.setAttribute("monthLabel", com.spendwise.util.ValidationUtil.monthLabel(ym));
        req.setAttribute("prevMonth", ym.minusMonths(1).toString());
        req.setAttribute("nextMonth", ym.plusMonths(1).toString());
    }
}
