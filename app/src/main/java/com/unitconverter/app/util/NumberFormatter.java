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

    // ------------------------------------------------------------------
    // Display formatting: digit grouping and numbers in words
    // ------------------------------------------------------------------

    /** How to group digits for display. */
    public enum Grouping {
        /** 1,250,000 · million / billion / trillion */
        INTERNATIONAL,
        /** 12,50,000 · lakh / crore */
        INDIAN
    }

    private static volatile Grouping grouping = Grouping.INTERNATIONAL;

    public static void setGrouping(Grouping g) {
        grouping = g == null ? Grouping.INTERNATIONAL : g;
    }

    public static Grouping getGrouping() {
        return grouping;
    }

    /** {@link #format(double)} with digit grouping in the current style. */
    public static String formatDisplay(double value) {
        return formatDisplay(value, grouping);
    }

    /**
     * Groups the integer digits ("1250000" becomes "1,250,000" or "12,50,000")
     * and writes scientific notation as "1.13687 × 10⁻¹³".
     */
    public static String formatDisplay(double value, Grouping style) {
        String plain = format(value);
        if (plain.isEmpty()) return plain;

        int e = plain.indexOf('E');
        if (e >= 0) {
            String mantissa = plain.substring(0, e);
            String exponent = plain.substring(e + 1).replace("+", "");
            return mantissa + " × 10" + superscript(exponent);
        }

        String sign = "";
        if (plain.startsWith("-")) {
            sign = "-";
            plain = plain.substring(1);
        }
        int dot = plain.indexOf('.');
        String integer = dot >= 0 ? plain.substring(0, dot) : plain;
        String fraction = dot >= 0 ? plain.substring(dot) : "";
        return sign + group(integer, style) + fraction;
    }

    /** Inserts separators into a run of digits. */
    static String group(String digits, Grouping style) {
        int n = digits.length();
        if (n <= 3) return digits;
        StringBuilder out = new StringBuilder(digits.substring(n - 3));
        int i = n - 3;
        int size = style == Grouping.INDIAN ? 2 : 3;
        while (i > 0) {
            int start = Math.max(0, i - size);
            out.insert(0, digits.substring(start, i) + ",");
            i = start;
        }
        return out.toString();
    }

    /** Words for the current grouping, e.g. "12.5 lakh". */
    public static String inWords(double value) {
        return inWords(value, grouping);
    }

    /**
     * A friendly reading of large numbers: "12.5 lakh", "3.4 crore",
     * "1.25 million". Empty below one thousand, where digits read fine.
     */
    public static String inWords(double value, Grouping style) {
        if (Double.isNaN(value) || Double.isInfinite(value)) return "";
        double abs = Math.abs(value);
        if (abs < 1000 || abs >= 1e18) return "";

        double[] steps;
        String[] names;
        if (style == Grouping.INDIAN) {
            steps = new double[]{1e7, 1e5, 1e3};
            names = new String[]{"crore", "lakh", "thousand"};
        } else {
            steps = new double[]{1e12, 1e9, 1e6, 1e3};
            names = new String[]{"trillion", "billion", "million", "thousand"};
        }
        for (int i = 0; i < steps.length; i++) {
            if (abs >= steps[i]) {
                double scaled = value / steps[i];
                BigDecimal rounded = new BigDecimal(scaled)
                        .setScale(2, RoundingMode.HALF_UP)
                        .stripTrailingZeros();
                String number = rounded.scale() <= 0
                        ? rounded.toBigInteger().toString()
                        : rounded.toPlainString();
                // Group the whole part too, for "1,250 crore"
                String sign = number.startsWith("-") ? "-" : "";
                if (!sign.isEmpty()) number = number.substring(1);
                int dot = number.indexOf('.');
                String whole = dot >= 0 ? number.substring(0, dot) : number;
                String frac = dot >= 0 ? number.substring(dot) : "";
                return sign + group(whole, style) + frac + " " + names[i];
            }
        }
        return "";
    }

    private static String superscript(String exponent) {
        StringBuilder sb = new StringBuilder();
        for (char c : exponent.toCharArray()) {
            switch (c) {
                case '-': sb.append('⁻'); break;
                case '0': sb.append('⁰'); break;
                case '1': sb.append('¹'); break;
                case '2': sb.append('²'); break;
                case '3': sb.append('³'); break;
                case '4': sb.append('⁴'); break;
                case '5': sb.append('⁵'); break;
                case '6': sb.append('⁶'); break;
                case '7': sb.append('⁷'); break;
                case '8': sb.append('⁸'); break;
                case '9': sb.append('⁹'); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
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
