package com.unitconverter.app.presentation.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.unitconverter.app.R;

/**
 * Draws a strip of ruler ticks: a short tick every step, a medium one every 5th,
 * a long one every 10th. Ticks hang from the top edge (or stand on the bottom
 * edge with {@code app:rulerFromBottom="true"}).
 *
 * {@link #nudge(int)} slides the scale sideways, like tape being pulled out,
 * so a new result visibly "moves" the instrument. The animation uses the
 * system animator scale, so it is skipped when the user has turned animations off.
 */
public class RulerView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float step;
    private final float shortTick;
    private final float midTick;
    private final float longTick;
    private final boolean fromBottom;

    /** Horizontal scroll of the scale, in px. */
    private float offset = 0f;
    private ValueAnimator animator;

    public RulerView(Context context) {
        this(context, null);
    }

    public RulerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        float dp = getResources().getDisplayMetrics().density;

        int color = 0xFF1C2A33;
        boolean bottom = false;
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RulerView);
            color = a.getColor(R.styleable.RulerView_rulerColor, color);
            bottom = a.getBoolean(R.styleable.RulerView_rulerFromBottom, false);
            a.recycle();
        }
        fromBottom = bottom;

        step = 6f * dp;
        shortTick = 5f * dp;
        midTick = 9f * dp;
        longTick = 14f * dp;

        paint.setColor(color);
        paint.setStrokeWidth(Math.max(1f, dp));
        paint.setStrokeCap(Paint.Cap.BUTT);

        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desired = (int) Math.ceil(longTick + getPaddingTop() + getPaddingBottom());
        setMeasuredDimension(
                getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                resolveSize(desired, heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float width = getWidth();
        float top = getPaddingTop();
        float bottom = getHeight() - getPaddingBottom();

        // Index of the first tick at or left of x = 0, accounting for scroll.
        int first = (int) Math.floor(-offset / step);
        for (int i = first; ; i++) {
            float x = i * step + offset;
            if (x > width) break;
            if (x < 0) continue;

            int mod = ((i % 10) + 10) % 10; // floor-mod; Math.floorMod needs API 24
            float len = mod == 0 ? longTick : (mod == 5 ? midTick : shortTick);
            if (fromBottom) {
                canvas.drawLine(x, bottom - len, x, bottom, paint);
            } else {
                canvas.drawLine(x, top, x, top + len, paint);
            }
        }
    }

    /**
     * Slides the scale by {@code ticks} tick steps (negative moves left).
     */
    public void nudge(int ticks) {
        if (animator != null) animator.cancel();
        float start = offset;
        float end = start + ticks * step;
        animator = ValueAnimator.ofFloat(start, end);
        animator.setDuration(420);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.addUpdateListener(a -> {
            // Keep the offset within one decade so numbers never grow unbounded.
            offset = ((float) a.getAnimatedValue()) % (step * 10);
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) animator.cancel();
        super.onDetachedFromWindow();
    }
}
