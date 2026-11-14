package com.unitconverter.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.unitconverter.app.data.database.ConversionHistoryEntity;
import com.unitconverter.app.presentation.viewmodel.MainViewModel;
import com.unitconverter.app.domain.model.UnitCategory;
import com.unitconverter.app.util.NumberFormatter;

import java.util.List;

/**
 * Adapter for displaying full history items with detailed information.
 * Shows conversion details, category badge, timestamp, favorite toggle, and action buttons.
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
        ConversionHistoryEntity item = historyList.get(position);
        holder.bind(item);
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
        private final Chip categoryChip;
        private final TextView timestampText;
        private final ImageView favoriteIcon;
        private final TextView inputValueText;
        private final TextView fromUnitText;
        private final TextView resultValueText;
        private final TextView toUnitText;
        private final MaterialButton reuseButton;
        private final MaterialButton deleteButton;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            categoryChip = itemView.findViewById(R.id.categoryChip);
            timestampText = itemView.findViewById(R.id.timestampText);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
            inputValueText = itemView.findViewById(R.id.inputValueText);
            fromUnitText = itemView.findViewById(R.id.fromUnitText);
            resultValueText = itemView.findViewById(R.id.resultValueText);
            toUnitText = itemView.findViewById(R.id.toUnitText);
            reuseButton = itemView.findViewById(R.id.reuseButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        public void bind(ConversionHistoryEntity item) {
            // Category badge
            categoryChip.setText(UnitCategory.displayNameOf(item.getCategory()));

            // Timestamp - relative time
            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                item.getTimestamp(),
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            );
            timestampText.setText(relativeTime);

            // Conversion values
            inputValueText.setText(NumberFormatter.format(item.getInputValue()));
            fromUnitText.setText(item.getFromUnit());
            resultValueText.setText(NumberFormatter.format(item.getResultValue()));
            toUnitText.setText(item.getToUnit());

            // Favorite icon
            updateFavoriteIcon(item.isFavorite());
            favoriteIcon.setOnClickListener(v -> {
                viewModel.toggleFavorite(item);
                updateFavoriteIcon(!item.isFavorite());
            });

            // Reuse button - navigate to ConversionActivity with pre-filled data
            reuseButton.setOnClickListener(v -> {
                Intent intent = new Intent(context, ConversionActivity.class);
                intent.putExtra("CATEGORY", item.getCategory());
                intent.putExtra("FROM_UNIT", item.getFromUnit());
                intent.putExtra("TO_UNIT", item.getToUnit());
                intent.putExtra("INPUT_VALUE", item.getInputValue());
                context.startActivity(intent);
            });

            // Delete button
            deleteButton.setOnClickListener(v -> {
                viewModel.deleteHistoryItem(item);
            });

            // Card click - also navigate to conversion
            cardView.setOnClickListener(v -> reuseButton.performClick());
        }

        private void updateFavoriteIcon(boolean isFavorite) {
            if (isFavorite) {
                favoriteIcon.setImageResource(R.drawable.ic_star_filled);
                favoriteIcon.setColorFilter(
                    ContextCompat.getColor(context, R.color.mark),
                    PorterDuff.Mode.SRC_IN
                );
            } else {
                favoriteIcon.setImageResource(R.drawable.ic_star_border);
                favoriteIcon.setColorFilter(
                    ContextCompat.getColor(context, R.color.text_faint),
                    PorterDuff.Mode.SRC_IN
                );
            }
        }
    }
}
