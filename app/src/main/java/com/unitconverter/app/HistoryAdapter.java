package com.unitconverter.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.unitconverter.app.data.database.ConversionHistoryEntity;
import com.unitconverter.app.domain.model.UnitCategory;
import com.unitconverter.app.presentation.viewmodel.MainViewModel;
import com.unitconverter.app.presentation.widget.TicketDrawable;
import com.unitconverter.app.settings.ThemeManager;
import com.unitconverter.app.util.NumberFormatter;
import com.unitconverter.app.util.UnitText;

import java.util.List;

/**
 * History entries as railway tickets. Tap a ticket to reuse the conversion,
 * tap the star to keep it, tap the bin (or swipe) to delete.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<ConversionHistoryEntity> historyList;
    private final MainViewModel viewModel;
    private final Context context;

    public HistoryAdapter(List<ConversionHistoryEntity> historyList, MainViewModel viewModel, Context context) {
        this.historyList = historyList;
        this.viewModel = viewModel;
        this.context = context;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_full, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.bind(historyList.get(position));
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public void setHistoryList(List<ConversionHistoryEntity> historyList) {
        this.historyList = historyList;
        notifyDataSetChanged();
    }

    public ConversionHistoryEntity getItem(int position) {
        return historyList.get(position);
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView cardView;
        private final View ticketBody;
        private final TextView categoryText;
        private final TextView timestampText;
        private final ImageView favoriteIcon;
        private final TextView inputValueText;
        private final TextView fromUnitText;
        private final TextView resultValueText;
        private final TextView toUnitText;
        private final View deleteButton;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            ticketBody = itemView.findViewById(R.id.ticketBody);
            categoryText = itemView.findViewById(R.id.categoryChip);
            timestampText = itemView.findViewById(R.id.timestampText);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
            inputValueText = itemView.findViewById(R.id.inputValueText);
            fromUnitText = itemView.findViewById(R.id.fromUnitText);
            resultValueText = itemView.findViewById(R.id.resultValueText);
            toUnitText = itemView.findViewById(R.id.toUnitText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        void bind(ConversionHistoryEntity item) {
            Context ctx = itemView.getContext();
            float dp = ctx.getResources().getDisplayMetrics().density;
            ticketBody.setBackground(new TicketDrawable(
                    ThemeManager.color(ctx, R.attr.scSurface),
                    ThemeManager.color(ctx, R.attr.scLine), dp, 0.76f));

            categoryText.setText(UnitCategory.displayNameOf(item.getCategory()));
            try {
                UnitCategory cat = UnitCategory.valueOf(item.getCategory());
                Drawable dot = ContextCompat.getDrawable(ctx, R.drawable.bg_dot);
                if (dot != null) {
                    dot = DrawableCompat.wrap(dot.mutate());
                    DrawableCompat.setTint(dot, ThemeManager.hue(ctx, cat));
                    categoryText.setCompoundDrawablesRelativeWithIntrinsicBounds(dot, null, null, null);
                }
            } catch (IllegalArgumentException ignored) {
                categoryText.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
            }

            timestampText.setText(DateUtils.getRelativeTimeSpanString(
                    item.getTimestamp(), System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE));

            inputValueText.setText(NumberFormatter.formatDisplay(item.getInputValue()));
            fromUnitText.setText(UnitText.nameOf(item.getFromUnit()));
            resultValueText.setText(NumberFormatter.formatDisplay(item.getResultValue()));
            toUnitText.setText(UnitText.nameOf(item.getToUnit()));

            updateFavoriteIcon(item.isFavorite());
            favoriteIcon.setOnClickListener(v -> {
                viewModel.toggleFavorite(item);
                updateFavoriteIcon(!item.isFavorite());
            });

            deleteButton.setOnClickListener(v -> viewModel.deleteHistoryItem(item));

            cardView.setContentDescription(item.getDisplayText());
            cardView.setOnClickListener(v -> reuse(item));
        }

        /** Opens the conversion screen pre-filled with this entry. */
        private void reuse(ConversionHistoryEntity item) {
            Intent intent = new Intent(context, ConversionActivity.class);
            intent.putExtra("CATEGORY", item.getCategory());
            intent.putExtra("FROM_UNIT", item.getFromUnit());
            intent.putExtra("TO_UNIT", item.getToUnit());
            intent.putExtra("INPUT_VALUE", item.getInputValue());
            context.startActivity(intent);
        }

        private void updateFavoriteIcon(boolean isFavorite) {
            Context ctx = itemView.getContext();
            favoriteIcon.setImageResource(isFavorite ? R.drawable.ic_star_filled : R.drawable.ic_star_border);
            favoriteIcon.setColorFilter(ThemeManager.color(ctx,
                    isFavorite ? R.attr.scAccent : R.attr.scInkFaint), PorterDuff.Mode.SRC_IN);
        }
    }
}
