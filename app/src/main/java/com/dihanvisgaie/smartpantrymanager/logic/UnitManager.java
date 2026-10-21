package com.dihanvisgaie.smartpantrymanager.logic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
  UnitManager - converts quantities to one BASE unit per category so they can be compared.

    MASS   -> grams       (g, kg, mg)
    VOLUME -> millilitres (ml, L, tsp, tbsp, cup)
    COUNT  -> pieces      (pcs, piece, each, whole)

    Example: 1.5 kg -> 1500 g, so it satisfies a recipe that needs 500 g.

    Quantities in DIFFERENT categories are never compared (200 g of flour is not
    "2 cups" of flour - that would need the density of every ingredient).
 */
public final class UnitManager {

    /** The kind of measurement a unit belongs to. */
    public enum Category { MASS, VOLUME, COUNT, UNKNOWN }

    private static final Map<String, Double> GRAMS_PER_UNIT = new HashMap<>();
    private static final Map<String, Double> MILLILITRES_PER_UNIT = new HashMap<>();
    private static final Set<String> COUNT_UNITS = new HashSet<>();

    static {
        // Mass -> grams
        GRAMS_PER_UNIT.put("mg", 0.001);
        GRAMS_PER_UNIT.put("g", 1.0);
        GRAMS_PER_UNIT.put("gram", 1.0);
        GRAMS_PER_UNIT.put("grams", 1.0);
        GRAMS_PER_UNIT.put("kg", 1000.0);
        GRAMS_PER_UNIT.put("kilogram", 1000.0);
        GRAMS_PER_UNIT.put("kilograms", 1000.0);

        // Volume -> millilitres (metric kitchen measures)
        MILLILITRES_PER_UNIT.put("ml", 1.0);
        MILLILITRES_PER_UNIT.put("millilitre", 1.0);
        MILLILITRES_PER_UNIT.put("millilitres", 1.0);
        MILLILITRES_PER_UNIT.put("l", 1000.0);
        MILLILITRES_PER_UNIT.put("litre", 1000.0);
        MILLILITRES_PER_UNIT.put("litres", 1000.0);
        MILLILITRES_PER_UNIT.put("liter", 1000.0);
        MILLILITRES_PER_UNIT.put("tsp", 5.0);
        MILLILITRES_PER_UNIT.put("teaspoon", 5.0);
        MILLILITRES_PER_UNIT.put("tbsp", 15.0);
        MILLILITRES_PER_UNIT.put("tablespoon", 15.0);
        MILLILITRES_PER_UNIT.put("cup", 250.0);
        MILLILITRES_PER_UNIT.put("cups", 250.0);

        // Count -> pieces
        COUNT_UNITS.add("pcs");
        COUNT_UNITS.add("pc");
        COUNT_UNITS.add("piece");
        COUNT_UNITS.add("pieces");
        COUNT_UNITS.add("each");
        COUNT_UNITS.add("whole");
    }

    private UnitManager() {
        // static helper only
    }

    /** Which category a unit belongs to (case-insensitive: "KG", "kg" and " Kg " all work). */
    public static Category categoryOf(String unit) {
        String key = clean(unit);
        if (GRAMS_PER_UNIT.containsKey(key)) {
            return Category.MASS;
        }
        if (MILLILITRES_PER_UNIT.containsKey(key)) {
            return Category.VOLUME;
        }
        if (COUNT_UNITS.contains(key)) {
            return Category.COUNT;
        }
        return Category.UNKNOWN;
    }

    /**
        Converts a quantity to its category's base unit (g, ml or pcs).
        Unknown units are returned unchanged and only ever compared with the exact same unit.
     */
    public static double toBaseUnit(double quantity, String unit) {
        String key = clean(unit);
        switch (categoryOf(unit)) {
            case MASS:
                return quantity * GRAMS_PER_UNIT.get(key);
            case VOLUME:
                return quantity * MILLILITRES_PER_UNIT.get(key);
            default:
                return quantity;
                // COUNT and UNKNOWN are already in their base unit
        }
    }

    /**
        A key describing what kind of amount this is. Two amounts can only be compared
        when their keys are equal: "MASS", "VOLUME", "COUNT", or "UNKNOWN:<unit>".
     */
    public static String comparisonKey(String unit) {
        Category category = categoryOf(unit);
        return category == Category.UNKNOWN
                ? "UNKNOWN:" + clean(unit)
                : category.name();
    }

    private static String clean(String unit) {
        return unit == null ? "" : unit.trim().toLowerCase(Locale.ROOT);
    }
}