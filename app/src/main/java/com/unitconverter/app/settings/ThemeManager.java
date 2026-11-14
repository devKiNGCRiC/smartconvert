package com.unitconverter.app.settings;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.unitconverter.app.R;
import com.unitconverter.app.domain.model.UnitCategory;

/**
 * Applies the chosen mood to activities, resolves theme tokens, and runs the
 * mood-switch transition: the old screen is captured, the activity is
 * recreated in the new mood, and the new look spreads out in a circle from
 * the point the user tapped.
 */
public final class ThemeManager {

    private ThemeManager() {
    }

    // Snapshot carried across recreate() for the reveal.
    private static Bitmap pendingSnapshot;
    private static float pendingX;
    private static float pendingY;

    /** Call before super.onCreate / setContentView. */
    public static void apply(Activity activity) {
        activity.setTheme(themeFor(AppSettings.of(activity).getMood()));
    }

    public static int themeFor(AppSettings.Mood mood) {
        switch (mood) {
            case BAZAAR: return R.style.Theme_SC_Bazaar;
            case TRUCK: return R.style.Theme_SC_Truck;
            default: return R.style.Theme_SC_Jantar;
        }
    }

    @ColorInt
    public static int color(Context context, @AttrRes int attr) {
        TypedArray a = context.obtainStyledAttributes(new int[]{attr});
        try {
            return a.getColor(0, 0xFF808080);
        } finally {
            a.recycle();
        }
    }

    @ColorInt
    public static int hue(Context context, UnitCategory category) {
        switch (category) {
            case WEIGHT: return color(context, R.attr.scHueWeight);
            case TEMPERATURE: return color(context, R.attr.scHueTemperature);
            case VOLUME: return color(context, R.attr.scHueVolume);
            case AREA: return color(context, R.attr.scHueArea);
            case SPEED: return color(context, R.attr.scHueSpeed);
            case TIME: return color(context, R.attr.scHueTime);
            case DIGITAL_STORAGE: return color(context, R.attr.scHueStorage);
            default: return color(context, R.attr.scHueLength);
        }
    }

    /**
     * Changes look settings and recreates {@code activity} with a circular reveal
     * centred on {@code originX/originY} (window coordinates).
     */
    public static void changeLook(@NonNull Activity activity, float originX, float originY,
                                  @NonNull Runnable change) {
        View decor = activity.getWindow().getDecorView();
        Bitmap snapshot = null;
        try {
            snapshot = Bitmap.createBitmap(decor.getWidth(), decor.getHeight(), Bitmap.Config.ARGB_8888);
            decor.draw(new Canvas(snapshot));
        } catch (OutOfMemoryError | IllegalArgumentException ignored) {
            // No snapshot: the change still applies, just without the reveal.
        }
        pendingSnapshot = snapshot;
        pendingX = originX;
        pendingY = originY;

        int nightBefore = AppCompatDelegate.getDefaultNightMode();
        change.run();
        AppSettings settings = AppSettings.of(activity);
        settings.applyGlobal(); // recreates on its own when the night mode changes
        if (AppCompatDelegate.getDefaultNightMode() == nightBefore) {
            activity.recreate();
        }
    }

    /** Call at the end of onCreate; plays the reveal if a look change is in flight. */
    public static void playPendingReveal(@NonNull Activity activity) {
        final Bitmap snapshot = pendingSnapshot;
        if (snapshot == null) return;
        pendingSnapshot = null;

        final ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
        final RevealOverlay overlay = new RevealOverlay(activity, snapshot, pendingX, pendingY,
                color(activity, R.attr.scAccent));
        decor.addView(overlay, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        overlay.post(() -> {
            float maxRadius = (float) Math.hypot(
                    Math.max(pendingX, overlay.getWidth() - pendingX),
                    Math.max(pendingY, overlay.getHeight() - pendingY));
            ValueAnimator anim = ValueAnimator.ofFloat(0f, maxRadius);
            anim.setDuration(620);
            anim.setInterpolator(new FastOutSlowInInterpolator());
            anim.addUpdateListener(a -> overlay.setHoleRadius((float) a.getAnimatedValue()));
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    decor.removeView(overlay);
                    snapshot.recycle();
                }
            });
            anim.start();
        });
    }

    /** Draws the old screen with a growing circular hole showing the new one. */
    private static final class RevealOverlay extends View {
        private final Bitmap snapshot;
        private final float cx;
        private final float cy;
        private final Path hole = new Path();
        private final Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float radius;

        RevealOverlay(Context context, Bitmap snapshot, float cx, float cy, int ringColor) {
            super(context);
            this.snapshot = snapshot;
            this.cx = cx;
            this.cy = cy;
            ring.setStyle(Paint.Style.STROKE);
            ring.setStrokeWidth(4f * getResources().getDisplayMetrics().density);
            ring.setColor(ringColor);
            setClickable(true); // swallow taps while the transition runs
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        }

        void setHoleRadius(float r) {
            radius = r;
            invalidate();
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            if (snapshot.isRecycled()) return;
            hole.reset();
            hole.addCircle(cx, cy, radius, Path.Direction.CW);
            canvas.save();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                canvas.clipOutPath(hole);
            } else {
                canvas.clipPath(hole, Region.Op.DIFFERENCE);
            }
            canvas.drawBitmap(snapshot, 0, 0, null);
            canvas.restore();
            if (radius > 0) canvas.drawCircle(cx, cy, radius, ring);
        }
    }
}
