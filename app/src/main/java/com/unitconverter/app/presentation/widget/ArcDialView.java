package com.unitconverter.app.presentation.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.unitconverter.app.R;

/**
 * A half-circle magnitude dial, after the quadrant scales of Jaipur's
 * Samrat Yantra. The needle points at the size of the result on a
 * logarithmic scale labelled with SI prefixes (µ, m, 1, k, M, G, T), so you
 * can see at a glance whether a number is tiny or huge.
 *
 * The needle sweeps to new values; the sweep follows the system animator
 * scale, so it is instant when animations are off.
 */
public class ArcDialView extends View {

    private static final int MIN_EXP = -6;
    private static final int MAX_EXP = 12;
    private static final String[] LABELS = {"µ", "m", "1", "k", "M", "G", "T"};

    private final Paint track = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progress = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tick = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint label = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint needle = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hub = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcRect = new RectF();
    private final Path needlePath = new Path();
    private final float dp;

    /** 0..1 along the arc. */
    private float position = 0f;
    private boolean hasValue = false;
    private ValueAnimator animator;

    public ArcDialView(Context context) {
        this(context, null);
    }

    public ArcDialView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        dp = getResources().getDisplayMetrics().density;
        int ink = 0xFF2A1E3F, accent = 0xFFE8792B, trackColor = 0x332A1E3F;
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ArcDialView);
            ink = a.getColor(R.styleable.ArcDialView_dialColor, ink);
            accent = a.getColor(R.styleable.ArcDialView_dialNeedleColor, accent);
            trackColor = a.getColor(R.styleable.ArcDialView_dialTrackColor, trackColor);
            a.recycle();
        }
        track.setStyle(Paint.Style.STROKE);
        track.setStrokeCap(Paint.Cap.ROUND);
        track.setColor(trackColor);

        progress.setStyle(Paint.Style.STROKE);
        progress.setStrokeCap(Paint.Cap.ROUND);
        progress.setColor(accent);

        tick.setColor(ink);
        tick.setStrokeCap(Paint.Cap.ROUND);

        label.setColor(ink);
        label.setTextAlign(Paint.Align.CENTER);
        label.setTextSize(12 * getResources().getDisplayMetrics().scaledDensity);
        label.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        label.setAlpha(170);

        needle.setColor(accent);
        hub.setColor(ink);

        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        // Cap the height so the dial stays compact on landscape phones and tablets;
        // onDraw centres it horizontally and sizes the radius from the height.
        int h = Math.min(Math.round(w * 0.56f), Math.round(200 * dp));
        setMeasuredDimension(w, resolveSize(h, heightMeasureSpec));
    }

    /** Points the needle at |value|; NaN or null clears it. */
    public void setValue(@Nullable Double value) {
        float target;
        if (value == null || value.isNaN() || value.isInfinite()) {
            hasValue = false;
            target = 0f;
        } else {
            hasValue = true;
            double abs = Math.abs(value);
            double exp = abs == 0 ? MIN_EXP : Math.log10(abs);
            exp = Math.max(MIN_EXP, Math.min(MAX_EXP, exp));
            target = (float) ((exp - MIN_EXP) / (MAX_EXP - MIN_EXP));
        }
        if (animator != null) animator.cancel();
        animator = ValueAnimator.ofFloat(position, target);
        animator.setDuration(650);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.addUpdateListener(a -> {
            position = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        float w = getWidth();
        float stroke = 14 * dp;
        float pad = stroke / 2 + 4 * dp;
        float radius = Math.min(w / 2 - pad, getHeight() - pad - 6 * dp);
        float cx = w / 2;
        float cy = pad + radius;
        arcRect.set(cx - radius, cy - radius, cx + radius, cy + radius);

        track.setStrokeWidth(stroke);
        progress.setStrokeWidth(stroke);
        canvas.drawArc(arcRect, 180, 180, false, track);
        if (hasValue || position > 0.001f) {
            canvas.drawArc(arcRect, 180, 180 * position, false, progress);
        }

        // Ticks: one per power of ten, longer every third (each SI prefix)
        int steps = MAX_EXP - MIN_EXP;
        float inner = radius - stroke / 2 - 6 * dp;
        for (int i = 0; i <= steps; i++) {
            double angle = Math.PI + Math.PI * i / steps;
            boolean major = i % 3 == 0;
            float len = major ? 12 * dp : 6 * dp;
            tick.setStrokeWidth(major ? 2.2f * dp : 1.4f * dp);
            tick.setAlpha(major ? 220 : 120);
            float cos = (float) Math.cos(angle), sin = (float) Math.sin(angle);
            canvas.drawLine(cx + cos * inner, cy + sin * inner,
                    cx + cos * (inner - len), cy + sin * (inner - len), tick);
            if (major) {
                float lr = inner - len - 12 * dp;
                canvas.drawText(LABELS[i / 3], cx + cos * lr,
                        cy + sin * lr + label.getTextSize() / 3, label);
            }
        }

        // Needle: a slim tapered blade from the hub, like a gnomon's shadow
        double a = Math.PI + Math.PI * position;
        float len = inner - 4 * dp;
        float tipX = cx + (float) Math.cos(a) * len;
        float tipY = cy + (float) Math.sin(a) * len;
        float bw = 5 * dp;
        float nx = (float) -Math.sin(a) * bw, ny = (float) Math.cos(a) * bw;
        needlePath.reset();
        needlePath.moveTo(tipX, tipY);
        needlePath.lineTo(cx + nx, cy + ny);
        needlePath.lineTo(cx - nx, cy - ny);
        needlePath.close();
        needle.setAlpha(hasValue ? 255 : 90);
        canvas.drawPath(needlePath, needle);
        canvas.drawCircle(cx, cy, 8 * dp, hub);
        canvas.drawCircle(cx, cy, 3.5f * dp, needle);
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) animator.cancel();
        super.onDetachedFromWindow();
    }
}
