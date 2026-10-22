package com.dihanvisgaie.smartpantrymanager.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dihanvisgaie.smartpantrymanager.R;
import com.dihanvisgaie.smartpantrymanager.models.Recipe;
import com.dihanvisgaie.smartpantrymanager.models.RecipeIngredient;
import java.util.ArrayList;
import java.util.List;

/**
    RecipeAdapter - shows the strictly-matched recipes on the Suggested Recipes screen.
    Each card shows the recipe name, how many ingredients it uses, and a preview of them.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    /** Implemented by SuggestedRecipesActivity to open the detail screen. */
    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private final List<Recipe> recipes = new ArrayList<>();
    private final OnRecipeClickListener listener;

    public RecipeAdapter(List<Recipe> recipes, OnRecipeClickListener listener) {
        this.recipes.addAll(recipes);
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    // the whole list is recalculated each time
    public void setRecipes(List<Recipe> newRecipes) {
        recipes.clear();
        recipes.addAll(newRecipes);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        holder.bind(recipes.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvRecipeName;
        private final TextView tvRecipeIngredientCount;
        private final TextView tvRecipeIngredients;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRecipeName = itemView.findViewById(R.id.tvRecipeName);
            tvRecipeIngredientCount = itemView.findViewById(R.id.tvRecipeIngredientCount);
            tvRecipeIngredients = itemView.findViewById(R.id.tvRecipeIngredients);
        }

        void bind(Recipe recipe, OnRecipeClickListener listener) {
            tvRecipeName.setText(recipe.getName());
            tvRecipeIngredientCount.setText(itemView.getContext().getString(
                    R.string.ingredient_count, recipe.getIngredients().size()));
            tvRecipeIngredients.setText(joinIngredientNames(recipe.getIngredients()));

            itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
        }

        /** "Egg, Cheese, Butter, Milk" - the layout cuts it off with "..." if too long. */
        private String joinIngredientNames(List<RecipeIngredient> ingredients) {
            StringBuilder builder = new StringBuilder();
            for (RecipeIngredient ingredient : ingredients) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }
                builder.append(ingredient.getName());
            }
            return builder.toString();
        }
    }
}
