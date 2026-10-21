package com.dihanvisgaie.smartpantrymanager.models;

/**
    RecipeIngredient: one row of the recipe_ingredients table:
    an ingredient a recipe needs, and how much of it.
 */
public class RecipeIngredient {

    private final long id;
    private final long recipeId;
    private final String name;
    private final String normalizedName;
    private final double quantity;
    private final String unit;

    public RecipeIngredient(long id, long recipeId, String name, String normalizedName,
                            double quantity, String unit) {
        this.id = id;
        this.recipeId = recipeId;
        this.name = name;
        this.normalizedName = normalizedName;
        this.quantity = quantity;
        this.unit = unit;
    }

    public long getId() {
        return id;
    }

    public long getRecipeId() {
        return recipeId;
    }

    public String getName() {
        return name;
    }

    public String getNormalizedName() {
        return normalizedName;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }
}

