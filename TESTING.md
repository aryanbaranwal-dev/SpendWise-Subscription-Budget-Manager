# SpendWise - Testing

## A. What was and was NOT verified before delivery

| Item | Status |
|---|---|
| All Java sources compile (`javac --release 11 -Xlint:all`) against the Tomcat 10.1.55 Servlet API | **Done, 0 errors, 0 warnings** |
| Logic checks run on compiled classes: monthly-equivalent maths, rounding, next-payment dates, amount/date/month parsing, password hash and verify, expense/subscription/budget/registration validation, due-text (46 checks) | **Done, 46 passed, 0 failed** |
| Static checks: XML well-formed, JSP tag balance, includes exist, every forwarded view exists, every link maps to a servlet, every POST form has a CSRF token, no scriptlets, no `javax`, EL properties exist on models | **Done, passed** |
| `mvn clean package` | **NOT run.** The build sandbox had no access to Maven Central. Run it locally. |
| JSP compilation (Jasper + JSTL) | **NOT run** (JSTL jars unavailable offline). Will first be exercised when you start Tomcat. |
| Oracle connectivity, SQL statements, `schema.sql` | **NOT tested** (no Oracle in the sandbox). The SQL uses only standard Oracle syntax, but you must run it. |
| Tomcat deployment, filters, sessions, CSRF flow in a browser | **NOT tested** |

Everything in sections B to I below must be run by you.

## B. Setup checks

| # | Step | Expected | Pass |
|---|---|---|---|
| B1 | Run `sql/schema.sql` as the app user | 4 sequences, 4 tables, 2 indexes created, no errors | [ ] |
| B2 | Start Tomcat, open `/spendwise/health` | "Database connection: OK" and "Schema check: OK" | [ ] |
| B3 | Open `/spendwise/` | Redirects to the sign-in page | [ ] |

## C. Registration and login

| # | Step | Expected | Pass |
|---|---|---|---|
| C1 | Register with valid data | Redirect to sign-in with "Account created" | [ ] |
| C2 | Register the same email again (also with different upper/lower case) | Error "already registered" | [ ] |
| C3 | Register with password `abcdefgh` | Error: needs a letter and a number | [ ] |
| C4 | Register with mismatched confirmation | Error on confirm field | [ ] |
| C5 | In SQL: `SELECT email, password_hash FROM sw_users;` | Hash starts with `pbkdf2_sha256$`, no plain password | [ ] |
| C6 | Sign in with wrong password | "Incorrect email or password." (same message for unknown email) | [ ] |
| C7 | Sign in correctly | Dashboard opens, name shown in sidebar | [ ] |
| C8 | Click Sign out | Sign-in page with "signed out" message | [ ] |
| C9 | After sign out press the browser Back button | Dashboard is not shown (redirects or reloads to sign-in) | [ ] |

## D. Session protection

| # | Step | Expected | Pass |
|---|---|---|---|
| D1 | Signed out, open `/spendwise/dashboard`, `/expenses`, `/subscriptions`, `/budget` | Each redirects to sign-in | [ ] |
| D2 | Open `/spendwise/WEB-INF/views/dashboard.jsp` | 404 (views are not directly reachable) | [ ] |
| D3 | Signed in, open `/spendwise/logout` by typing the URL (GET) | Not signed out (logout needs a POST) | [ ] |
| D4 | Open `/spendwise/health` | Works without login (dev aid only) | [ ] |

## E. Expenses

| # | Step | Expected | Pass |
|---|---|---|---|
| E1 | Add 5000, Rent, "Monthly rent", today | "Expense added." and it appears in the table | [ ] |
| E2 | Add 2500.50, Food, "Groceries", today | Month total shows 7,500.50 | [ ] |
| E3 | Submit amount `0`, `-5`, `abc`, `1.234` | Error under the amount field each time | [ ] |
| E4 | Submit empty description / no category | Errors shown, entered values kept | [ ] |
| E5 | Submit a future date | Error "cannot be in the future" | [ ] |
| E6 | Edit an expense and save | "Expense updated." Values changed | [ ] |
| E7 | Delete: cancel the confirm dialog, then confirm | Cancel keeps it; confirm removes it | [ ] |
| E8 | Filter by category Food | Only Food rows; total matches | [ ] |
| E9 | Previous/Next month links | Month changes; empty month shows the empty state | [ ] |
| E10 | Press Add expense, then browser Refresh after the redirect | No duplicate row (Post/Redirect/Get) | [ ] |

## F. Subscriptions

| # | Step | Expected | Pass |
|---|---|---|---|
| F1 | Add Netflix 199.00 Monthly, active, due in 3 days | Listed, per month 199.00, "Due in 3 days" | [ ] |
| F2 | Add Cloud storage 1200.00 Yearly | Per month 100.00 | [ ] |
| F3 | Add Newspaper 10.00 Weekly | Per month 43.33 | [ ] |
| F4 | Header total "Active, per month" | 342.33 | [ ] |
| F5 | Set Netflix to Cancelled (edit) | Row greyed; total becomes 143.33; absent from dashboard upcoming | [ ] |
| F6 | Add a subscription dated yesterday | Shows "Overdue by 1 day" in red | [ ] |
| F7 | Mark paid on a monthly subscription | Next payment date moves forward one month; no expense is created | [ ] |
| F8 | Submit empty name / bad amount / invalid date | Errors shown | [ ] |
| F9 | Delete a subscription | Removed after confirm | [ ] |

## G. Budget and dashboard calculations

Using budget 20000, expenses 5000 + 2500.50 and the three active subscriptions above.

| # | Step | Expected | Pass |
|---|---|---|---|
| G1 | Dashboard with no budget set | Empty state with "Set budget" | [ ] |
| G2 | Budget page: save 20000 for this month | "Budget ... saved." | [ ] |
| G3 | Save a different amount for the same month | Amount replaced; in SQL `SELECT COUNT(*) FROM sw_budgets` still 1 for that month | [ ] |
| G4 | Dashboard | Budget 20,000.00; Expenses 7,500.50; Remaining **12,499.50**; Used **37.5%** | [ ] |
| G5 | Subscription figure | 342.33, labelled separately, **not** deducted from Remaining | [ ] |
| G6 | Add expense 10000 | Used 87.5% (amber, near limit); remaining 2,499.50 | [ ] |
| G7 | Add expense 5000 | Remaining shown as "Over budget ... by 2,500.50" in red; bar capped at 100% | [ ] |
| G8 | Budget `0`, `-1`, `abc` | Validation error | [ ] |
| G9 | Switch to a month with expenses but no budget | Remaining not shown; empty state offers Set budget | [ ] |

## H. User data isolation

| # | Step | Expected | Pass |
|---|---|---|---|
| H1 | Register user B (private window). B sees dashboard, expenses, subscriptions | All empty | [ ] |
| H2 | As A note an expense id from the Edit link (`...action=edit&id=N`). As B open that URL | "That expense was not found." | [ ] |
| H3 | Same for a subscription edit URL | "not found" | [ ] |
| H4 | As B, craft a delete POST for A's id (browser dev tools) | "not found"; A's row still exists in SQL | [ ] |
| H5 | B sets a budget for the same month as A | Both saved independently | [ ] |

## I. Security and robustness

| # | Step | Expected | Pass |
|---|---|---|---|
| I1 | In dev tools remove the `csrfToken` field and submit any form | 403 "Request blocked" page | [ ] |
| I2 | Add expense description `<script>alert(1)</script>` | Shown as plain text, no alert; stored as typed | [ ] |
| I3 | Add description `' OR 1=1 --` | Stored as plain text, no error (prepared statements) | [ ] |
| I4 | Open `/spendwise/nothing` | Friendly 404 page | [ ] |
| I5 | Stop Oracle listener and open the dashboard | Friendly error page, no stack trace in the browser | [ ] |
| I6 | Narrow the browser window below 900 px | Sidebar becomes a Menu button; tables scroll sideways | [ ] |
