package com.dihanvisgaie.smartpantrymanager.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.dihanvisgaie.smartpantrymanager.R;
import com.dihanvisgaie.smartpantrymanager.adapters.IngredientAdapter;
import com.dihanvisgaie.smartpantrymanager.database.RecipeDao;
import com.dihanvisgaie.smartpantrymanager.models.Recipe;

/**
    RecipeDetailActivity - shows one recipe's full ingredient list and method.

    Started from SuggestedRecipesActivity with an explicit Intent carrying the
    recipe's database id (EXTRA_RECIPE_ID). Only the id is passed, and the full recipe is
    loaded from SQLite here. That keeps the Intent small and the data always current.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    /** Intent extra key for the id of the recipe to show. */
    public static final String EXTRA_RECIPE_ID = "com.yourname.smartpantry.EXTRA_RECIPE_ID";

    private static final long NO_ID = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 1. Read the recipe id passed in the Intent
        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, NO_ID);
        if (recipeId == NO_ID) {
            showErrorAndClose();
            return;
        }

        // 2. Load the recipe (with its ingredients) from the database
        Recipe recipe = new RecipeDao(this).getByIdWithIngredients(recipeId);
        if (recipe == null) {
            showErrorAndClose();
            return;
        }

        // 3. Display it
        displayRecipe(recipe);
    }

    private void displayRecipe(Recipe recipe) {
        setTitle(recipe.getName());

        TextView tvRecipeName = findViewById(R.id.tvRecipeName);
        TextView tvIngredientCount = findViewById(R.id.tvIngredientCount);
        RecyclerView rvIngredients = findViewById(R.id.rvIngredients);
        TextView tvSteps = findViewById(R.id.tvSteps);

        tvRecipeName.setText(recipe.getName());
        tvIngredientCount.setText(getString(R.string.ingredient_count,
                recipe.getIngredients().size()));

        // Ingredient list. Nested scrolling is off because the list sits inside a
        // NestedScrollView together with the method text.
        rvIngredients.setLayoutManager(new LinearLayoutManager(this));
        rvIngredients.setNestedScrollingEnabled(false);
        rvIngredients.setAdapter(new IngredientAdapter(recipe.getIngredients()));

        tvSteps.setText(formatSteps(recipe.getSteps()));
    }

    /**
        Steps are stored in the database as one step per line.
        This turns them into a numbered list: "1. ...", "2. ...".
     */
    private String formatSteps(String rawSteps) {
        if (rawSteps == null || rawSteps.trim().isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        int stepNumber = 1;
        for (String line : rawSteps.split("\n")) {
            String step = line.trim();
            if (step.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append("\n\n");
            }
            builder.append(stepNumber++).append(". ").append(step);
        }
        return builder.toString();
    }

    /** Defensive: bad or missing id -> tell the user and go back instead of crashing. */
    private void showErrorAndClose() {
        Toast.makeText(this, R.string.recipe_not_found, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

