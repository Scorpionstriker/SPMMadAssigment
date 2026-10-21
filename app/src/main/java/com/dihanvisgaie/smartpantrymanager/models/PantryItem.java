package com.dihanvisgaie.smartpantrymanager.models;

import com.dihanvisgaie.smartpantrymanager.logic.IngredientManager;

/**
    PantryItem: one row of the pantry_items table an ingredient the user has at home.
 */
public class PantryItem {

    private long id;
    private String name;
    private String normalizedName;
    private double quantity;
    private String unit;
    private String expiryDate;
    // "yyyy-MM-dd", or null if none

    /** Used by the Add/Edit form: no id yet SQLite assigns it on insert. */
    public PantryItem(String name, double quantity, String unit, String expiryDate) {
        this(0, name, null, quantity, unit, expiryDate);
    }

    /** Used by PantryDao when reading a full row from the database. */
    public PantryItem(long id, String name, String normalizedName, double quantity,
                      String unit, String expiryDate) {
        this.id = id;
        this.name = name;
        this.normalizedName = normalizedName;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.normalizedName = null;
        // recalculated on next get
    }

    /** Matching key, e.g. "tomato". Calculated from the name if not loaded from the DB. */
    public String getNormalizedName() {
        if (normalizedName == null) {
            normalizedName = IngredientManager.normalize(name);
        }
        return normalizedName;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }
}
