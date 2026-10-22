package com.dihanvisgaie.smartpantrymanager.database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import com.dihanvisgaie.smartpantrymanager.database.DatabaseSchema.RecipeIngredientTable;
import com.dihanvisgaie.smartpantrymanager.database.DatabaseSchema.RecipeTable;
import com.dihanvisgaie.smartpantrymanager.logic.IngredientManager;

/**
    RecipePreLouder: pre-loads 20 starter recipes the first time the database is created.

    Called only from DatabaseHelper.onCreate(), using the SQLiteDatabase passed in.
    Calling getWritableDatabase() inside onCreate() would cause a recursive-open error.

    UNIT RULE for seed data: keeps strict matching reliable:
    dry / solid foods  -> grams        (g)   e.g. flour, rice, cheese, butter, sugar
    liquids            -> millilitres  (ml)  e.g. milk, oil, cream, soy sauce
    countable items    -> pieces       (pcs) e.g. eggs, onions, slices of bread
    These match how people stock their pantry a 2 kg bag of flour, a 1 L bottle of milk,
    so UnitManager can always compare like with like.

    Seasonings such as salt, pepper and tap water are mentioned in the steps but are
    deliberately NOT listed as required ingredients, since almost every kitchen has them.
 */
public final class RecipePreLouder {

    private RecipePreLouder() {
        // static helper only
    }

    /* Inserts every starter recipe. */
    public static void seed(SQLiteDatabase db) {

        insertRecipe(db, "Cheese Omelette",
                "Whisk the eggs and milk together with a pinch of salt and pepper.\n"
                        + "Melt the butter in a non-stick pan over medium heat.\n"
                        + "Pour in the egg mixture and let it set for about 1 minute.\n"
                        + "Sprinkle the grated cheese over one half.\n"
                        + "Fold the omelette in half and cook for another minute, then serve.",
                ing("Egg", 3, "pcs"),
                ing("Cheese", 50, "g"),
                ing("Butter", 10, "g"),
                ing("Milk", 30, "ml"));

        insertRecipe(db, "Scrambled Eggs on Toast",
                "Toast the bread.\n"
                        + "Whisk the eggs and milk with a pinch of salt.\n"
                        + "Melt half the butter in a pan over low heat and add the eggs.\n"
                        + "Stir gently until just set.\n"
                        + "Butter the toast and spoon the eggs on top.",
                ing("Egg", 2, "pcs"),
                ing("Bread", 2, "pcs"),
                ing("Butter", 15, "g"),
                ing("Milk", 30, "ml"));

        insertRecipe(db, "French Toast",
                "Whisk the eggs, milk and sugar in a shallow bowl.\n"
                        + "Dip each slice of bread into the mixture on both sides.\n"
                        + "Melt the butter in a pan over medium heat.\n"
                        + "Fry the bread for 2-3 minutes per side until golden.\n"
                        + "Serve warm.",
                ing("Bread", 4, "pcs"),
                ing("Egg", 2, "pcs"),
                ing("Milk", 100, "ml"),
                ing("Sugar", 15, "g"),
                ing("Butter", 20, "g"));

        insertRecipe(db, "Pancakes",
                "Mix the flour and sugar in a bowl.\n"
                        + "Whisk in the egg and milk until smooth.\n"
                        + "Melt a little butter in a pan over medium heat.\n"
                        + "Pour in a small ladle of batter and cook until bubbles appear, then flip.\n"
                        + "Repeat with the remaining batter.",
                ing("Flour", 125, "g"),
                ing("Milk", 250, "ml"),
                ing("Egg", 1, "pcs"),
                ing("Sugar", 25, "g"),
                ing("Butter", 30, "g"));

        insertRecipe(db, "Banana Oat Smoothie",
                "Peel and slice the banana.\n"
                        + "Add the banana, oats, milk and honey to a blender.\n"
                        + "Blend until smooth and serve immediately.",
                ing("Banana", 1, "pcs"),
                ing("Oats", 30, "g"),
                ing("Milk", 250, "ml"),
                ing("Honey", 15, "g"));

        insertRecipe(db, "Oats Porridge",
                "Bring the milk to a gentle simmer in a pot.\n"
                        + "Stir in the oats.\n"
                        + "Cook for 4-5 minutes, stirring often, until thick and creamy.\n"
                        + "Sweeten with the sugar and serve.",
                ing("Oats", 50, "g"),
                ing("Milk", 250, "ml"),
                ing("Sugar", 10, "g"));

        insertRecipe(db, "Tomato Pasta",
                "Cook the pasta in salted boiling water until al dente, then drain.\n"
                        + "Heat the oil in a pan and fry the chopped onion until soft.\n"
                        + "Add the crushed garlic and fry for 1 minute.\n"
                        + "Add the chopped tomatoes and simmer for 10 minutes.\n"
                        + "Season, toss the sauce with the pasta and serve.",
                ing("Pasta", 200, "g"),
                ing("Tomato", 3, "pcs"),
                ing("Onion", 1, "pcs"),
                ing("Garlic", 2, "pcs"),
                ing("Cooking Oil", 30, "ml"));

        insertRecipe(db, "Macaroni and Cheese",
                "Cook the pasta until al dente and drain.\n"
                        + "Melt the butter in a pot and stir in the flour for 1 minute.\n"
                        + "Slowly whisk in the milk and cook until the sauce thickens.\n"
                        + "Stir in the grated cheese until melted.\n"
                        + "Mix the sauce with the pasta and serve.",
                ing("Pasta", 200, "g"),
                ing("Cheese", 100, "g"),
                ing("Milk", 250, "ml"),
                ing("Butter", 30, "g"),
                ing("Flour", 20, "g"));

        insertRecipe(db, "Spaghetti Bolognese",
                "Heat the oil and fry the chopped onion and garlic until soft.\n"
                        + "Add the mince and brown it, breaking it up with a spoon.\n"
                        + "Add the chopped tomatoes and simmer for 20 minutes.\n"
                        + "Meanwhile cook the pasta in salted boiling water and drain.\n"
                        + "Season the sauce and serve it over the pasta.",
                ing("Pasta", 250, "g"),
                ing("Beef Mince", 500, "g"),
                ing("Onion", 1, "pcs"),
                ing("Tomato", 4, "pcs"),
                ing("Garlic", 2, "pcs"),
                ing("Cooking Oil", 30, "ml"));

        insertRecipe(db, "Chicken Stir-Fry with Rice",
                "Cook the rice according to the packet instructions.\n"
                        + "Slice the chicken, carrot, pepper and onion into thin strips.\n"
                        + "Heat the oil in a large pan and fry the chicken until cooked through.\n"
                        + "Add the vegetables and stir-fry for 3-4 minutes.\n"
                        + "Add the soy sauce, toss well and serve over the rice.",
                ing("Chicken Breast", 300, "g"),
                ing("Rice", 200, "g"),
                ing("Carrot", 1, "pcs"),
                ing("Green Pepper", 1, "pcs"),
                ing("Onion", 1, "pcs"),
                ing("Soy Sauce", 30, "ml"),
                ing("Cooking Oil", 30, "ml"));

        insertRecipe(db, "Egg Fried Rice",
                "Cook the rice and let it cool (leftover rice works best).\n"
                        + "Heat the oil and fry the chopped onion and grated carrot.\n"
                        + "Push the vegetables aside, scramble the eggs in the pan.\n"
                        + "Add the rice and soy sauce and stir-fry for 3 minutes.\n"
                        + "Serve hot.",
                ing("Rice", 200, "g"),
                ing("Egg", 2, "pcs"),
                ing("Carrot", 1, "pcs"),
                ing("Onion", 1, "pcs"),
                ing("Soy Sauce", 30, "ml"),
                ing("Cooking Oil", 15, "ml"));

        insertRecipe(db, "Chakalaka",
                "Heat the oil and fry the chopped onion until soft.\n"
                        + "Add the curry powder and fry for 1 minute.\n"
                        + "Add the grated carrots and chopped green pepper and cook for 5 minutes.\n"
                        + "Add the chopped tomatoes and simmer for 5 minutes.\n"
                        + "Stir in the baked beans, heat through and season to taste.",
                ing("Onion", 1, "pcs"),
                ing("Carrot", 2, "pcs"),
                ing("Green Pepper", 1, "pcs"),
                ing("Tomato", 2, "pcs"),
                ing("Baked Beans", 410, "g"),
                ing("Curry Powder", 10, "g"),
                ing("Cooking Oil", 30, "ml"));

        insertRecipe(db, "Pap (Stiff Maize Porridge)",
                "Bring about 750 ml of salted water to the boil.\n"
                        + "Slowly pour in the maize meal while stirring to avoid lumps.\n"
                        + "Cover and simmer on low heat for 20-25 minutes, stirring now and then.\n"
                        + "Stir in the butter and serve with chakalaka or a stew.",
                ing("Maize Meal", 250, "g"),
                ing("Butter", 10, "g"));

        insertRecipe(db, "Tuna Mayo Sandwich",
                "Drain the tuna and flake it into a bowl.\n"
                        + "Mix in the mayonnaise and season with pepper.\n"
                        + "Spread the mixture between the slices of bread and serve.",
                ing("Bread", 2, "pcs"),
                ing("Tuna", 170, "g"),
                ing("Mayonnaise", 30, "g"));

        insertRecipe(db, "Cheese and Tomato Toastie",
                "Butter the outside of each slice of bread.\n"
                        + "Place cheese and sliced tomato between the unbuttered sides.\n"
                        + "Toast in a pan or sandwich maker for 3-4 minutes per side until golden.",
                ing("Bread", 2, "pcs"),
                ing("Cheese", 60, "g"),
                ing("Tomato", 1, "pcs"),
                ing("Butter", 10, "g"));

        insertRecipe(db, "Vegetable Soup",
                "Peel and dice the potatoes, carrots and onion.\n"
                        + "Place them in a pot with the chopped tomatoes.\n"
                        + "Dissolve the stock cube in 1 litre of boiling water and pour it over.\n"
                        + "Simmer for 25 minutes until the vegetables are soft.\n"
                        + "Blend if you like a smooth soup, season and serve.",
                ing("Potato", 2, "pcs"),
                ing("Carrot", 2, "pcs"),
                ing("Onion", 1, "pcs"),
                ing("Tomato", 2, "pcs"),
                ing("Stock Cube", 1, "pcs"));

        insertRecipe(db, "Mashed Potatoes",
                "Peel and quarter the potatoes.\n"
                        + "Boil in salted water for 15-20 minutes until soft, then drain.\n"
                        + "Mash with the butter and warm milk until smooth.\n"
                        + "Season to taste and serve.",
                ing("Potato", 4, "pcs"),
                ing("Butter", 30, "g"),
                ing("Milk", 100, "ml"));

        insertRecipe(db, "Creamy Chicken and Mushroom Pasta",
                "Cook the pasta until al dente and drain.\n"
                        + "Melt the butter and fry the diced chicken until cooked through.\n"
                        + "Add the sliced mushrooms and garlic and fry for 4 minutes.\n"
                        + "Pour in the cream and simmer for 3 minutes until slightly thickened.\n"
                        + "Season, stir in the pasta and serve.",
                ing("Pasta", 200, "g"),
                ing("Chicken Breast", 250, "g"),
                ing("Mushroom", 200, "g"),
                ing("Cream", 250, "ml"),
                ing("Garlic", 2, "pcs"),
                ing("Butter", 20, "g"));

        insertRecipe(db, "Peanut Butter Banana Toast",
                "Toast the bread.\n"
                        + "Spread the peanut butter over the toast.\n"
                        + "Top with sliced banana and drizzle with honey.",
                ing("Bread", 2, "pcs"),
                ing("Peanut Butter", 30, "g"),
                ing("Banana", 1, "pcs"),
                ing("Honey", 10, "g"));

        insertRecipe(db, "Savoury Mince and Rice",
                "Cook the rice according to the packet instructions.\n"
                        + "Heat the oil and fry the chopped onion until soft.\n"
                        + "Add the mince and curry powder and brown the mince.\n"
                        + "Add the grated carrot and chopped tomatoes and simmer for 15 minutes.\n"
                        + "Season and serve the mince over the rice.",
                ing("Beef Mince", 500, "g"),
                ing("Rice", 200, "g"),
                ing("Onion", 1, "pcs"),
                ing("Tomato", 2, "pcs"),
                ing("Carrot", 1, "pcs"),
                ing("Curry Powder", 5, "g"),
                ing("Cooking Oil", 15, "ml"));
    }

    // Helpers

    /* Inserts one recipe row, then one row per ingredient linked by the new recipe id. */
    private static void insertRecipe(SQLiteDatabase db, String name, String steps,
                                     SeedIngredient... ingredients) {
        ContentValues recipeValues = new ContentValues();
        recipeValues.put(RecipeTable.COL_NAME, name);
        recipeValues.put(RecipeTable.COL_STEPS, steps);
        long recipeId = db.insert(RecipeTable.TABLE_NAME, null, recipeValues);

        if (recipeId == -1) {
            return;
            // e.g. duplicate name: skip its ingredients
        }

        for (SeedIngredient ingredient : ingredients) {
            ContentValues values = new ContentValues();
            values.put(RecipeIngredientTable.COL_RECIPE_ID, recipeId);
            values.put(RecipeIngredientTable.COL_NAME, ingredient.name);
            values.put(RecipeIngredientTable.COL_NORMALIZED_NAME,
                    IngredientManager.normalize(ingredient.name));
            values.put(RecipeIngredientTable.COL_QUANTITY, ingredient.quantity);
            values.put(RecipeIngredientTable.COL_UNIT, ingredient.unit);
            db.insert(RecipeIngredientTable.TABLE_NAME, null, values);
        }
    }

    /* Short factory so each recipe above reads like a real ingredient list. */
    private static SeedIngredient ing(String name, double quantity, String unit) {
        return new SeedIngredient(name, quantity, unit);
    }

    /* Simple holder for one ingredient line in the seed data. */
    private static final class SeedIngredient {
        final String name;
        final double quantity;
        final String unit;

        SeedIngredient(String name, double quantity, String unit) {
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
        }
    }
}

