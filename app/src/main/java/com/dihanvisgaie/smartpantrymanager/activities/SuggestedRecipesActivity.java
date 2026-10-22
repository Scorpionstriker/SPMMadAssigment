package com.dihanvisgaie.smartpantrymanager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.dihanvisgaie.smartpantrymanager.R;
import com.dihanvisgaie.smartpantrymanager.adapters.RecipeAdapter;
import com.dihanvisgaie.smartpantrymanager.database.PantryDao;
import com.dihanvisgaie.smartpantrymanager.database.RecipeDao;
import com.dihanvisgaie.smartpantrymanager.logic.RecipeMatcher;
import com.dihanvisgaie.smartpantrymanager.models.PantryItem;
import com.dihanvisgaie.smartpantrymanager.models.Recipe;
import com.dihanvisgaie.smartpantrymanager.utils.NavigationHelper;
import java.util.ArrayList;
import java.util.List;

/**
    SuggestedRecipesActivity - lists ONLY the recipes the user can cook right now.

    Every time the screen is shown, it loads the current pantry and all recipes from
    SQLite and passes them to RecipeMatcher, which applies the strict-matching rule:
    a recipe qualifies only if EVERY ingredient is in the pantry in at least the
    required quantity. This screen only displays the result. The matching logic itself
    lives in RecipeMatcher.
 */
public class SuggestedRecipesActivity extends AppCompatActivity
        implements RecipeAdapter.OnRecipeClickListener {

    private PantryDao pantryDao;
    private RecipeDao recipeDao;
    private RecipeAdapter adapter;

    private RecyclerView rvSuggestions;
    private TextView tvSummary;
    private View layoutEmptyState;
    private TextView tvEmptyMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.title_suggested);

        pantryDao = new PantryDao(this);
        recipeDao = new RecipeDao(this);

        // View binding
        rvSuggestions = findViewById(R.id.rvSuggestions);
        tvSummary = findViewById(R.id.tvSummary);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        Button btnGoToPantry = findViewById(R.id.btnGoToPantry);

        adapter = new RecipeAdapter(new ArrayList<>(), this);
        rvSuggestions.setLayoutManager(new LinearLayoutManager(this));
        rvSuggestions.setAdapter(adapter);

        // Empty-state shortcut: jump straight to the pantry to add ingredients
        btnGoToPantry.setOnClickListener(v ->
                NavigationHelper.navigateTo(this, PantryListActivity.class));

        NavigationHelper.setupBottomNav(this, findViewById(R.id.bottomNav), R.id.nav_recipes);
    }

    /**
        Re-run the matching every time the screen is shown, so that adding or removing
        a single ingredient immediately makes a recipe appear or disappear.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadSuggestions();
    }

    private void loadSuggestions() {
        List<PantryItem> pantry = pantryDao.getAll();
        List<Recipe> allRecipes = recipeDao.getAllWithIngredients();

        // Core business logic: strict matching (see logic/RecipeMatcher.java)
        List<Recipe> makeable = RecipeMatcher.findMakeable(pantry, allRecipes);

        adapter.setRecipes(makeable);

        if (makeable.isEmpty()) {
            // Feedback instead of a blank screen (required by the brief)
            rvSuggestions.setVisibility(View.GONE);
            tvSummary.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
            tvEmptyMessage.setText(pantry.isEmpty()
                    ? R.string.no_matches_empty_pantry
                    : R.string.no_matches);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            rvSuggestions.setVisibility(View.VISIBLE);
            tvSummary.setVisibility(View.VISIBLE);
            tvSummary.setText(getString(R.string.suggestions_summary,
                    makeable.size(), allRecipes.size()));
        }
    }

    /** Tap a recipe -> open its detail screen, passing the recipe id in the Intent. */
    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}

