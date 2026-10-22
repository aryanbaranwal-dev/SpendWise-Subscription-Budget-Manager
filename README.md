# SpendWise - Subscription & Budget Manager

A Java web application built with JSP, Jakarta Servlets, JDBC, and Oracle Database for tracking expenses, recurring subscriptions, and monthly budgets. It is designed for **Apache Tomcat 10.1** and uses the `jakarta.servlet.*` APIs.

**Technology stack:** Java 11+ (JDK 17 or 21 recommended), Jakarta Servlet 6.0 / JSP 3.1, JSTL 3.0, JDBC (`ojdbc11`), Oracle Database, HTML, CSS, vanilla JavaScript, Maven WAR, Tomcat 10.1, and Eclipse IDE.

---

## 1. Prerequisites

| Requirement | How to check |
|---|---|
| JDK 11 or newer (a JDK, not just a JRE) | `javac -version` |
| Eclipse IDE for Enterprise Java and Web Developers, version 2023-03 or newer | **Help > About** |
| Apache Tomcat 10.1.x | Run `<tomcat>/bin/version.bat` |
| Oracle Database running, with its listener | Run `lsnrctl status` |
| Internet access for the first Maven build | Maven downloads dependencies on the first build |

> **Compatibility:** Tomcat 9 or older will not work without code changes because this project uses `jakarta.*` APIs rather than `javax.*`.

## 2. Oracle database setup

### 2.1 Find your connection details

Connect with SQL Developer or SQL*Plus and run:

```sql
SELECT banner FROM v$version;
SELECT sys_context('USERENV', 'SERVICE_NAME') FROM dual;
```

The second query returns the service name. For Oracle XE 18c/21c, it is commonly `XEPDB1`. The host is usually `localhost`, and the listener port is usually `1521`. Confirm your actual settings rather than assuming these values.

### 2.2 Create a dedicated application user

Use a dedicated application user rather than `SYS` or `SYSTEM`. If you already have a suitable user, skip this step. Otherwise, connect as a DBA to the **same service** that the application will use and run the following with your own username and a strong password:

```sql
CREATE USER your_user IDENTIFIED BY "choose_a_strong_password";
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE TO your_user;
ALTER USER your_user QUOTA UNLIMITED ON USERS;
```

If your database does not have a `USERS` tablespace, replace it with the appropriate default tablespace.

### 2.3 Create the tables

Connect as the application user and run `sql/schema.sql`.

- **SQL Developer:** Open `sql/schema.sql`, select the correct connection, and press **F5** (Run Script).
- **SQL*Plus:** Run `sqlplus your_user@//localhost:1521/YOUR_SERVICE`, then execute `@sql/schema.sql`.

The schema script is expected to create four sequences and four tables. Check the output for errors. To reset the application's schema later, use `sql/drop_schema.sql` only when you intentionally want to drop the project's objects and their data.

## 3. Database configuration

1. In `src/main/resources/`, copy `db.properties.template` to a new file named `db.properties`.
2. Edit `db.properties` with your own connection details:

```properties
db.host=localhost
db.port=1521
db.service=YOUR_SERVICE_NAME
db.username=YOUR_DB_USERNAME
db.password=YOUR_DB_PASSWORD
```

Replace all placeholder values with the correct values for your environment.

**Security:** `db.properties` is listed in `.gitignore` and must not be committed or shared. The repository should contain only `db.properties.template`, with placeholder values. Never put a real database password in README files, screenshots, commits, or a public repository.

For a SID-style connection, configure `db.url` as appropriate for your Oracle installation, for example:

```properties
db.url=jdbc:oracle:thin:@localhost:1521:XE
```

Alternatively, keep the configuration file outside the project and set the JVM system property `-Dspendwise.config=C:/secure/spendwise-db.properties` or the environment variable `SPENDWISE_CONFIG`. In Eclipse, JVM arguments can be configured through the server launch configuration.

## 4. Import the project into Eclipse

1. Extract the project ZIP to a folder path without spaces, for example `C:\Projects\spendwise`.
2. In Eclipse, select **File > Import > Maven > Existing Maven Projects** and click **Next**.
3. Browse to the extracted project folder, select `pom.xml`, and click **Finish**.
4. Wait for workspace building and Maven dependency downloads to finish.
5. Right-click the project and choose **Maven > Update Project** (usually `Alt+F5`). If needed, select **Force Update**, then click **OK**.
6. Register Tomcat if it is not already configured: **Window > Preferences > Server > Runtime Environments > Add > Apache Tomcat v10.1**. Select the Tomcat installation folder and an appropriate JDK.
7. Under the project’s **Properties > Project Facets**, confirm Dynamic Web Module 6.0 and Java 11 or newer. Under **Targeted Runtimes**, select Tomcat 10.1.
8. If Eclipse reports errors, see the troubleshooting section below.

## 5. Configure and start Tomcat in Eclipse

1. Open **Window > Show View > Servers**.
2. Create an **Apache Tomcat v10.1 Server** if one is not already configured, and add the `spendwise` project to the configured resources.
3. Double-click the server. If port `8080` is already in use, choose an available port such as `8081`, then save the server configuration.
4. Right-click the server and select **Start**, or run the project using **Run As > Run on Server**.
5. Wait for the server startup message in the Console and check for errors.

## 6. First run and demo checklist

1. Open `http://localhost:8081/spendwise/health` if your Tomcat HTTP port is `8081`; otherwise replace `8081` with your configured port. A successful health check should report `Database connection: OK` and `Schema check: OK`.
2. Open `http://localhost:8081/spendwise/` (adjust the port if needed), create an account, and sign in.
3. Use the following sample values to demonstrate the main features:

   - **Monthly budget:** `20000`
   - **Expenses:** `5000` (Rent) and `2500.50` (Food)
   - **Subscriptions:** `199` monthly (Netflix), `1200` yearly (Cloud storage), and `10` weekly (Newspaper)

   The monthly-equivalent subscription estimate is approximately **₹342.33**:
   - Monthly: ₹199.00
   - Yearly: ₹1,200 / 12 = ₹100.00
   - Weekly: ₹10 × 52 / 12 ≈ ₹43.33

   Total recorded expenses are **₹7,500.50**. If the dashboard includes active subscriptions in committed budget usage, total committed usage is approximately **₹7,842.83**, and the remaining budget is approximately **₹12,157.17**. Utilization is approximately **39.21%** of the ₹20,000 budget.

4. Demonstrate account-level data isolation by signing out, signing in as a second user, and confirming that the second user cannot see the first user's records.

> Demo results depend on the application's current implementation and the records entered. Verify the displayed values locally before presenting them.

## 7. Build a WAR without Eclipse (optional)

If Maven is installed and available on your PATH, run this command from the project root:

```bash
mvn clean package
```

The WAR should be generated at `target/spendwise.war` if the build succeeds. You can deploy it to `<tomcat>/webapps/` and start Tomcat using `<tomcat>/bin/startup.bat`. Ensure database configuration is available to the application before running it.

## 8. Troubleshooting

| Symptom | Possible cause and suggested fix |
|---|---|
| Red errors on JSP files, such as “HttpServlet cannot be resolved” | Check that the project targets Tomcat 10.1, run **Maven > Update Project**, then **Project > Clean**. |
| Tomcat will not start because port 8080 is in use | Configure the server to use another available HTTP port, such as 8081. |
| `/health` reports `db.properties not found` | Copy `db.properties.template` to `db.properties`, configure it, then clean/restart the project. |
| `/health` reports a placeholder value | Replace all `YOUR_...` values in `db.properties` with real local configuration values. |
| `ORA-12514` or `ORA-12505` | Check the Oracle service name. Re-run the service-name query in Section 2.1. |
| `ORA-01017` | Check the database username and password. |
| `ORA-28000` | The account may be locked. Unlock it as a DBA if appropriate. |
| `ORA-01950` | The user may lack tablespace quota. Configure the appropriate quota as a DBA. |
| `ORA-00942` | Check that `sql/schema.sql` was run successfully as the same user configured in `db.properties`. |
| `ORA-00955` while running the schema script | An object may already exist. Inspect the schema; use `sql/drop_schema.sql` only if you intend to reset the project's data. |
| Connection refused or listener errors | Start/check the Oracle database and listener services. |
| `ClassNotFoundException: oracle.jdbc.OracleDriver` | Update Maven dependencies and check that Maven Dependencies are included in the web application's deployment assembly. |
| `jakarta.tags.core` cannot be resolved | Update Maven dependencies and clean/redeploy the server. |
| Every page returns 404 | Check the deployed context path and ensure it is `/spendwise/`. |
| `UnsupportedClassVersionError` | Tomcat may be running on an older JDK than the one used to compile the project. |
| Old code appears after edits | Clean the project and server, then restart/redeploy. |
| A POST form returns 403 after the session expires | Reload the page and retry; the form's CSRF token may no longer match the session. |
| Yellow tag warnings appear in shared `.jspf` fragments | These may be Eclipse validation warnings. Confirm the JSPs compile and run correctly on Tomcat. |
| The rupee symbol appears incorrectly | Ensure source files are UTF-8; in Eclipse, check **Window > Preferences > General > Workspace > Text file encoding**. |

## 9. Project structure

```text
spendwise/
├── pom.xml
├── sql/
│   ├── schema.sql
│   └── drop_schema.sql
├── src/main/java/com/spendwise/
│   ├── controller/   # Servlets: authentication, dashboard, expenses, subscriptions, budget, health
│   ├── filter/       # Encoding, CSRF, and authentication filters
│   ├── model/        # User, Expense, Subscription, Budget, DashboardSummary, enums
│   ├── dao/          # JDBC/SQL data-access classes
│   ├── service/      # Business rules and calculations
│   └── util/         # Database, password, validation, flash-message utilities
├── src/main/resources/
│   └── db.properties.template
├── src/main/webapp/
│   ├── index.jsp
│   ├── css/style.css
│   ├── js/app.js
│   └── WEB-INF/
│       ├── web.xml
│       └── views/    # JSP pages and shared fragments
├── .gitignore
├── README.md
├── TESTING.md
└── PROJECT_EXPLANATION.md
```

## 10. Implemented features

- Registration, login, and logout.
- Salted PBKDF2-HMAC-SHA256 password hashing.
- Session-based login, session ID renewal on login, and protected pages.
- CSRF token validation on POST requests.
- Expense creation, listing, editing, and deletion; month/category filters; server-side validation.
- Subscription creation, listing, editing, and deletion; weekly, monthly, and yearly billing cycles; active, paused, and cancelled statuses.
- Monthly-equivalent subscription cost calculation.
- “Mark paid” advances the next payment date by one billing cycle; it does **not** automatically create an expense.
- Monthly budget creation/update, with one budget per user per month.
- Dashboard showing budget, recorded expenses, remaining budget, utilization, subscription estimate, recent expenses, and upcoming payments.
- Responsive layout, empty states, and success/error messages.
- Database queries are scoped by `user_id` to separate each user's records.

## 11. Known limitations

- Verify the project locally using the checklist and `TESTING.md`; do not assume a clean build or successful deployment unless you have run it.
- There is no JDBC connection pool; the application opens connections for database operations. This is acceptable for a small demonstration but not ideal for heavy load.
- Subscriptions do not automatically create expenses; “Mark paid” only advances the next payment date.
- Expense dates cannot be in the future. Categories use a fixed list, and the application displays INR.
- No password reset, email notifications, charts, export, pagination, search, admin role, or account deletion.
- Login has no lockout or rate limiting.
- `/health` is a development aid and should be removed or protected before production deployment.
- No automated unit tests are included. This is a minimum viable project, not production-ready software.

## 12. Future enhancements

Potential improvements include charts, CSV/PDF export, reminders, automatic expense creation when subscriptions are paid, user-defined categories, a Tomcat JNDI connection pool, password reset, pagination/search, and automated tests.

---

## Submission checklist

Before sharing this repository or a ZIP with a reviewer:

- [ ] Confirm the project builds successfully on your machine.
- [ ] Run the app and verify login, expense CRUD, subscription management, budget calculation, and dashboard.
- [ ] Check `TESTING.md` so its results accurately distinguish tests actually run from tests not run.
- [ ] Confirm `db.properties` and all real credentials are excluded.
- [ ] Include `pom.xml`, `src/`, `sql/`, `README.md`, `TESTING.md`, `PROJECT_EXPLANATION.md`, and `.gitignore`.
- [ ] Do not include generated build output such as `target/` unless specifically requested.
