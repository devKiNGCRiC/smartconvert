package com.unitconverter.app.presentation.widget;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A railway-ticket shape: rounded rectangle with two semicircular notches and
 * a perforated line between them. Used for recent conversions and history.
 *
 * {@code stubAt} is the notch position as a fraction of the width (notches on
 * top and bottom edges, perforation vertical).
 */
public class TicketDrawable extends Drawable {

    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint perf = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final float radius;
    private final float notch;
    private final float stubAt;

    public TicketDrawable(int fillColor, int strokeColor, float density, float stubAt) {
        this.radius = 18 * density;
        this.notch = 9 * density;
        this.stubAt = stubAt;
        fill.setColor(fillColor);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(1 * density);
        stroke.setColor(strokeColor);
        perf.setStyle(Paint.Style.STROKE);
        perf.setStrokeWidth(1.5f * density);
        perf.setColor(strokeColor);
        perf.setPathEffect(new DashPathEffect(new float[]{5 * density, 5 * density}, 0));
    }

    @Override
    protected void onBoundsChange(@NonNull Rect b) {
        super.onBoundsChange(b);
        path.reset();
        RectF r = new RectF(b);
        r.inset(stroke.getStrokeWidth() / 2, stroke.getStrokeWidth() / 2);
        float nx = r.left + r.width() * stubAt;

        path.moveTo(r.left + radius, r.top);
        path.lineTo(nx - notch, r.top);
        path.arcTo(new RectF(nx - notch, r.top - notch, nx + notch, r.top + notch), 180, -180);
        path.lineTo(r.right - radius, r.top);
        path.arcTo(new RectF(r.right - 2 * radius, r.top, r.right, r.top + 2 * radius), 270, 90);
        path.lineTo(r.right, r.bottom - radius);
        path.arcTo(new RectF(r.right - 2 * radius, r.bottom - 2 * radius, r.right, r.bottom), 0, 90);
        path.lineTo(nx + notch, r.bottom);
        path.arcTo(new RectF(nx - notch, r.bottom - notch, nx + notch, r.bottom + notch), 0, -180);
        path.lineTo(r.left + radius, r.bottom);
        path.arcTo(new RectF(r.left, r.bottom - 2 * radius, r.left + 2 * radius, r.bottom), 90, 90);
        path.lineTo(r.left, r.top + radius);
        path.arcTo(new RectF(r.left, r.top, r.left + 2 * radius, r.top + 2 * radius), 180, 90);
        path.close();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawPath(path, fill);
        canvas.drawPath(path, stroke);
        Rect b = getBounds();
        float nx = b.left + b.width() * stubAt;
        canvas.drawLine(nx, b.top + notch + 4, nx, b.bottom - notch - 4, perf);
    }

    @Override
    public void getOutline(@NonNull Outline outline) {
        outline.setRoundRect(getBounds(), radius);
        outline.setAlpha(0.9f);
    }

    @Override
    public void setAlpha(int alpha) {
        fill.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        fill.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
