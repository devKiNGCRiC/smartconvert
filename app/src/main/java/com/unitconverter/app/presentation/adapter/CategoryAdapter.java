package com.unitconverter.app.presentation.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.unitconverter.app.R;
import com.unitconverter.app.data.provider.UnitProvider;
import com.unitconverter.app.domain.model.Unit;
import com.unitconverter.app.domain.model.UnitCategory;
import com.unitconverter.app.settings.ThemeManager;

/**
 * Bento grid of categories on the home screen.
 *
 * Tiles take the category hue and the mood's tile shape (arch, stamp or
 * plaque). {@link #SPANS} sets the bento rhythm on a 6-column grid; the
 * heights follow the span so big tiles feel big. On first show the tiles
 * rise in one staggered wave.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    /** Display order, chosen so long names land on wide tiles. */
    public static final UnitCategory[] ORDER = {
            UnitCategory.LENGTH, UnitCategory.WEIGHT,
            UnitCategory.VOLUME, UnitCategory.AREA, UnitCategory.TIME,
            UnitCategory.TEMPERATURE, UnitCategory.SPEED,
            UnitCategory.DIGITAL_STORAGE
    };

    /** Column spans out of 6, matching {@link #ORDER}. */
    public static final int[] SPANS = {3, 3, 2, 2, 2, 3, 3, 6};

    private final UnitCategory[] categories;
    private final OnCategoryClickListener clickListener;
    private boolean showIndianUnits;
    private final boolean playEntrance;
    private int lastAnimated = -1;

    public interface OnCategoryClickListener {
        void onCategoryClick(UnitCategory category, View tile);
    }

    public CategoryAdapter(UnitCategory[] categories, OnCategoryClickListener clickListener,
                           boolean showIndianUnits, boolean playEntrance) {
        this.categories = categories;
        this.clickListener = clickListener;
        this.showIndianUnits = showIndianUnits;
        this.playEntrance = playEntrance;
    }

    public void setShowIndianUnits(boolean show) {
        showIndianUnits = show;
        notifyDataSetChanged();
    }

    public static int spanFor(int position) {
        return position >= 0 && position < SPANS.length ? SPANS[position] : 3;
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
        holder.bind(categories[position], spanFor(position), showIndianUnits, clickListener);
        if (playEntrance && position > lastAnimated) {
            lastAnimated = position;
            holder.rise(position);
        }
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.length : 0;
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView card;
        private final TextView symbol;
        private final TextView name;
        private final TextView count;
        private final com.unitconverter.app.presentation.widget.PatternView pattern;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.categoryCard);
            symbol = itemView.findViewById(R.id.categoryIcon);
            name = itemView.findViewById(R.id.categoryName);
            count = itemView.findViewById(R.id.categoryUnitCount);
            pattern = itemView.findViewById(R.id.tilePattern);
        }

        void bind(UnitCategory category, int span, boolean showIndian, OnCategoryClickListener listener) {
            Context c = itemView.getContext();
            float dp = c.getResources().getDisplayMetrics().density;
            int hue = ThemeManager.hue(c, category);

            ViewGroup.LayoutParams lp = card.getLayoutParams();
            lp.height = Math.round((span == 6 ? 118 : span == 3 ? 156 : 138) * dp);
            card.setLayoutParams(lp);

            card.setCardBackgroundColor(hue);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // Coloured "clay" shadow in the tile's own hue
                card.setOutlineSpotShadowColor(hue);
                card.setOutlineAmbientShadowColor(hue);
            }
            card.setRippleColor(ColorStateList.valueOf(0x40FFFFFF));
            // Pattern and clay sheen run edge to edge, clipped to the mood's tile shape
            pattern.setClipShape(card.getShapeAppearanceModel());
            pattern.setSheen(true);

            symbol.setText(category.getSymbol());
            symbol.setTextSize(span == 2 ? 30 : 38);
            name.setText(category.getDisplayName());

            int total = 0, indian = 0;
            for (Unit u : UnitProvider.getInstance().getUnitsForCategory(category)) {
                if (u.getSystem() == Unit.MeasureSystem.INDIAN) {
                    if (!showIndian) continue;
                    indian++;
                }
                total++;
            }
            count.setText(indian > 0 && span != 2
                    ? c.getString(R.string.units_count_indian, total, indian)
                    : c.getString(R.string.units_count, total));

            symbol.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            card.setContentDescription(category.getDisplayName() + ", " + count.getText());
            card.setTransitionName("tile_" + category.name());
            card.setOnClickListener(v -> {
                if (listener != null) listener.onCategoryClick(category, card);
            });
        }

        /** One orchestrated entrance: tiles rise and settle in sequence. */
        void rise(int position) {
            float dp = itemView.getResources().getDisplayMetrics().density;
            itemView.setAlpha(0f);
            itemView.setTranslationY(36 * dp);
            itemView.setScaleX(0.96f);
            itemView.setScaleY(0.96f);
            itemView.animate()
                    .alpha(1f).translationY(0f).scaleX(1f).scaleY(1f)
                    .setStartDelay(120 + position * 55L)
                    .setDuration(460)
                    .setInterpolator(new DecelerateInterpolator(2f))
                    .start();
        }
    }
}
