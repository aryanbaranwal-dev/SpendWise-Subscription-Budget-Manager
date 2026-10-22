# SpendWise - Project Explanation (for viva)

## 1. Architecture

Layered MVC. Each layer talks only to the one below it.

```
Browser
   | HTTP
Filters   EncodingFilter -> CsrfFilter -> AuthFilter
   |
Servlet (Controller)   reads the request, builds a model object, calls a service, chooses a view
   |
Service                business rules: validation, calculations, ownership-safe operations
   |
DAO                    the only place with SQL; PreparedStatement + try-with-resources
   |
Oracle (via DBUtil)

Servlet --forward--> JSP (View, in WEB-INF/views) <-- request attributes (model objects)
```

* **Model**: plain Java objects (`Expense`, `Subscription`, `Budget`, `User`, `DashboardSummary`) and two enums.
* **View**: JSP + JSTL + EL only (scriptlets are disabled in `web.xml`). Views sit in `WEB-INF`, so a browser cannot open them directly.
* **Controller**: one servlet per feature, with an `action` parameter (`new`, `edit`, `save`, `delete`, `paid`).
* **Why services?** Rules live in one place (e.g. monthly-equivalent maths), not scattered in servlets or JSPs.

## 2. Request flow examples

**Adding an expense**
1. `GET /expenses?action=new`: EncodingFilter, CsrfFilter (creates the session token), AuthFilter (user logged in?) -> `ExpenseServlet.doGet` -> forwards to `expense-form.jsp`.
2. User submits the form: `POST /expenses` with `action=save` and a hidden `csrfToken`.
3. CsrfFilter compares the token; mismatch -> 403.
4. `ExpenseServlet.save` parses text into an `Expense` (bad amount or date becomes `null`), sets `userId` from the session, calls `ExpenseService.validate`.
5. Errors: the form is shown again with the user's input and messages. Success: `ExpenseDao.insert`, a flash message is stored, and the browser is **redirected** (Post/Redirect/Get) to the list, so Refresh cannot re-submit.

**Login**: `LoginServlet.doPost` -> `AuthService.login` -> PBKDF2 verify -> `request.changeSessionId()` (prevents session fixation) -> new CSRF token -> `User` (without hash) stored in the session -> redirect to `/dashboard`.

## 3. Database tables (Oracle)

| Table | Key columns | Notes |
|---|---|---|
| `SW_USERS` | `user_id` PK, `email` UNIQUE, `password_hash` | email stored lower-case; hash only, never the password |
| `SW_EXPENSES` | `expense_id` PK, `user_id` FK, `amount NUMBER(12,2)`, `category`, `description`, `expense_date DATE` | CHECK amount > 0; index (user_id, expense_date) |
| `SW_SUBSCRIPTIONS` | `sub_id` PK, `user_id` FK, `sub_name`, `amount`, `billing_cycle`, `next_payment_date`, `status` | CHECK cycle in WEEKLY/MONTHLY/YEARLY, status in ACTIVE/PAUSED/CANCELLED |
| `SW_BUDGETS` | `budget_id` PK, `user_id` FK, `budget_month DATE` (first day), `amount` | UNIQUE (user_id, budget_month): one budget per user per month |

IDs come from sequences (`sw_user_seq` etc.), which work on every Oracle version. Foreign keys use `ON DELETE CASCADE`. Column names avoid Oracle reserved words (`USER`, `DATE`, `MONTH`, `COMMENT`).

## 4. Key classes

| Class | Role |
|---|---|
| `DBUtil` | Reads `db.properties` (never hard-coded credentials) and opens connections |
| `PasswordUtil` | PBKDF2-HMAC-SHA256, random 16-byte salt per password, constant-time compare |
| `ValidationUtil` | Safe parsing of amount (regex, 2 decimals max, `BigDecimal`), date and month |
| `BillingCycle` | Enum holding the monthly-equivalent formula and next-payment-date rule |
| `CsrfFilter` | Session token + POST check |
| `AuthFilter` | Redirects anonymous users to `/login` |
| `*Dao` | SQL. Every query includes `user_id = ?`, so ownership is enforced inside the SQL |
| `DashboardService` | Builds budget, expense, remaining and utilization figures |
| `BaseServlet` | `forward`, `redirect`, flash messages, month navigation helpers |

## 5. Important calculations

* **Money** is always `BigDecimal` (never `double`), stored as `NUMBER(12,2)`.
* **Monthly equivalent** of one subscription (rounded to 2 decimals, HALF_UP):
  * monthly: amount
  * yearly: amount / 12
  * weekly: amount x 52 / 12
* **Subscription estimate** = sum of monthly equivalents of **ACTIVE** subscriptions only.
* **Remaining budget** = monthly budget - recorded expenses for the selected month.
* **Utilization %** = recorded expenses / budget x 100 (1 decimal). Bar width is capped at 100.
* **No double counting:** subscriptions are never added to expenses and never subtracted from remaining; the estimate is a separate figure. "Mark paid" only moves the next payment date and does not create an expense.
* **Upcoming payments:** active subscriptions with `next_payment_date <= today + 30 days` (overdue ones included).
* Example: budget 20000, expenses 7500.50 -> remaining 12499.50, used 37.5%. Subscriptions 199 monthly + 1200 yearly + 10 weekly = 199.00 + 100.00 + 43.33 = **342.33**.

## 6. Security measures

* SQL injection: `PreparedStatement` everywhere; the only text added to SQL is fixed fragments written by the developer.
* Passwords: salted PBKDF2, 310,000 iterations; the iteration count is stored in the hash so it can be raised later.
* Sessions: HttpOnly cookie, 30-minute timeout, session id changed on login, invalidated on logout.
* CSRF: random token per session, required on every POST.
* Authorization: `AuthFilter` for pages; `user_id` in every SQL statement for data.
* XSS: all user text is printed with `<c:out>` (HTML-escaped).
* Headers: `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`, `Cache-Control: no-store`.
* Login errors are identical for unknown email and wrong password, and the dummy hash keeps timing similar.
* Secrets: database password only in `db.properties`, which is git-ignored and not in the ZIP.

## 7. Likely viva questions

* *Why `jakarta.servlet` and not `javax.servlet`?* Tomcat 10 moved to Jakarta EE 9+; packages were renamed.
* *Why redirect after POST?* Prevents duplicate submissions on refresh (Post/Redirect/Get).
* *Why `BigDecimal`?* `double` cannot represent decimal money exactly.
* *Why `WEB-INF/views`?* Files there cannot be requested directly; all access goes through servlets.
* *How do you stop user A from editing user B's expense?* The `WHERE ... AND user_id = ?` clause returns no rows, so the operation reports "not found".
* *What is a sequence?* An Oracle object generating unique numbers; used instead of AUTO_INCREMENT.
* *What would you improve?* Connection pooling, tests, charts, export, password reset, rate-limited login.
