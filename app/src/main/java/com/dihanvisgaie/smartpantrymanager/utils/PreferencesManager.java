package com.dihanvisgaie.smartpantrymanager.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
    PreferencesManager - wraps SharedPreferences for the Settings screen.

    Settings are simple key-value pairs, so SharedPreferences is a better fit than
    an SQLite table. Pantry and recipe data stay in SQLite.
 */
public class PreferencesManager {

    private static final String PREFS_NAME = "smart_pantry_prefs";

    private static final String KEY_EXPIRY_ALERTS = "expiry_alerts_enabled";
    private static final String KEY_WARNING_DAYS = "expiry_warning_days";
    private static final String KEY_DEFAULT_UNIT = "default_unit";

    private static final boolean DEFAULT_EXPIRY_ALERTS = true;
    private static final int DEFAULT_WARNING_DAYS = 3;
    private static final String DEFAULT_UNIT = "g";

    private final SharedPreferences prefs;

    public PreferencesManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isExpiryAlertsEnabled() {
        return prefs.getBoolean(KEY_EXPIRY_ALERTS, DEFAULT_EXPIRY_ALERTS);
    }

    public void setExpiryAlertsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_EXPIRY_ALERTS, enabled).apply();
    }

    public int getExpiryWarningDays() {
        return prefs.getInt(KEY_WARNING_DAYS, DEFAULT_WARNING_DAYS);
    }

    public void setExpiryWarningDays(int days) {
        prefs.edit().putInt(KEY_WARNING_DAYS, days).apply();
    }

    public String getDefaultUnit() {
        return prefs.getString(KEY_DEFAULT_UNIT, DEFAULT_UNIT);
    }

    public void setDefaultUnit(String unit) {
        prefs.edit().putString(KEY_DEFAULT_UNIT, unit).apply();
    }
}

