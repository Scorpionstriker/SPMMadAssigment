package com.dihanvisgaie.smartpantrymanager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dihanvisgaie.smartpantrymanager.R;
import com.dihanvisgaie.smartpantrymanager.models.RecipeIngredient;
import com.dihanvisgaie.smartpantrymanager.utils.FormatUtils;
import java.util.ArrayList;
import java.util.List;

/**
    IngredientAdapter - read-only list of a recipe's ingredients on the Recipe Detail screen
    (e.g. "Egg ............ 3 pcs").
 */
public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder> {

    private final List<RecipeIngredient> ingredients;

    public IngredientAdapter(List<RecipeIngredient> ingredients) {
        this.ingredients = new ArrayList<>(ingredients);
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        RecipeIngredient ingredient = ingredients.get(position);
        holder.tvIngredientName.setText(ingredient.getName());
        holder.tvIngredientAmount.setText(
                FormatUtils.formatAmount(ingredient.getQuantity(), ingredient.getUnit()));
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    static class IngredientViewHolder extends RecyclerView.ViewHolder {

        final TextView tvIngredientName;
        final TextView tvIngredientAmount;

        IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIngredientName = itemView.findViewById(R.id.tvIngredientName);
            tvIngredientAmount = itemView.findViewById(R.id.tvIngredientAmount);
        }
    }
}

