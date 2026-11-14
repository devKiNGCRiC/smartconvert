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
}
