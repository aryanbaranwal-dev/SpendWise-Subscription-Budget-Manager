# SpendWise - Subscription & Budget Manager

Java web application (JSP + Jakarta Servlets + JDBC + Oracle) for tracking expenses, recurring
subscriptions and a monthly budget. Built for Apache Tomcat 10.1 (`jakarta.servlet.*`).

**Stack:** Java 11+ (JDK 17 or 21 recommended), Jakarta Servlet 6.0 / JSP 3.1, JSTL 3.0,
JDBC with `ojdbc11`, Oracle Database, HTML/CSS/vanilla JS, Maven WAR, Tomcat 10.1, Eclipse.

---

## 1. Prerequisites

| Need | Check |
|---|---|
| JDK 11 or newer (a JDK, not just a JRE) | `javac -version` |
| Eclipse IDE for **Enterprise Java and Web Developers**, 2023-03 or newer | Help > About |
| Apache Tomcat **10.1.x** (zip, extracted to a folder without spaces) | `<tomcat>/bin/version.bat` |
| Oracle Database running, with its listener | `lsnrctl status` |
| Internet on first build (Maven downloads libraries) | |

Tomcat 9 or older will NOT work: it uses `javax.*`, this project uses `jakarta.*`.

## 2. Oracle setup

### 2.1 Find your connection details
Connect with SQL Developer or SQL*Plus as any user and run:
```sql
SELECT banner FROM v$version;
SELECT sys_context('USERENV','SERVICE_NAME') FROM dual;
```
The second query gives the **service name** (for XE 18c/21c it is usually `XEPDB1`). Host is
usually `localhost`, port `1521`. `lsnrctl status` lists services too.

### 2.2 Application user
Use a dedicated user, not SYS or SYSTEM. If you already have one, skip this. Otherwise, connected as a
DBA **to the same service name** (for example `XEPDB1`), run, choosing your own username and password:
```sql
CREATE USER your_user IDENTIFIED BY "choose_a_strong_password";
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE TO your_user;
ALTER USER your_user QUOTA UNLIMITED ON USERS;
```
(If your database has no `USERS` tablespace, replace it with your default tablespace.)

### 2.3 Create the tables
Connect **as that application user** and run `sql/schema.sql`:
* **SQL Developer:** open the file, choose your connection, press **F5** (Run Script).
* **SQL*Plus:** `sqlplus your_user@//localhost:1521/YOUR_SERVICE` then `@sql/schema.sql`

Expect 4 sequences and 4 tables created, with no errors. To start over later, run `sql/drop_schema.sql`.

## 3. Database configuration (where your credentials go)

1. In `src/main/resources/` copy `db.properties.template` to **`db.properties`**.
2. Edit `db.properties`:
```properties
db.host=localhost
db.port=1521
db.service=YOUR_SERVICE_NAME
db.username=YOUR_DB_USERNAME
db.password=YOUR_DB_PASSWORD
```
`db.properties` is in `.gitignore` and is intentionally excluded from this source package. Never commit or share it.
For a SID-style connection set `db.url=jdbc:oracle:thin:@localhost:1521:XE` instead.

Optional: keep the file outside the project and point to it with a Tomcat VM argument
`-Dspendwise.config=C:/secure/spendwise-db.properties` (Eclipse: Servers > double-click server >
Open launch configuration > Arguments > VM arguments) or the environment variable `SPENDWISE_CONFIG`.

## 4. Import into Eclipse

1. Extract the ZIP to a folder **without spaces** (for example `C:\projects\spendwise`).
2. Eclipse: **File > Import > Maven > Existing Maven Projects** > Next > Browse to the extracted
   `spendwise` folder > tick `pom.xml` > Finish. Wait for "Building workspace" / Maven downloads to finish
   (bottom-right progress bar).
3. Right-click the project > **Maven > Update Project** (Alt+F5) > tick *Force Update* > OK.
4. Register Tomcat (once): **Window > Preferences > Server > Runtime Environments > Add >
   Apache Tomcat v10.1** > point to your Tomcat folder and choose your JDK.
5. Right-click project > **Properties > Project Facets** and confirm: *Dynamic Web Module = 6.0*,
   *Java = 11 or higher*. Under **Targeted Runtimes** tick *Apache Tomcat v10.1*.
6. If you see red errors in the Problems view for this project, see Troubleshooting below.

## 5. Configure and start Tomcat in Eclipse

1. **Window > Show View > Servers** > "No servers are available. Click this link to create a new server".
2. Choose **Apache > Tomcat v10.1 Server** > Next > add **spendwise** to *Configured* > Finish.
3. Double-click the server. **If Oracle XE is installed, port 8080 is often already used by it:** set
   *HTTP/1.1* port to **8081**. Save (Ctrl+S).
4. Right-click the server > **Start** (or right-click project > **Run As > Run on Server**).
5. Wait for `Server startup in [...] milliseconds` in the Console, with no red stack trace.

## 6. First run: check, register, demonstrate

1. Open **http://localhost:8080/spendwise/health** (use 8081 if you changed it). You should see
   `Database connection: OK` and `Schema check: OK`. If not, fix that first (Troubleshooting).
2. Open **http://localhost:8080/spendwise/** and click *Create an account*, then sign in.
3. Suggested demo (numbers are easy to verify):
   * **Budget:** Budget page > set the current month to `20000`.
   * **Expenses:** add `5000` (Rent), `2500.50` (Food).
   * **Subscriptions:** `Netflix` 199 Monthly, `Cloud storage` 1200 Yearly, `Newspaper` 10 Weekly.
   * **Dashboard:** Expenses `7,500.50`, Remaining `12,499.50`, Used `37.5%`,
     Subscriptions per month `342.33`; the dashboard counts active subscriptions at their monthly-equivalent cost as part of committed budget usage.
4. Show security: sign out, then try `/dashboard` (redirects to login); register a second user and
   confirm they see none of the first user's data.

## 7. Build a WAR without Eclipse (optional)

```
mvn clean package
```
Copy `target/spendwise.war` to `<tomcat>/webapps/`, start `<tomcat>/bin/startup.bat`, open
`http://localhost:8080/spendwise/`. (`db.properties` must exist in `src/main/resources` before building,
or use the `-Dspendwise.config` option above.)

## 8. Troubleshooting

| Symptom | Cause and fix |
|---|---|
| Red errors on `.jsp` files such as "superclass ... not found" or "HttpServlet cannot be resolved" | Project not tied to Tomcat 10.1. Properties > Targeted Runtimes > tick Tomcat v10.1; Maven > Update Project; Project > Clean. |
| Tomcat will not start: "port 8080 already in use" | Oracle XE uses 8080. Change the server HTTP port to 8081. |
| `/health` says `db.properties not found` | You did not copy the template. See section 3. Then Project > Clean and restart the server. |
| `/health` says `please set a real value for ...` | A `YOUR_...` placeholder is still in `db.properties`. |
| `ORA-12514` / `ORA-12505` | Wrong service name. Re-run the query in 2.1, or use `db.url` with a SID. |
| `ORA-01017` | Wrong username or password. |
| `ORA-28000` | Account locked: `ALTER USER your_user ACCOUNT UNLOCK;` as DBA. |
| `ORA-01950` | No quota: `ALTER USER your_user QUOTA UNLIMITED ON USERS;` |
| `ORA-00942` table or view does not exist | `schema.sql` was not run as this same user. |
| `ORA-00955` while running schema | Objects already exist. Use `sql/drop_schema.sql` first, or skip. |
| Listener errors / "Connection refused" | Start the Oracle services (`OracleService...` and `...TNSListener`). |
| `ClassNotFoundException: oracle.jdbc.OracleDriver` / "driver is not deployed" | Maven > Update Project; Server > Clean; check Deployment Assembly shows Maven Dependencies mapped to `WEB-INF/lib`. |
| Oracle 11g or 12.1 and the driver refuses to connect | In `pom.xml` replace `ojdbc11` 23.5.0.24.07 with `com.oracle.database.jdbc:ojdbc8:19.25.0.0` (needs JDK 8+). |
| "The absolute uri: jakarta.tags.core cannot be resolved" | JSTL jars missing from `WEB-INF/lib`: Maven > Update Project, then Server > Clean. |
| 404 for every page | Wrong context path. The URL must start with `/spendwise/`. Check Modules tab of the server. |
| `UnsupportedClassVersionError` | The JDK running Tomcat is older than the compile level. Choose a JDK 11+ for the server runtime. |
| Old code still runs after editing | Server > Clean, Project > Clean, then restart. |
| "Request blocked ... session expired" (403) | The form's security token no longer matches (session expired or cookies disabled). Reload the page and retry. |
| Eclipse shows yellow warnings in `common/*.jspf` such as "Unknown tag (c:...)" | Harmless. Those fragments are included into pages that declare the taglibs; Tomcat compiles them correctly. |
| ₹ shows as garbage | Make sure files are UTF-8 (Window > Preferences > General > Workspace > Text file encoding: UTF-8). |

## 9. Project structure

```
spendwise/
  pom.xml                      Maven WAR configuration
  sql/schema.sql               tables, sequences, constraints, indexes
  sql/drop_schema.sql          deletes everything (reset)
  src/main/java/com/spendwise/
    controller/                Servlets (Login, Register, Logout, Dashboard, Expense,
                               Subscription, Budget, Health) + BaseServlet
    filter/                    EncodingFilter, CsrfFilter, AuthFilter
    model/                     User, Expense, Subscription, Budget, DashboardSummary, enums
    dao/                       UserDao, ExpenseDao, SubscriptionDao, BudgetDao (all SQL/JDBC)
    service/                   AuthService, ExpenseService, SubscriptionService,
                               BudgetService, DashboardService (rules and calculations)
    util/                      DBUtil, PasswordUtil, ValidationUtil, Flash, Categories, ...
  src/main/resources/db.properties.template
  src/main/webapp/
    index.jsp, css/style.css, js/app.js
    WEB-INF/web.xml
    WEB-INF/views/*.jsp        pages (not directly reachable from the browser)
    WEB-INF/views/common/*.jspf  shared header/footer fragments
  README.md  TESTING.md  PROJECT_EXPLANATION.md  .gitignore
```

## 10. Implemented features

* Registration, login, logout; PBKDF2-HMAC-SHA256 salted password hashing; session login;
  session id renewed on login; protected pages; CSRF token on every POST.
* Expenses: add, list, edit, delete; month and category filters; server-side validation.
* Subscriptions: add, list, edit, delete; weekly/monthly/yearly; statuses active/paused/cancelled;
  monthly-equivalent cost; "Mark paid" (advances the next payment date by one cycle);
  overdue and due-soon text.
* Monthly budget: one per user per month (database unique constraint), set or update.
* Dashboard: budget, expenses, remaining, utilization %, subscription estimate (separate), recent
  expenses, upcoming payments (next 30 days, overdue included), month navigation.
* Responsive layout, empty states, success/error messages.
* Every query filters by `user_id`, so users can only reach their own records.

## 11. Known limitations (honest list)

* **Not tested here against a live Oracle database or a running Tomcat** (see TESTING.md for exactly what
  was and was not verified). You must run the checklist locally.
* No connection pool: one JDBC connection per query. Fine for a demo, not for heavy load.
* Subscriptions never create expenses automatically; "Mark paid" only moves the date.
* Expense dates cannot be in the future. Fixed category list. Single currency (INR, shown as the rupee sign).
* No password reset, email, charts, export, pagination, search, admin role, or account deletion.
* Login has no lockout or rate limiting.
* The `/health` page is a development aid; remove the servlet before any real deployment.
* No automated unit tests are packaged; this is an MVP, not production-ready software.

## 12. Future enhancements

Charts, CSV/PDF export, reminders, auto-created expenses from paid subscriptions, categories per user,
connection pool (Tomcat JNDI), password reset, pagination and search, automated tests.
