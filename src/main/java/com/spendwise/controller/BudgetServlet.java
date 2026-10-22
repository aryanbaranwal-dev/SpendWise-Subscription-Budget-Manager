package com.spendwise.controller;

import com.spendwise.model.DashboardSummary;
import com.spendwise.service.BudgetService;
import com.spendwise.service.DashboardService;
import com.spendwise.util.Flash;
import com.spendwise.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;

/** GET /budget?month=yyyy-MM shows the month; POST saves (creates or updates) its budget. */
@WebServlet("/budget")
public class BudgetServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final transient BudgetService budgets = new BudgetService();
    private final transient DashboardService dashboard = new DashboardService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        YearMonth ym = ValidationUtil.parseMonth(req.getParameter("month"));
        DashboardSummary summary = dashboard.summarize(userId(req), ym);
        req.setAttribute("summary", summary);
        req.setAttribute("amountValue", summary.isBudgetSet() ? summary.getBudget().toPlainString() : "");
        putMonthAttributes(req, ym);
        forward(req, resp, "budget.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        YearMonth ym = ValidationUtil.parseMonth(req.getParameter("month"));
        BigDecimal amount = ValidationUtil.parseAmount(req.getParameter("amount"));
        Map<String, String> errors = budgets.validate(amount);
        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            req.setAttribute("summary", dashboard.summarize(userId(req), ym));
            req.setAttribute("amountValue", param(req, "amount"));
            putMonthAttributes(req, ym);
            forward(req, resp, "budget.jsp");
            return;
        }
        budgets.save(userId(req), ym, amount);
        Flash.success(req, "Budget for " + ValidationUtil.monthLabel(ym) + " saved.");
        redirect(req, resp, "/budget?month=" + ym);
    }
}
