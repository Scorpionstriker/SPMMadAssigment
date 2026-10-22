package com.dihanvisgaie.smartpantrymanager.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.dihanvisgaie.smartpantrymanager.R;
import com.dihanvisgaie.smartpantrymanager.database.PantryDao;
import com.dihanvisgaie.smartpantrymanager.models.PantryItem;
import com.dihanvisgaie.smartpantrymanager.utils.DateUtils;
import com.dihanvisgaie.smartpantrymanager.utils.FormatUtils;
import com.dihanvisgaie.smartpantrymanager.utils.PreferencesManager;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
    AddEditIngredientActivity - one form used for both creating and updating a pantry item.

    The mode is decided by the Intent that started this screen:
    no EXTRA_ITEM_ID    -> ADD mode  (Create)
    EXTRA_ITEM_ID given -> EDIT mode (Update, plus a Delete button)

    All input is validated before anything is written to the database.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    /** Intent extra key for the id of the pantry item to edit. */
    public static final String EXTRA_ITEM_ID = "com.yourname.smartpantry.EXTRA_ITEM_ID";

    private static final long NO_ID = -1L;
    private static final String STATE_EXPIRY_DATE = "state_expiry_date";

    // Validation limits
    private static final int MAX_NAME_LENGTH = 40;
    private static final double MAX_QUANTITY = 100_000;

    private PantryDao pantryDao;
    private PreferencesManager prefs;

    private TextInputLayout tilName;
    private TextInputLayout tilQuantity;
    private TextInputEditText etName;
    private TextInputEditText etQuantity;
    private Spinner spinnerUnit;
    private TextView tvExpiryDate;
    private Button btnClearDate;

    /** Database id of the item being edited, or NO_ID when adding. */
    private long itemId = NO_ID;

    /** Selected expiry date in yyyy-MM-dd format, or null for "no expiry date". */
    private String expiryDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // back arrow
        }

        pantryDao = new PantryDao(this);
        prefs = new PreferencesManager(this);

        // View binding
        tilName = findViewById(R.id.tilName);
        tilQuantity = findViewById(R.id.tilQuantity);
        etName = findViewById(R.id.etName);
        etQuantity = findViewById(R.id.etQuantity);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        tvExpiryDate = findViewById(R.id.tvExpiryDate);
        Button btnPickDate = findViewById(R.id.btnPickDate);
        btnClearDate = findViewById(R.id.btnClearDate);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnDelete = findViewById(R.id.btnDelete);

        setupUnitSpinner();

        // Read the Intent extra to decide between ADD and EDIT mode
        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, NO_ID);

        if (isEditMode()) {
            setTitle(R.string.title_edit_ingredient);
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            setTitle(R.string.title_add_ingredient);
            btnDelete.setVisibility(View.GONE);
        }

        if (savedInstanceState != null) {
            // Screen was recreated (e.g. rotation). EditTexts and the Spinner restore
            // themselves; we only need to restore our own field.
            expiryDate = savedInstanceState.getString(STATE_EXPIRY_DATE);
        } else if (isEditMode()) {
            loadExistingItem();
        } else {
            // New item: pre-select the default unit chosen in Settings
            spinnerUnit.setSelection(indexOfUnit(prefs.getDefaultUnit()));
        }
        updateExpiryLabel();

        // Click listeners
        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnClearDate.setOnClickListener(v -> {
            expiryDate = null;
            updateExpiryLabel();
        });
        btnSave.setOnClickListener(v -> validateAndSave());
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    /** Saves state that Android does not keep automatically, so rotation doesn't lose it. */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_EXPIRY_DATE, expiryDate);
    }

    /** Toolbar back arrow closes this screen and returns to the previous one. */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private boolean isEditMode() {
        return itemId != NO_ID;
    }

    // Setup / loading

    /** Fills the unit dropdown from res/values/arrays.xml (g, kg, ml, L, ...). */
    private void setupUnitSpinner() {
        ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(
                this, R.array.units, android.R.layout.simple_spinner_item);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);
    }

    /** EDIT mode: reads the item from SQLite and pre-fills the form. */
    private void loadExistingItem() {
        PantryItem item = pantryDao.getById(itemId);
        if (item == null) {
            // Defensive: the id passed in the Intent no longer exists
            Toast.makeText(this, R.string.item_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        etName.setText(item.getName());
        etQuantity.setText(FormatUtils.formatQuantity(item.getQuantity()));
        spinnerUnit.setSelection(indexOfUnit(item.getUnit()));
        expiryDate = item.getExpiryDate();
    }

    /** Finds the position of a unit in the units array (case-insensitive); 0 if not found. */
    private int indexOfUnit(String unit) {
        String[] units = getResources().getStringArray(R.array.units);
        for (int i = 0; i < units.length; i++) {
            if (units[i].equalsIgnoreCase(unit)) {
                return i;
            }
        }
        return 0;
    }

    // Expiry date

    /** Opens a DatePickerDialog. Past dates are blocked, which is part of input validation. */
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        // If a date is already chosen, open the picker on that date
        if (expiryDate != null) {
            try {
                Date current = new SimpleDateFormat(DateUtils.DATE_PATTERN, Locale.US).parse(expiryDate);
                if (current != null) {
                    calendar.setTime(current);
                }
            } catch (ParseException ignored) {
                // fall back to today
            }
        }

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    // month is 0-based in DatePicker, so add 1
                    expiryDate = String.format(Locale.US, "%04d-%02d-%02d",
                            year, month + 1, dayOfMonth);
                    updateExpiryLabel();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }

    private void updateExpiryLabel() {
        if (expiryDate == null) {
            tvExpiryDate.setText(R.string.no_expiry);
            btnClearDate.setVisibility(View.GONE);
        } else {
            tvExpiryDate.setText(getString(R.string.expiry_label, expiryDate));
            btnClearDate.setVisibility(View.VISIBLE);
        }
    }

    // Validation + save (Create / Update)

    /**
        Validates every field, showing an error message under each invalid one.
        Only writes to the database when ALL fields are valid.
     */
    private void validateAndSave() {
        String name = etName.getText() == null ? "" : etName.getText().toString().trim();
        String quantityText = etQuantity.getText() == null ? ""
                : etQuantity.getText().toString().trim();
        String unit = spinnerUnit.getSelectedItem().toString();

        // Clear old errors first
        tilName.setError(null);
        tilQuantity.setError(null);
        boolean isValid = true;

        //Name
        if (name.isEmpty()) {
            tilName.setError(getString(R.string.error_name_required));
            isValid = false;
        } else if (name.length() > MAX_NAME_LENGTH) {
            tilName.setError(getString(R.string.error_name_too_long, MAX_NAME_LENGTH));
            isValid = false;
        } else if (!name.matches("[\\p{L} '\\-]+")) {
            // letters, spaces, apostrophes and hyphens only (e.g. "chilli", "self-raising flour")
            tilName.setError(getString(R.string.error_name_invalid));
            isValid = false;
        }

        // Quantity
        double quantity = 0;
        if (quantityText.isEmpty()) {
            tilQuantity.setError(getString(R.string.error_quantity_required));
            isValid = false;
        } else {
            try {
                // South African locale keyboards often type a comma as the decimal separator
                quantity = Double.parseDouble(quantityText.replace(',', '.'));
                if (quantity <= 0) {
                    tilQuantity.setError(getString(R.string.error_quantity_positive));
                    isValid = false;
                } else if (quantity > MAX_QUANTITY) {
                    tilQuantity.setError(getString(R.string.error_quantity_too_large));
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                tilQuantity.setError(getString(R.string.error_quantity_invalid));
                isValid = false;
            }
        }

        if (!isValid) {
            return;
            // stay on the form so the user can fix the errors
        }

        PantryItem item = new PantryItem(name, quantity, unit, expiryDate);
        boolean success;

        if (isEditMode()) {
            item.setId(itemId);
            success = pantryDao.update(item) > 0;
            // rows affected
        } else {
            success = pantryDao.insert(item) != -1;
            // new row id, -1 on failure
        }

        if (success) {
            Toast.makeText(this,
                    isEditMode() ? R.string.item_updated : R.string.item_added,
                    Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
            // PantryListActivity.onResume() will reload the list
        } else {
            Toast.makeText(this, R.string.save_failed, Toast.LENGTH_LONG).show();
        }
    }

    // Delete (EDIT mode only)

    private void confirmDelete() {
        String name = etName.getText() == null ? "" : etName.getText().toString().trim();
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_message, name))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    pantryDao.delete(itemId);
                    Toast.makeText(this, getString(R.string.item_deleted, name),
                            Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}

