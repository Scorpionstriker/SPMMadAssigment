package com.dihanvisgaie.smartpantrymanager.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
    DateUtils - expiry date helpers. Dates are stored as "yyyy-MM-dd" text in SQLite.
 */
public final class DateUtils {

    public static final String DATE_PATTERN = "yyyy-MM-dd";

    private static final double MILLIS_PER_DAY = 24.0 * 60 * 60 * 1000;

    private DateUtils() {
        // static helper only
    }

    /**
        Whole days from today until the given date: 0 = today, 1 = tomorrow,
        negative = already expired. Returns null if there is no valid date.
     */
    public static Long daysUntil(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN, Locale.US);
        format.setLenient(false);
        // reject impossible dates such as 2026-02-31
        try {
            Date expiry = format.parse(isoDate);
            if (expiry == null) {
                return null;
            }
            return Math.round((expiry.getTime() - startOfToday()) / MILLIS_PER_DAY);
        } catch (ParseException e) {
            return null;
        }
    }

    /** True if the date is before today. */
    public static boolean isExpired(String isoDate) {
        Long days = daysUntil(isoDate);
        return days != null && days < 0;
    }

    /** Today at 00:00:00.000, so differences are in whole days. */
    private static long startOfToday() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return today.getTimeInMillis();
    }
}
