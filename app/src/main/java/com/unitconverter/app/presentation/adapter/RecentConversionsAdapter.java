package com.unitconverter.app.presentation.adapter;

import android.content.Context;
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

import com.unitconverter.app.R;
import com.unitconverter.app.data.database.ConversionHistoryEntity;
import com.unitconverter.app.domain.model.UnitCategory;
import com.unitconverter.app.presentation.widget.TicketDrawable;
import com.unitconverter.app.settings.ThemeManager;
import com.unitconverter.app.util.NumberFormatter;
import com.unitconverter.app.util.UnitText;

import java.util.ArrayList;
import java.util.List;

/**
 * Recent conversions as a horizontal row of railway tickets
 * ("1,250 km" over "12,50,000 m", star and time on the stub).
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
        holder.bind(conversions.get(position), clickListener);
    }

    @Override
    public int getItemCount() {
        return Math.min(conversions.size(), 8);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView category;
        private final TextView from;
        private final TextView to;
        private final TextView timestamp;
        private final ImageView favoriteIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            category = itemView.findViewById(R.id.ticketCategory);
            from = itemView.findViewById(R.id.ticketFrom);
            to = itemView.findViewById(R.id.conversionText);
            timestamp = itemView.findViewById(R.id.timestampText);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
        }

        void bind(ConversionHistoryEntity c, OnConversionClickListener listener) {
            Context ctx = itemView.getContext();
            float dp = ctx.getResources().getDisplayMetrics().density;
            itemView.setBackground(new TicketDrawable(
                    ThemeManager.color(ctx, R.attr.scSurface),
                    ThemeManager.color(ctx, R.attr.scLine), dp, 0.76f));

            UnitCategory cat = null;
            try {
                cat = UnitCategory.valueOf(c.getCategory());
            } catch (IllegalArgumentException ignored) {
            }
            category.setText(cat != null ? cat.getDisplayName() : c.getCategory());
            Drawable dot = ContextCompat.getDrawable(ctx, R.drawable.bg_dot);
            if (dot != null && cat != null) {
                dot = DrawableCompat.wrap(dot.mutate());
                DrawableCompat.setTint(dot, ThemeManager.hue(ctx, cat));
                category.setCompoundDrawablesRelativeWithIntrinsicBounds(dot, null, null, null);
            }

            from.setText(NumberFormatter.formatDisplay(c.getInputValue()) + " "
                    + UnitText.symbolOf(c.getFromUnit()));
            to.setText(NumberFormatter.formatDisplay(c.getResultValue()) + " "
                    + UnitText.symbolOf(c.getToUnit()));
            timestamp.setText(DateUtils.getRelativeTimeSpanString(
                    c.getTimestamp(), System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE));

            favoriteIcon.setImageResource(c.isFavorite() ? R.drawable.ic_star_filled : R.drawable.ic_star_border);
            favoriteIcon.setColorFilter(ThemeManager.color(ctx,
                    c.isFavorite() ? R.attr.scAccent : R.attr.scInkFaint), PorterDuff.Mode.SRC_IN);

            itemView.setContentDescription(c.getDisplayText());
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onConversionClick(c);
            });
            favoriteIcon.setOnClickListener(v -> {
                if (listener != null) listener.onFavoriteClick(c);
            });
        }
    }
}
