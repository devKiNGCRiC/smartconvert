package com.unitconverter.app.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.unitconverter.app.R;
import com.unitconverter.app.domain.model.UnitCategory;

/**
 * RecyclerView adapter for the category grid on the home screen.
 *
 * Each tile shows a representative unit symbol ("m", "kg", "°C"…) in large
 * condensed type, with the category name underneath. Styling lives in
 * item_category.xml and the theme; this class only binds data and clicks.
 *
 * TEACHING NOTE: ViewHolder pattern
 * - findViewById runs once per ViewHolder, not once per bind
 * - Views are recycled as the user scrolls
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final UnitCategory[] categories;
    private final OnCategoryClickListener clickListener;

    /** Callback for category taps. */
    public interface OnCategoryClickListener {
        void onCategoryClick(UnitCategory category);
    }

    public CategoryAdapter(UnitCategory[] categories, OnCategoryClickListener clickListener) {
        this.categories = categories;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        holder.bind(categories[position], clickListener);
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.length : 0;
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView symbolTextView;
        private final TextView nameTextView;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.categoryCard);
            symbolTextView = itemView.findViewById(R.id.categoryIcon);
            nameTextView = itemView.findViewById(R.id.categoryName);
        }

        void bind(final UnitCategory category, final OnCategoryClickListener listener) {
            symbolTextView.setText(category.getSymbol());
            nameTextView.setText(category.getDisplayName());

            // The symbol is decorative; the name is what a screen reader should announce.
            symbolTextView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            cardView.setContentDescription(category.getDisplayName());

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(category);
                }
            });
        }
    }
}
