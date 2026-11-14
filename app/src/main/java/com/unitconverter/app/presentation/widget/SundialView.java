package com.unitconverter.app.presentation.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.unitconverter.app.R;

import java.util.Calendar;

/**
 * A tiny Samrat Yantra (the giant sundial at Jantar Mantar) that tells the
 * real time: by day the sun rides the arc and the gnomon's shadow falls on
 * the hour lines; by night a crescent moon and a few stars take over.
 * Refreshes once a minute while attached.
 */
public class SundialView extends View {

    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint sun = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cut = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF plate = new RectF();
    private final Path moon = new Path();
    private final float dp;
    private int lineColor;

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            invalidate();
            postDelayed(this, 60_000);
        }
    };

    public SundialView(Context context) {
        this(context, null);
    }

    public SundialView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        dp = getResources().getDisplayMetrics().density;
        lineColor = 0xFFFCEFE9;
        int sunColor = 0xFFE8792B;
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SundialView);
            lineColor = a.getColor(R.styleable.SundialView_sundialColor, lineColor);
            sunColor = a.getColor(R.styleable.SundialView_sundialSunColor, sunColor);
            a.recycle();
        }
        line.setStyle(Paint.Style.STROKE);
        line.setColor(lineColor);
        line.setStrokeCap(Paint.Cap.ROUND);

        shadow.setColor(lineColor);
        shadow.setStrokeCap(Paint.Cap.ROUND);

        sun.setColor(sunColor);
        glow.setColor(sunColor);
        glow.setAlpha(60);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        post(tick);
    }

    @Override
    protected void onDetachedFromWindow() {
        removeCallbacks(tick);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        float w = getWidth(), h = getHeight();
        float r = Math.min(w / 2f, h) - 14 * dp;
        float cx = w / 2f, cy = h - 4 * dp;

        Calendar now = Calendar.getInstance();
        float hour = now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE) / 60f;
        boolean day = hour >= 6f && hour < 18f;

        // Dial plate and hour lines (6:00 to 18:00)
        line.setStrokeWidth(1.6f * dp);
        line.setAlpha(150);
        plate.set(cx - r, cy - r, cx + r, cy + r);
        canvas.drawArc(plate, 180, 180, false, line);
        line.setAlpha(90);
        for (int i = 0; i <= 12; i++) {
            double a = Math.PI + Math.PI * i / 12.0;
            float inner = i % 3 == 0 ? r * 0.62f : r * 0.78f;
            canvas.drawLine(cx + (float) Math.cos(a) * inner, cy + (float) Math.sin(a) * inner,
                    cx + (float) Math.cos(a) * r, cy + (float) Math.sin(a) * r, line);
        }
        line.setAlpha(180);
        line.setStrokeWidth(2.4f * dp);
        canvas.drawLine(cx - r - 6 * dp, cy, cx + r + 6 * dp, cy, line);

        if (day) {
            float t = (hour - 6f) / 12f;                  // 0 at sunrise, 1 at sunset
            double sunA = Math.PI + Math.PI * t;          // east (left) to west (right)
            double shadowA = Math.PI + Math.PI * (1 - t); // shadow falls opposite the sun
            float sr = r + 4 * dp;
            float sx = cx + (float) Math.cos(sunA) * sr, sy = cy + (float) Math.sin(sunA) * sr;

            shadow.setStrokeWidth(4 * dp);
            shadow.setAlpha(230);
            canvas.drawLine(cx, cy, cx + (float) Math.cos(shadowA) * r * 0.92f,
                    cy + (float) Math.sin(shadowA) * r * 0.92f, shadow);
            canvas.drawCircle(sx, sy, 11 * dp, glow);
            canvas.drawCircle(sx, sy, 6.5f * dp, sun);
        } else {
            float t = ((hour < 6f ? hour + 24f : hour) - 18f) / 12f;
            double a = Math.PI + Math.PI * t;
            float mr = r + 2 * dp;
            float mx = cx + (float) Math.cos(a) * mr, my = cy + (float) Math.sin(a) * mr;
            float rad = 7 * dp;
            moon.reset();
            moon.addCircle(mx, my, rad, Path.Direction.CW);
            Path bite = new Path();
            bite.addCircle(mx + rad * 0.55f, my - rad * 0.35f, rad, Path.Direction.CW);
            moon.op(bite, Path.Op.DIFFERENCE);
            sun.setAlpha(255);
            canvas.drawPath(moon, sun);
            // Stars
            cut.setColor(lineColor);
            cut.setAlpha(200);
            float[][] stars = {{0.18f, 0.25f}, {0.82f, 0.18f}, {0.66f, 0.42f}, {0.30f, 0.55f}};
            for (float[] s : stars) {
                canvas.drawCircle(w * s[0], h * s[1], 1.6f * dp, cut);
            }
        }
    }
}
