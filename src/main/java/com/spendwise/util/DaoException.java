package com.spendwise.util;

import java.sql.SQLException;

/** Unchecked wrapper so callers are not forced to handle SQLException everywhere. */
public class DaoException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final int oracleErrorCode;

    public DaoException(String message, SQLException cause) {
        super(message + " (" + cause.getMessage() + ")", cause);
        this.oracleErrorCode = cause.getErrorCode();
    }

    /** ORA-00001: unique constraint violated (duplicate email or budget month). */
    public boolean isUniqueViolation() {
        return oracleErrorCode == 1;
    }
}
