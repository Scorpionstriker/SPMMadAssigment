package com.dihanvisgaie.smartpantrymanager.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.dihanvisgaie.smartpantrymanager.database.DatabaseSchema.RecipeIngredientTable;
import com.dihanvisgaie.smartpantrymanager.database.DatabaseSchema.RecipeTable;
import com.dihanvisgaie.smartpantrymanager.models.Recipe;
import com.dihanvisgaie.smartpantrymanager.models.RecipeIngredient;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
    RecipeDao: reads recipes together with their ingredients.

    Recipes are seeded once by RecipePreLouder and are read-only in the app,
    so this DAO only needs read methods.
 */
public class RecipeDao {

    private final DatabaseHelper dbHelper;

    public RecipeDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
        Returns ALL recipes, each with its full ingredient list, sorted by name.

        Uses exactly TWO queries all recipes, then all ingredients and joins them in
        Java using a Map, instead of running one ingredient query per recipe
        which would be 21 queries for 20 recipes - the "N+1 query" problem.
     */
    public List<Recipe> getAllWithIngredients() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // LinkedHashMap keeps the recipes in the alphabetical order of the query
        Map<Long, Recipe> recipesById = new LinkedHashMap<>();

        // Query 1: every recipe
        try (Cursor cursor = db.query(
                RecipeTable.TABLE_NAME, null, null, null, null, null,
                RecipeTable.COL_NAME + " COLLATE NOCASE ASC")) {

            while (cursor.moveToNext()) {
                Recipe recipe = recipeFromCursor(cursor);
                recipesById.put(recipe.getId(), recipe);
            }
        }

        // Query 2: every ingredient, attached to its recipe via recipe_id
        try (Cursor cursor = db.query(
                RecipeIngredientTable.TABLE_NAME, null, null, null, null, null,
                RecipeIngredientTable.COL_RECIPE_ID + " ASC, " + RecipeIngredientTable._ID + " ASC")) {

            while (cursor.moveToNext()) {
                RecipeIngredient ingredient = ingredientFromCursor(cursor);
                Recipe owner = recipesById.get(ingredient.getRecipeId());
                if (owner != null) {
                    owner.addIngredient(ingredient);
                }
            }
        }

        return new ArrayList<>(recipesById.values());
    }

    /** Returns one recipe with its ingredients, or null if the id doesn't exist. */
    public Recipe getByIdWithIngredients(long recipeId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] args = {String.valueOf(recipeId)};
        Recipe recipe;

        try (Cursor cursor = db.query(
                RecipeTable.TABLE_NAME, null,
                RecipeTable._ID + " = ?", args,
                null, null, null)) {

            if (!cursor.moveToFirst()) {
                return null;
            }
            recipe = recipeFromCursor(cursor);
        }

        try (Cursor cursor = db.query(
                RecipeIngredientTable.TABLE_NAME, null,
                RecipeIngredientTable.COL_RECIPE_ID + " = ?", args,
                null, null, RecipeIngredientTable._ID + " ASC")) {

            while (cursor.moveToNext()) {
                recipe.addIngredient(ingredientFromCursor(cursor));
            }
        }
        return recipe;
    }

    /** Total number of recipes in the database useful for checks and the report. */
    public int count() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + RecipeTable.TABLE_NAME, null)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    // Mapping helpers

    private Recipe recipeFromCursor(Cursor cursor) {
        return new Recipe(
                cursor.getLong(cursor.getColumnIndexOrThrow(RecipeTable._ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(RecipeTable.COL_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(RecipeTable.COL_STEPS)));
    }

    private RecipeIngredient ingredientFromCursor(Cursor cursor) {
        return new RecipeIngredient(
                cursor.getLong(cursor.getColumnIndexOrThrow(RecipeIngredientTable._ID)),
                cursor.getLong(cursor.getColumnIndexOrThrow(RecipeIngredientTable.COL_RECIPE_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(RecipeIngredientTable.COL_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(RecipeIngredientTable.COL_NORMALIZED_NAME)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(RecipeIngredientTable.COL_QUANTITY)),
                cursor.getString(cursor.getColumnIndexOrThrow(RecipeIngredientTable.COL_UNIT)));
    }
}

