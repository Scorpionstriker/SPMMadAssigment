package com.dihanvisgaie.smartpantrymanager.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.dihanvisgaie.smartpantrymanager.R;
import com.dihanvisgaie.smartpantrymanager.database.PantryDao;
import com.dihanvisgaie.smartpantrymanager.utils.NavigationHelper;
import com.dihanvisgaie.smartpantrymanager.utils.PreferencesManager;

/**
    SettingsActivity - user preferences, saved with SharedPreferences.

    Expiring-soon alerts on/off      (used by the banner on PantryListActivity)
    How many days counts as "soon"   (1 / 3 / 7 / 14 days)
    Default unit for new ingredients (pre-selected on the Add Ingredient screen)
    Clear pantry                     (deletes all pantry items; handy for demos)

    Every change is saved immediately, so there is no Save button.
 */
public class SettingsActivity extends AppCompatActivity {

    /** Day values matching R.array.expiry_warning_days_labels, in the same order. */
    private static final int[] WARNING_DAY_OPTIONS = {1, 3, 7, 14};

    private PreferencesManager prefs;
    private Spinner spinnerWarningDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.title_settings);

        prefs = new PreferencesManager(this);

        // CompoundButton works with Switch, SwitchCompat or MaterialSwitch in the XML
        CompoundButton switchExpiryAlerts = findViewById(R.id.switchExpiryAlerts);
        spinnerWarningDays = findViewById(R.id.spinnerWarningDays);
        Spinner spinnerDefaultUnit = findViewById(R.id.spinnerDefaultUnit);
        Button btnClearPantry = findViewById(R.id.btnClearPantry);

        //Expiry alerts switch
        boolean alertsOn = prefs.isExpiryAlertsEnabled();
        switchExpiryAlerts.setChecked(alertsOn);
        spinnerWarningDays.setEnabled(alertsOn);
        switchExpiryAlerts.setOnCheckedChangeListener((button, isChecked) -> {
            prefs.setExpiryAlertsEnabled(isChecked);
            spinnerWarningDays.setEnabled(isChecked);
            // days only matter when alerts are on
        });

        //Warning days spinner
        setupSpinner(spinnerWarningDays, R.array.expiry_warning_days_labels);
        spinnerWarningDays.setSelection(indexOfDays(prefs.getExpiryWarningDays()));
        spinnerWarningDays.setOnItemSelectedListener(new SimpleItemSelectedListener(position ->
                prefs.setExpiryWarningDays(WARNING_DAY_OPTIONS[position])));

        //Default unit spinner
        setupSpinner(spinnerDefaultUnit, R.array.units);
        spinnerDefaultUnit.setSelection(indexOfUnit(prefs.getDefaultUnit()));
        spinnerDefaultUnit.setOnItemSelectedListener(new SimpleItemSelectedListener(position ->
                prefs.setDefaultUnit(spinnerDefaultUnit.getItemAtPosition(position).toString())));

        //Clear pantry
        btnClearPantry.setOnClickListener(v -> confirmClearPantry());

        NavigationHelper.setupBottomNav(this, findViewById(R.id.bottomNav), R.id.nav_settings);
    }

    private void setupSpinner(Spinner spinner, int arrayResId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, arrayResId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private int indexOfDays(int days) {
        for (int i = 0; i < WARNING_DAY_OPTIONS.length; i++) {
            if (WARNING_DAY_OPTIONS[i] == days) {
                return i;
            }
        }
        return 1;
        // default: 3 days
    }

    private int indexOfUnit(String unit) {
        String[] units = getResources().getStringArray(R.array.units);
        for (int i = 0; i < units.length; i++) {
            if (units[i].equalsIgnoreCase(unit)) {
                return i;
            }
        }
        return 0;
    }

    private void confirmClearPantry() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.clear_pantry_title)
                .setMessage(R.string.clear_pantry_message)
                .setPositiveButton(R.string.clear, (dialog, which) -> {
                    new PantryDao(this).deleteAll();
                    Toast.makeText(this, R.string.pantry_cleared, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /**
        Small helper so each Spinner only needs a one-line lambda instead of an
        anonymous class with an empty onNothingSelected() method.
     */
    private static class SimpleItemSelectedListener implements AdapterView.OnItemSelectedListener {

        interface OnSelected {
            void onSelected(int position);
        }

        private final OnSelected callback;

        SimpleItemSelectedListener(OnSelected callback) {
            this.callback = callback;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            callback.onSelected(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // nothing to do
        }
    }
}

