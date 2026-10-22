package com.spendwise.controller;

import com.spendwise.model.Expense;
import com.spendwise.service.ExpenseService;
import com.spendwise.util.Categories;
import com.spendwise.util.Flash;
import com.spendwise.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * GET  /expenses                 list (filters: month, category)
 * GET  /expenses?action=new      empty form
 * GET  /expenses?action=edit&id= form filled with an existing expense
 * POST /expenses  action=save    create or update, then redirect
 * POST /expenses  action=delete  delete, then redirect
 */
@WebServlet("/expenses")
public class ExpenseServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final transient ExpenseService service = new ExpenseService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = param(req, "action");
        if ("new".equals(action)) {
            Map<String, String> form = blankForm();
            showForm(req, resp, form);
        } else if ("edit".equals(action)) {
            Optional<Expense> e = service.find(ValidationUtil.parseLong(req.getParameter("id"), 0), userId(req));
            if (!e.isPresent()) {
                Flash.error(req, "That expense was not found.");
                redirect(req, resp, "/expenses");
                return;
            }
            showForm(req, resp, formFrom(e.get()));
        } else {
            showList(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = param(req, "action");
        if ("delete".equals(action)) {
            boolean ok = service.delete(ValidationUtil.parseLong(req.getParameter("id"), 0), userId(req));
            if (ok) {
                Flash.success(req, "Expense deleted.");
            } else {
                Flash.error(req, "That expense was not found.");
            }
            redirect(req, resp, "/expenses?month=" + ValidationUtil.parseMonth(req.getParameter("month")));
        } else if ("save".equals(action)) {
            save(req, resp);
        } else {
            redirect(req, resp, "/expenses");
        }
    }

    private void save(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long id = ValidationUtil.parseLong(req.getParameter("id"), 0);
        Expense e = new Expense();
        e.setId(id);
        e.setUserId(userId(req));
        e.setAmount(ValidationUtil.parseAmount(req.getParameter("amount")));
        e.setCategory(param(req, "category"));
        e.setDescription(param(req, "description"));
        e.setExpenseDate(ValidationUtil.parseDate(req.getParameter("expenseDate")));

        Map<String, String> errors = service.validate(e);
        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            Map<String, String> form = new LinkedHashMap<>();
            form.put("id", String.valueOf(id));
            form.put("amount", param(req, "amount"));
            form.put("category", e.getCategory());
            form.put("description", e.getDescription());
            form.put("expenseDate", param(req, "expenseDate"));
            showForm(req, resp, form);
            return;
        }
        if (id == 0) {
            service.create(e);
            Flash.success(req, "Expense added.");
        } else if (service.update(e)) {
            Flash.success(req, "Expense updated.");
        } else {
            Flash.error(req, "That expense was not found.");
        }
        redirect(req, resp, "/expenses?month=" + YearMonth.from(e.getExpenseDate()));
    }

    private void showList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        YearMonth ym = ValidationUtil.parseMonth(req.getParameter("month"));
        String category = param(req, "category");
        if (!Categories.ALL.contains(category)) {
            category = "";
        }
        List<Expense> expenses = service.list(userId(req), ym, category.isEmpty() ? null : category);
        req.setAttribute("expenses", expenses);
        req.setAttribute("total", service.total(expenses));
        req.setAttribute("category", category);
        req.setAttribute("categories", Categories.ALL);
        putMonthAttributes(req, ym);
        forward(req, resp, "expense-list.jsp");
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp, Map<String, String> form)
            throws ServletException, IOException {
        req.setAttribute("form", form);
        req.setAttribute("editing", !"0".equals(form.get("id")));
        req.setAttribute("categories", Categories.ALL);
        req.setAttribute("today", LocalDate.now().toString());
        req.setAttribute("month", ValidationUtil.parseMonth(req.getParameter("month")).toString());
        forward(req, resp, "expense-form.jsp");
    }

    private Map<String, String> blankForm() {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("id", "0");
        form.put("amount", "");
        form.put("category", "");
        form.put("description", "");
        form.put("expenseDate", LocalDate.now().toString());
        return form;
    }

    private Map<String, String> formFrom(Expense e) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("id", String.valueOf(e.getId()));
        form.put("amount", e.getAmount().toPlainString());
        form.put("category", e.getCategory());
        form.put("description", e.getDescription());
        form.put("expenseDate", e.getExpenseDate().toString());
        return form;
    }
}
