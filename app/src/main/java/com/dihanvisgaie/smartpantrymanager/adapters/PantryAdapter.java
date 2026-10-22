package com.dihanvisgaie.smartpantrymanager.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.dihanvisgaie.smartpantrymanager.R;
import com.dihanvisgaie.smartpantrymanager.models.PantryItem;
import com.dihanvisgaie.smartpantrymanager.utils.DateUtils;
import com.dihanvisgaie.smartpantrymanager.utils.FormatUtils;
import java.util.ArrayList;
import java.util.List;

/**
    PantryAdapter: connects the list of PantryItems to the RecyclerView on the pantry screen.

    How a RecyclerView adapter works:
    onCreateViewHolder(): inflates item_pantry.xml ONCE per visible row (rows are recycled)
    onBindViewHolder():   puts one PantryItem's data into a recycled row as you scroll
    getItemCount():       tells the RecyclerView how many rows there are

    Clicks are passed back to the Activity through OnPantryItemClickListener, so the
    adapter only displays data and never navigates or touches the database itself.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    /** Implemented by PantryListActivity to react to taps and long-presses. */
    public interface OnPantryItemClickListener {
        void onItemClick(PantryItem item);

        void onItemLongClick(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final OnPantryItemClickListener listener;

    public PantryAdapter(List<PantryItem> items, OnPantryItemClickListener listener) {
        this.items.addAll(items);
        this.listener = listener;
    }

    /** Replaces the whole list (called after every database reload) and redraws it. */
    @SuppressLint("NotifyDataSetChanged") // the whole list is reloaded from the DB each time
    public void setItems(List<PantryItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Holds the views of one row so findViewById() runs once per row, not on every scroll. */
    static class PantryViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvItemName;
        private final TextView tvItemQuantity;
        private final TextView tvItemExpiry;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemQuantity = itemView.findViewById(R.id.tvItemQuantity);
            tvItemExpiry = itemView.findViewById(R.id.tvItemExpiry);
        }

        void bind(PantryItem item, OnPantryItemClickListener listener) {
            tvItemName.setText(item.getName());
            tvItemQuantity.setText(FormatUtils.formatAmount(item.getQuantity(), item.getUnit()));

            // Expiry line: hidden if no date, red if already expired
            String expiry = item.getExpiryDate();
            if (expiry == null) {
                tvItemExpiry.setVisibility(View.GONE);
            } else {
                tvItemExpiry.setVisibility(View.VISIBLE);
                boolean expired = DateUtils.isExpired(expiry);
                tvItemExpiry.setText(itemView.getContext().getString(
                        expired ? R.string.expired_on : R.string.expires_on, expiry));
                tvItemExpiry.setTextColor(ContextCompat.getColor(itemView.getContext(),
                        expired ? R.color.expired_red : R.color.text_secondary));
            }

            itemView.setOnClickListener(v -> listener.onItemClick(item));
            itemView.setOnLongClickListener(v -> {
                listener.onItemLongClick(item);
                return true;
                // event handled - don't also fire a normal click
            });
        }
    }
}

