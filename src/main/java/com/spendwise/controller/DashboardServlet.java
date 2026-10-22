package com.spendwise.controller;

import com.spendwise.service.DashboardService;
import com.spendwise.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.YearMonth;

@WebServlet("/dashboard")
public class DashboardServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final transient DashboardService service = new DashboardService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        YearMonth ym = ValidationUtil.parseMonth(req.getParameter("month"));
        req.setAttribute("summary", service.buildDashboard(userId(req), ym));
        putMonthAttributes(req, ym);
        forward(req, resp, "dashboard.jsp");
    }
}
