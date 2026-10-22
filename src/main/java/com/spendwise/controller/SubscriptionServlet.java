package com.spendwise.controller;

import com.spendwise.model.BillingCycle;
import com.spendwise.model.Subscription;
import com.spendwise.model.SubscriptionStatus;
import com.spendwise.service.SubscriptionService;
import com.spendwise.util.Flash;
import com.spendwise.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * GET  /subscriptions               list
 * GET  /subscriptions?action=new    empty form
 * GET  /subscriptions?action=edit&id=
 * POST /subscriptions action=save | delete | paid
 */
@WebServlet("/subscriptions")
public class SubscriptionServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final transient SubscriptionService service = new SubscriptionService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = param(req, "action");
        if ("new".equals(action)) {
            Map<String, String> form = new LinkedHashMap<>();
            form.put("id", "0");
            form.put("name", "");
            form.put("amount", "");
            form.put("billingCycle", BillingCycle.MONTHLY.name());
            form.put("nextPaymentDate", LocalDate.now().plusMonths(1).toString());
            form.put("status", SubscriptionStatus.ACTIVE.name());
            showForm(req, resp, form);
        } else if ("edit".equals(action)) {
            Optional<Subscription> s = service.find(ValidationUtil.parseLong(req.getParameter("id"), 0), userId(req));
            if (!s.isPresent()) {
                Flash.error(req, "That subscription was not found.");
                redirect(req, resp, "/subscriptions");
                return;
            }
            Map<String, String> form = new LinkedHashMap<>();
            form.put("id", String.valueOf(s.get().getId()));
            form.put("name", s.get().getName());
            form.put("amount", s.get().getAmount().toPlainString());
            form.put("billingCycle", s.get().getBillingCycle().name());
            form.put("nextPaymentDate", s.get().getNextPaymentDate().toString());
            form.put("status", s.get().getStatus().name());
            showForm(req, resp, form);
        } else {
            List<Subscription> subs = service.listAll(userId(req));
            req.setAttribute("subscriptions", subs);
            req.setAttribute("monthlyTotal", service.monthlyTotal(subs));
            forward(req, resp, "subscription-list.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = param(req, "action");
        long id = ValidationUtil.parseLong(req.getParameter("id"), 0);
        if ("delete".equals(action)) {
            if (service.delete(id, userId(req))) {
                Flash.success(req, "Subscription deleted.");
            } else {
                Flash.error(req, "That subscription was not found.");
            }
            redirect(req, resp, "/subscriptions");
        } else if ("paid".equals(action)) {
            Optional<LocalDate> next = service.markPaid(id, userId(req));
            if (next.isPresent()) {
                Flash.success(req, "Marked as paid. Next payment date is now " + next.get() + ".");
            } else {
                Flash.error(req, "That subscription was not found.");
            }
            redirect(req, resp, "/subscriptions");
        } else if ("save".equals(action)) {
            save(req, resp, id);
        } else {
            redirect(req, resp, "/subscriptions");
        }
    }

    private void save(HttpServletRequest req, HttpServletResponse resp, long id) throws ServletException, IOException {
        Subscription s = new Subscription();
        s.setId(id);
        s.setUserId(userId(req));
        s.setName(param(req, "name"));
        s.setAmount(ValidationUtil.parseAmount(req.getParameter("amount")));
        s.setBillingCycle(BillingCycle.fromCode(req.getParameter("billingCycle")));
        s.setNextPaymentDate(ValidationUtil.parseDate(req.getParameter("nextPaymentDate")));
        s.setStatus(SubscriptionStatus.fromCode(req.getParameter("status")));

        Map<String, String> errors = service.validate(s);
        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            Map<String, String> form = new LinkedHashMap<>();
            form.put("id", String.valueOf(id));
            form.put("name", s.getName());
            form.put("amount", param(req, "amount"));
            form.put("billingCycle", param(req, "billingCycle"));
            form.put("nextPaymentDate", param(req, "nextPaymentDate"));
            form.put("status", param(req, "status"));
            showForm(req, resp, form);
            return;
        }
        if (id == 0) {
            service.create(s);
            Flash.success(req, "Subscription added.");
        } else if (service.update(s)) {
            Flash.success(req, "Subscription updated.");
        } else {
            Flash.error(req, "That subscription was not found.");
        }
        redirect(req, resp, "/subscriptions");
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp, Map<String, String> form)
            throws ServletException, IOException {
        req.setAttribute("form", form);
        req.setAttribute("editing", !"0".equals(form.get("id")));
        req.setAttribute("cycles", BillingCycle.values());
        req.setAttribute("statuses", SubscriptionStatus.values());
        forward(req, resp, "subscription-form.jsp");
    }
}
