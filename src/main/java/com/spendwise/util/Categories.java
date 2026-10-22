package com.spendwise.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Fixed expense categories for the MVP. */
public final class Categories {

    public static final List<String> ALL = Collections.unmodifiableList(Arrays.asList(
            "Food", "Transport", "Rent", "Utilities", "Shopping",
            "Entertainment", "Health", "Education", "Other"));

    private Categories() { }
}
