package com.dihanvisgaie.smartpantrymanager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.dihanvisgaie.smartpantrymanager.R;
import com.dihanvisgaie.smartpantrymanager.adapters.PantryAdapter;
import com.dihanvisgaie.smartpantrymanager.database.PantryDao;
import com.dihanvisgaie.smartpantrymanager.models.PantryItem;
import com.dihanvisgaie.smartpantrymanager.utils.DateUtils;
import com.dihanvisgaie.smartpantrymanager.utils.NavigationHelper;
import com.dihanvisgaie.smartpantrymanager.utils.PreferencesManager;
import java.util.ArrayList;
import java.util.List;

/**
    PantryListActivity - the app's launcher screen.

    Shows every ingredient in the user's pantry in a RecyclerView (the "Read" in CRUD).
    Tap the + button  -> opens AddEditIngredientActivity in ADD mode   (Create)
    Tap a row         -> opens AddEditIngredientActivity in EDIT mode  (Update)
    Long-press a row  -> confirmation dialog, then delete               (Delete)

    The list is reloaded in onResume(), so it is always up to date when the user
    returns from the add/edit screen or from another tab.
 */
public class PantryListActivity extends AppCompatActivity
        implements PantryAdapter.OnPantryItemClickListener {

    private PantryDao pantryDao;
    private PreferencesManager prefs;
    private PantryAdapter adapter;

    private RecyclerView rvPantry;
    private TextView tvEmptyPantry;
    private TextView tvExpiringBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_list);

        // Toolbar acts as this screen's action bar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.title_pantry);

        pantryDao = new PantryDao(this);
        prefs = new PreferencesManager(this);

        // View binding
        rvPantry = findViewById(R.id.rvPantry);
        tvEmptyPantry = findViewById(R.id.tvEmptyPantry);
        tvExpiringBanner = findViewById(R.id.tvExpiringBanner);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddIngredient);

        // RecyclerView setup: vertical list with dividers, starts empty, filled in onResume()
        adapter = new PantryAdapter(new ArrayList<>(), this);
        rvPantry.setLayoutManager(new LinearLayoutManager(this));
        rvPantry.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rvPantry.setAdapter(adapter);

        // Explicit Intent with NO extra -> AddEditIngredientActivity opens in ADD mode
        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditIngredientActivity.class)));

        NavigationHelper.setupBottomNav(this, findViewById(R.id.bottomNav), R.id.nav_pantry);
    }

    /**
        onResume() runs every time this screen comes back into the foreground
        (first launch, returning from Add/Edit, switching back from another tab),
        so reloading here keeps the list in sync with the database.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadPantry();
    }

    /** Reads all pantry items from SQLite and updates the list, empty state and banner. */
    private void loadPantry() {
        List<PantryItem> items = pantryDao.getAll();
        adapter.setItems(items);

        boolean isEmpty = items.isEmpty();
        rvPantry.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        tvEmptyPantry.setVisibility(isEmpty ? View.VISIBLE : View.GONE);

        updateExpiryBanner(items);
    }

    /**
        If expiring-soon alerts are switched on in Settings, shows a banner counting the
        items that have expired or will expire within the user's chosen number of days.
     */
    private void updateExpiryBanner(List<PantryItem> items) {
        if (!prefs.isExpiryAlertsEnabled()) {
            tvExpiringBanner.setVisibility(View.GONE);
            return;
        }

        int warningDays = prefs.getExpiryWarningDays();
        int count = 0;
        for (PantryItem item : items) {
            Long daysLeft = DateUtils.daysUntil(item.getExpiryDate());
            if (daysLeft != null && daysLeft <= warningDays) {
                count++;
            }
        }

        if (count > 0) {
            tvExpiringBanner.setText(getString(R.string.expiring_banner, count, warningDays));
            tvExpiringBanner.setVisibility(View.VISIBLE);
        } else {
            tvExpiringBanner.setVisibility(View.GONE);
        }
    }

    // PantryAdapter.OnPantryItemClickListener callbacks

    /** Tap -> open the edit screen, passing the item's database id as an Intent extra. */
    @Override
    public void onItemClick(PantryItem item) {
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(intent);
    }

    /** Long-press -> ask for confirmation before deleting. */
    @Override
    public void onItemLongClick(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_message, item.getName()))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    pantryDao.delete(item.getId());
                    Toast.makeText(this, getString(R.string.item_deleted, item.getName()),
                            Toast.LENGTH_SHORT).show();
                    loadPantry();
                    // refresh the list straight away
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}

