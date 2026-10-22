package com.spendwise.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Pattern;

/** Turns form text into typed values. Never throws on bad input. */
public final class ValidationUtil {

    /** Up to 10 digits, optional 1-2 decimals: matches NUMBER(12,2). */
    private static final Pattern AMOUNT = Pattern.compile("^\\d{1,10}(\\.\\d{1,2})?$");
    public static final int MIN_YEAR = 2000;
    public static final int MAX_YEAR = 2100;
    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

    private ValidationUtil() { }

    public static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    /** Amount with scale 2, or null when the text is not valid money. */
    public static BigDecimal parseAmount(String text) {
        String t = trim(text).replace(",", "");
        if (!AMOUNT.matcher(t).matches()) {
            return null;
        }
        return new BigDecimal(t).setScale(2);
    }

    /** Parses yyyy-MM-dd; null when invalid. */
    public static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(trim(text));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** Parses yyyy-MM; current month when missing or invalid. */
    public static YearMonth parseMonth(String text) {
        try {
            YearMonth ym = YearMonth.parse(trim(text));
            if (ym.getYear() >= MIN_YEAR && ym.getYear() <= MAX_YEAR) {
                return ym;
            }
        } catch (DateTimeParseException e) {
            // use default below
        }
        return YearMonth.now();
    }

    public static long parseLong(String text, long fallback) {
        try {
            return Long.parseLong(trim(text));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static boolean yearInRange(LocalDate d) {
        return d.getYear() >= MIN_YEAR && d.getYear() <= MAX_YEAR;
    }

    public static String monthLabel(YearMonth ym) {
        return ym.format(MONTH_LABEL);
    }
}
