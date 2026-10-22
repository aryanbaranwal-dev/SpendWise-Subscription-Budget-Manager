package com.spendwise.filter;

import com.spendwise.util.SessionKeys;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * CSRF protection. Each session gets a random token. Every form includes it as a hidden
 * field named "csrfToken"; any POST without the matching token is rejected with 403.
 */
public class CsrfFilter implements Filter {

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(true);
        String token = (String) session.getAttribute(SessionKeys.CSRF);
        if (token == null) {
            token = newToken();
            session.setAttribute(SessionKeys.CSRF, token);
        }

        if ("POST".equalsIgnoreCase(req.getMethod())) {
            String sent = req.getParameter("csrfToken");
            boolean ok = sent != null && MessageDigest.isEqual(
                    sent.getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8));
            if (!ok) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Your session expired or the form was invalid. Please go back, reload the page and try again.");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
