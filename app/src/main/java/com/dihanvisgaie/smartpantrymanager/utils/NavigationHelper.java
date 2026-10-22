package com.dihanvisgaie.smartpantrymanager.utils;

import android.app.Activity;
import android.content.Intent;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.dihanvisgaie.smartpantrymanager.R;
import com.dihanvisgaie.smartpantrymanager.activities.PantryListActivity;
import com.dihanvisgaie.smartpantrymanager.activities.SettingsActivity;
import com.dihanvisgaie.smartpantrymanager.activities.SuggestedRecipesActivity;

/**
    NavigationHelper - shared bottom-navigation logic for the three main screens
    (Pantry, Recipes, Settings), so the same code isn't copied into each Activity.
 */
public final class NavigationHelper {

    private NavigationHelper() {
        // utility class - no instances
    }

    /**
        Highlights the current tab and switches screens with an explicit Intent when
        another tab is tapped.

        @param activity   the Activity that owns the bottom bar
        @param bottomNav  the BottomNavigationView in that Activity's layout
        @param currentTab menu id of the tab this Activity represents (e.g. R.id.nav_pantry)
     */
    public static void setupBottomNav(Activity activity, BottomNavigationView bottomNav,
                                      int currentTab) {
        // Set the selection BEFORE adding the listener so it doesn't trigger navigation
        bottomNav.setSelectedItemId(currentTab);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == currentTab) {
                return true;
                // already on this screen
            }

            // if/else instead of switch: resource ids are not compile-time constants in AGP 8+
            if (id == R.id.nav_pantry) {
                navigateTo(activity, PantryListActivity.class);
            } else if (id == R.id.nav_recipes) {
                navigateTo(activity, SuggestedRecipesActivity.class);
            } else if (id == R.id.nav_settings) {
                navigateTo(activity, SettingsActivity.class);
            } else {
                return false;
            }
            return true;
        });
    }

    /**
        Opens one of the main screens. FLAG_ACTIVITY_REORDER_TO_FRONT reuses the screen
        if it is already open instead of stacking a new copy every time a tab is tapped.
     */
    public static void navigateTo(Activity from, Class<? extends Activity> target) {
        Intent intent = new Intent(from, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        from.startActivity(intent);
    }
}
