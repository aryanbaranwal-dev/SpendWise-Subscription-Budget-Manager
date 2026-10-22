package com.spendwise.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * One-time messages that survive a redirect (Post/Redirect/Get).
 * success()/error() store a message in the session; expose() moves it to the
 * request so the JSP shows it exactly once.
 */
public final class Flash {

    private Flash() { }

    public static void success(HttpServletRequest req, String message) {
        req.getSession().setAttribute(SessionKeys.FLASH_SUCCESS, message);
    }

    public static void error(HttpServletRequest req, String message) {
        req.getSession().setAttribute(SessionKeys.FLASH_ERROR, message);
    }

    public static void expose(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return;
        }
        move(session, req, SessionKeys.FLASH_SUCCESS);
        move(session, req, SessionKeys.FLASH_ERROR);
    }

    private static void move(HttpSession session, HttpServletRequest req, String key) {
        Object value = session.getAttribute(key);
        if (value != null) {
            req.setAttribute(key, value);
            session.removeAttribute(key);
        }
    }
}
