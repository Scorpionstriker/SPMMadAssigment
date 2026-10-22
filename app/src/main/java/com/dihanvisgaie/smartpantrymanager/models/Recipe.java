package com.dihanvisgaie.smartpantrymanager.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
    Recipe: one row of the recipes table plus its list of required ingredients
    loaded from recipe_ingredients by RecipeDao.
 */
public class Recipe {

    private final long id;
    private final String name;
    private final String steps;
    // one step per line
    private final List<RecipeIngredient> ingredients = new ArrayList<>();

    public Recipe(long id, String name, String steps) {
        this.id = id;
        this.name = name;
        this.steps = steps;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSteps() {
        return steps;
    }

    /* Read-only view, so other classes can't accidentally change a recipe's ingredients. */
    public List<RecipeIngredient> getIngredients() {
        return Collections.unmodifiableList(ingredients);
    }

    /* Used by RecipeDao while loading. */
    public void addIngredient(RecipeIngredient ingredient) {
        ingredients.add(ingredient);
    }
}

