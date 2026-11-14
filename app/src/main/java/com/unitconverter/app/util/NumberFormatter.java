package com.unitconverter.app.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Locale-independent number formatting and parsing for conversions.
 *
 * Results are rounded to 6 significant figures. Values that would show as "0"
 * with plain notation (very small) or run to many digits (very large) fall back
 * to scientific notation, e.g. 1 bit in terabytes is "1.13687E-13" instead of "0".
 */
public final class NumberFormatter {

    private static final int SIGNIFICANT_FIGURES = 6;
    private static final double PLAIN_MIN = 1e-6;
    private static final double PLAIN_MAX = 1e15;

    private NumberFormatter() {
    }

    /**
     * Formats a value for display using 6 significant figures.
     * Never depends on the device locale, so output is always e.g. "1.524".
     */
    public static String format(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "";
        }
        if (value == 0) {
            return "0";
        }

        BigDecimal rounded = new BigDecimal(value)
                .round(new MathContext(SIGNIFICANT_FIGURES, RoundingMode.HALF_UP))
                .stripTrailingZeros();

        double abs = Math.abs(value);
        if (abs >= PLAIN_MIN && abs < PLAIN_MAX) {
            return rounded.toPlainString();
        }
        return rounded.toString();
    }

    /**
     * Parses user input, accepting both '.' and ',' as the decimal separator
     * (numeric keyboards emit ',' in many locales).
     *
     * @throws NumberFormatException if the text is not a number
     */
    public static double parse(String input) {
        if (input == null) {
            throw new NumberFormatException("null input");
        }
        return Double.parseDouble(input.trim().replace(',', '.'));
    }

    /**
     * True for text that is a valid start of a number but not yet a number
     * (for example "-" or "."), so the UI should not flag it as an error.
     */
    public static boolean isIncomplete(String input) {
        if (input == null) {
            return false;
        }
        String text = input.trim();
        return text.equals("-") || text.equals(".") || text.equals(",")
                || text.equals("-.") || text.equals("-,");
    }
}
