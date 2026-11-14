package com.unitconverter.app.util;

/**
 * History stores units as display text, e.g. "Kilometer (km)".
 * These helpers split that back into name and symbol for compact display.
 */
public final class UnitText {

    private UnitText() {
    }

    /** "Kilometer (km)" -> "km"; falls back to the whole text. */
    public static String symbolOf(String displayText) {
        if (displayText == null) return "";
        int open = displayText.lastIndexOf('(');
        int close = displayText.lastIndexOf(')');
        if (open >= 0 && close > open) {
            return displayText.substring(open + 1, close);
        }
        return displayText;
    }

    /** "Kilometer (km)" -> "Kilometer"; "Bigha (Bengal) (bigha B)" -> "Bigha (Bengal)". */
    public static String nameOf(String displayText) {
        if (displayText == null) return "";
        int open = displayText.lastIndexOf(" (");
        return open > 0 ? displayText.substring(0, open) : displayText;
    }
}
