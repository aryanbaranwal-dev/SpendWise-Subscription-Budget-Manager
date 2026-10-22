package com.spendwise.controller;

import com.spendwise.util.DBUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Setup check: open http://localhost:8080/spendwise/health to see whether the app can
 * reach Oracle and whether the tables exist. Prints no passwords. Remove for production.
 */
@WebServlet("/health")
public class HealthServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        StringBuilder out = new StringBuilder();
        try (Connection c = DBUtil.getConnection()) {
            out.append("1. Database connection: OK\n");
            out.append("   Oracle version: ").append(c.getMetaData().getDatabaseProductVersion().split("\n")[0]).append('\n');
            try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM sw_users")) {
                rs.next();
                out.append("2. Schema check: OK (sw_users exists, ").append(rs.getInt(1)).append(" users)\n");
            } catch (SQLException e) {
                out.append("2. Schema check: FAILED (").append(firstLine(e)).append(")\n");
                out.append("   Run sql/schema.sql as this same database user.\n");
            }
        } catch (SQLException | RuntimeException e) {
            out.append("1. Database connection: FAILED\n   ").append(firstLine(e)).append('\n');
            out.append("   See README.md > Troubleshooting.\n");
        }
        resp.getWriter().print(out);
    }

    private String firstLine(Exception e) {
        String m = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        return m.split("\n")[0];
    }
}
