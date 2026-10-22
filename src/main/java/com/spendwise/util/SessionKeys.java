package com.spendwise.util;

/** Names of attributes stored in the HTTP session. */
public final class SessionKeys {
    public static final String USER = "loggedInUser";
    public static final String CSRF = "csrfToken";
    public static final String FLASH_SUCCESS = "flashSuccess";
    public static final String FLASH_ERROR = "flashError";

    private SessionKeys() { }
}
