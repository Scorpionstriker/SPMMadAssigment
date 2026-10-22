package com.dihanvisgaie.smartpantrymanager.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.dihanvisgaie.smartpantrymanager.models.PantryItem;
import com.dihanvisgaie.smartpantrymanager.models.Recipe;
import com.dihanvisgaie.smartpantrymanager.models.RecipeIngredient;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
    Unit tests for the strict-matching rule .
    Pure Java: runs on yhe computer without an emulator:
    right-click this file in Android Studio > Run RecipeMatcherTest.
 */
public class RecipeMatcherTest {

    // helpers to build test data

    private static PantryItem pantry(String name, double qty, String unit) {
        return new PantryItem(name, qty, unit, null);
    }

    private static Recipe recipe(String name, RecipeIngredient... ingredients) {
        Recipe recipe = new Recipe(1, name, "Step 1");
        for (RecipeIngredient ingredient : ingredients) {
            recipe.addIngredient(ingredient);
        }
        return recipe;
    }

    private static RecipeIngredient needs(String name, double qty, String unit) {
        return new RecipeIngredient(0, 1, name, IngredientManager.normalize(name), qty, unit);
    }

    private static Recipe omelette() {
        return recipe("Cheese Omelette",
                needs("Egg", 3, "pcs"),
                needs("Cheese", 50, "g"),
                needs("Butter", 10, "g"),
                needs("Milk", 30, "ml"));
    }

    private static boolean canMake(Recipe recipe, PantryItem... items) {
        return RecipeMatcher.canMake(recipe, RecipeMatcher.buildStock(Arrays.asList(items)));
    }

    // tests

    @Test
    public void allIngredientsPresent_recipeIsSuggested() {
        assertTrue(canMake(omelette(),
                pantry("Egg", 6, "pcs"),
                pantry("Cheese", 200, "g"),
                pantry("Butter", 250, "g"),
                pantry("Milk", 1, "L")));
    }

    @Test
    public void oneIngredientMissing_recipeIsNotSuggested() {
        // 3 of 4 ingredients: the brief's exact example: must NOT appear
        assertFalse(canMake(omelette(),
                pantry("Egg", 6, "pcs"),
                pantry("Cheese", 200, "g"),
                pantry("Butter", 250, "g")));
    }

    @Test
    public void notEnoughOfOneIngredient_recipeIsNotSuggested() {
        assertFalse(canMake(omelette(),
                pantry("Egg", 2, "pcs"),          // needs 3
                pantry("Cheese", 200, "g"),
                pantry("Butter", 250, "g"),
                pantry("Milk", 1, "L")));
    }

    @Test
    public void exactlyEnough_recipeIsSuggested() {
        assertTrue(canMake(omelette(),
                pantry("Egg", 3, "pcs"),
                pantry("Cheese", 50, "g"),
                pantry("Butter", 10, "g"),
                pantry("Milk", 30, "ml")));
    }

    @Test
    public void pluralsAndCapitalsStillMatch() {
        assertTrue(canMake(recipe("Salad", needs("Tomato", 2, "pcs")),
                pantry("  TOMATOES ", 4, "pcs")));
    }

    @Test
    public void differentUnitsInSameCategoryAreConverted() {
        // 0.5 kg = 500 g and 1 L = 1000 ml
        assertTrue(canMake(recipe("Test", needs("Flour", 500, "g"), needs("Milk", 250, "ml")),
                pantry("Flour", 0.5, "kg"),
                pantry("Milk", 1, "L")));
    }

    @Test
    public void differentCategoriesNeverMatch() {
        // 2 cups volume of flour can't satisfy 200 g mass
        assertFalse(canMake(recipe("Test", needs("Flour", 200, "g")),
                pantry("Flour", 2, "cup")));
    }

    @Test
    public void duplicatePantryRowsAreAddedTogether() {
        assertTrue(canMake(recipe("Test", needs("Egg", 3, "pcs")),
                pantry("Eggs", 2, "pcs"),
                pantry("egg", 1, "pcs")));
    }

    @Test
    public void synonymsMatch() {
        assertTrue(canMake(recipe("Pap", needs("Maize Meal", 250, "g")),
                pantry("Mielie meal", 1, "kg")));
    }

    @Test
    public void emptyPantry_noSuggestions() {
        List<Recipe> result = RecipeMatcher.findMakeable(
                Collections.emptyList(), Collections.singletonList(omelette()));
        assertTrue(result.isEmpty());
    }

    @Test
    public void findMakeable_returnsOnlyFullMatches() {
        Recipe toast = recipe("Toast", needs("Bread", 2, "pcs"), needs("Butter", 10, "g"));
        List<Recipe> all = new ArrayList<>(Arrays.asList(omelette(), toast));

        List<Recipe> result = RecipeMatcher.findMakeable(Arrays.asList(
                pantry("Bread", 10, "pcs"),
                pantry("Butter", 100, "g"),
                pantry("Egg", 6, "pcs")), all);

        assertEquals(1, result.size());
        assertEquals("Toast", result.get(0).getName());
    }
}
