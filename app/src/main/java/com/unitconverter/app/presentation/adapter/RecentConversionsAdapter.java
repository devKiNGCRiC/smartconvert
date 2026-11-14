package com.unitconverter.app.presentation.adapter;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.unitconverter.app.R;
import com.unitconverter.app.data.database.ConversionHistoryEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for Recent Conversions
 * Shows last 5 conversions in MainActivity
 */
public class RecentConversionsAdapter extends RecyclerView.Adapter<RecentConversionsAdapter.ViewHolder> {

    private List<ConversionHistoryEntity> conversions = new ArrayList<>();
    private final OnConversionClickListener clickListener;

    public interface OnConversionClickListener {
        void onConversionClick(ConversionHistoryEntity conversion);
        void onFavoriteClick(ConversionHistoryEntity conversion);
    }

    public RecentConversionsAdapter(OnConversionClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setConversions(List<ConversionHistoryEntity> conversions) {
        this.conversions = conversions != null ? conversions : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_conversion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConversionHistoryEntity conversion = conversions.get(position);
        holder.bind(conversion, clickListener);
        // Hairline between rows, none under the last one
        holder.divider.setVisibility(position == getItemCount() - 1 ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return Math.min(conversions.size(), 5); // Show max 5 recent
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView conversionText;
        private final TextView timestampText;
        private final ImageView favoriteIcon;
        final View divider;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            conversionText = itemView.findViewById(R.id.conversionText);
            timestampText = itemView.findViewById(R.id.timestampText);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
            divider = itemView.findViewById(R.id.divider);
        }

        public void bind(ConversionHistoryEntity conversion, OnConversionClickListener listener) {
            // Set conversion text
            conversionText.setText(conversion.getDisplayText());

            // Set relative timestamp
            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                    conversion.getTimestamp(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
            );
            timestampText.setText(relativeTime);

            // Set favorite icon
            if (conversion.isFavorite()) {
                favoriteIcon.setImageResource(R.drawable.ic_star_filled);
                favoriteIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.mark));
            } else {
                favoriteIcon.setImageResource(R.drawable.ic_star_border);
                favoriteIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.text_faint));
            }

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onConversionClick(conversion);
                }
            });

            favoriteIcon.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFavoriteClick(conversion);
                }
            });
        }
    }
}
