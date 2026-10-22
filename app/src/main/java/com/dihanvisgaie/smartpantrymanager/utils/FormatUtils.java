package com.dihanvisgaie.smartpantrymanager.utils;

/**
 * FormatUtils - shared display formatting so every screen shows amounts the same way.
 */
public final class FormatUtils {

    private FormatUtils() {
        // static helper only
    }

    /** 2.0 -> "2", 1.5 -> "1.5", 0.25 -> "0.25". */
    public static String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity) && !Double.isInfinite(quantity)) {
            return String.valueOf((long) quantity);
        }
        // Up to 2 decimals, without trailing zeros
        String text = String.format(java.util.Locale.US, "%.2f", quantity);
        return text.replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    /** (250, "g") -> "250 g". */
    public static String formatAmount(double quantity, String unit) {
        return formatQuantity(quantity) + " " + unit;
    }
}

