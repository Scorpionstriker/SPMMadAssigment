package com.dihanvisgaie.smartpantrymanager.logic;

import com.dihanvisgaie.smartpantrymanager.models.PantryItem;
import com.dihanvisgaie.smartpantrymanager.models.Recipe;
import com.dihanvisgaie.smartpantrymanager.models.RecipeIngredient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
    RecipeMatcher: THE STRICT-MATCHING RULE.

    A recipe is suggested ONLY IF every single ingredient it requires is present in the
    pantry in AT LEAST the required quantity. One missing ingredient, or too little of one,
    and the recipe is excluded - there are no partial matches.

    How it works:
    1. buildStock(): turn the pantry into a lookup table
        key   = normalized name + measurement category   e.g. egg|COUNT, flour|MASS
        value = total amount in the base unit            e.g. 6.0,         2000.0 grams
        Duplicate pantry rows Eggs 6 and eggs 2 are added together.
    2. canMake(): for each recipe ingredient, look up the same key. If it's missing,
        or the pantry has less than needed, the recipe fails immediately.

    Robustness comes from IngredientManager Tomatoes = tomato and
    UnitManager 1 kg = 1000 g, so trivial differences don't break matching.

    This is plain Java with no Android code, so it can be unit tested on its own
    which allows us to test the code in the test unit named RecipeMatcherTest.
 */
public final class RecipeMatcher {

    /* Small tolerance for floating-point rounding e.g. 0.1 + 0.2 != 0.3 exactly. */
    private static final double EPSILON = 1e-6;

    private RecipeMatcher() {
        // static helper only
    }

    /*
        Returns only the recipes that can be made right now with what is in the pantry.
        The original order of the recipe list is kept.
     */
    public static List<Recipe> findMakeable(List<PantryItem> pantry, List<Recipe> recipes) {
        Map<String, Double> stock = buildStock(pantry);

        List<Recipe> makeable = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (canMake(recipe, stock)) {
                makeable.add(recipe);
            }
        }
        return makeable;
    }

    /*
        Strict check for ONE recipe: true only if EVERY ingredient is available
        in at least the required amount.
     */
    public static boolean canMake(Recipe recipe, Map<String, Double> stock) {
        List<RecipeIngredient> required = recipe.getIngredients();

        // A recipe with no ingredient data can't be verified, so it is never suggested
        if (required.isEmpty()) {
            return false;
        }

        for (RecipeIngredient ingredient : required) {
            String key = stockKey(ingredient.getNormalizedName(), ingredient.getUnit());
            Double available = stock.get(key);

            if (available == null) {
                return false;
                // ingredient not in the pantry at all
            }

            double needed = UnitManager.toBaseUnit(ingredient.getQuantity(), ingredient.getUnit());
            if (available + EPSILON < needed) {
                return false;
                // in the pantry, but not enough of it
            }
        }
        return true;
        // every single ingredient is covered
    }

    /*
        Builds the pantry lookup table: name|category -> total quantity in base units.
     */
    public static Map<String, Double> buildStock(List<PantryItem> pantry) {
        Map<String, Double> stock = new HashMap<>();
        for (PantryItem item : pantry) {
            String key = stockKey(item.getNormalizedName(), item.getUnit());
            double amount = UnitManager.toBaseUnit(item.getQuantity(), item.getUnit());
            stock.merge(key, amount, Double::sum);
            // adds duplicates together
        }
        return stock;
    }

    /* e.g. tomato, pcs -> tomato|COUNT, flour, kg -> flour|MASS. */
    static String stockKey(String normalizedName, String unit) {
        return normalizedName + "|" + UnitManager.comparisonKey(unit);
    }
}

