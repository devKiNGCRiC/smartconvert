package com.unitconverter.app.util;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Edge-to-edge support. From targetSdk 35 the system draws the app behind the
 * status and navigation bars, so every screen must pad its content by the
 * system bar insets (and by the keyboard while it is open).
 */
public final class WindowInsetsHelper {

    private WindowInsetsHelper() {
    }

    /** Pads {@code root} so its content stays clear of system bars, cutouts and the keyboard. */
    public static void applySystemBarPadding(View root) {
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout()
                            | WindowInsetsCompat.Type.ime());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    /**
     * For screens whose coloured hero runs under the status bar: {@code root}
     * is padded for the sides, navigation bar and keyboard, while
     * {@code heroContent} gets the status bar height added to its top padding.
     */
    public static void applyHeroInsets(View root, View heroContent) {
        applyHeroInsets(root, heroContent, null);
    }

    /** As above; {@code statusScrim}, if given, is sized to the status bar. */
    public static void applyHeroInsets(View root, View heroContent, View statusScrim) {
        final int baseTop = heroContent.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout());
            Insets ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, 0, bars.right, Math.max(bars.bottom, ime.bottom));
            heroContent.setPadding(heroContent.getPaddingLeft(), baseTop + bars.top,
                    heroContent.getPaddingRight(), heroContent.getPaddingBottom());
            if (statusScrim != null) {
                android.view.ViewGroup.LayoutParams lp = statusScrim.getLayoutParams();
                lp.height = bars.top;
                statusScrim.setLayoutParams(lp);
            }
            return WindowInsetsCompat.CONSUMED;
        });
    }
}
