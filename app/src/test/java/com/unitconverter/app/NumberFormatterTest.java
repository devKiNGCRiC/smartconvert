package com.unitconverter.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.unitconverter.app.data.repository.ConversionRepository;
import com.unitconverter.app.util.NumberFormatter;

import org.junit.Test;

import java.util.Locale;

/**
 * Tests for input parsing and result formatting.
 */
public class NumberFormatterTest {

    @Test
    public void format_wholeNumber_hasNoDecimals() {
        assertEquals("100", NumberFormatter.format(100.0));
        assertEquals("1000", NumberFormatter.format(1000.0));
    }

    @Test
    public void format_trimsTrailingZerosAndRoundsToSixSignificantFigures() {
        assertEquals("1.524", NumberFormatter.format(1.524));
        assertEquals("2.20462", NumberFormatter.format(2.2046226218));
        assertEquals("-2.5", NumberFormatter.format(-2.5));
    }

    @Test
    public void format_zero() {
        assertEquals("0", NumberFormatter.format(0.0));
    }

    @Test
    public void format_verySmallValue_usesScientificNotationInsteadOfZero() {
        // 1 bit expressed in terabytes used to be displayed as "0"
        String formatted = NumberFormatter.format(1.0 / 8 / 1024 / 1024 / 1024 / 1024);
        assertEquals("1.13687E-13", formatted);
    }

    @Test
    public void format_veryLargeValue_usesScientificNotation() {
        assertEquals("1E+20", NumberFormatter.format(1e20));
    }

    @Test
    public void format_isIndependentOfDeviceLocale() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);
            assertEquals("1.524", NumberFormatter.format(1.524));
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    public void format_nanAndInfinity_returnEmpty() {
        assertEquals("", NumberFormatter.format(Double.NaN));
        assertEquals("", NumberFormatter.format(Double.POSITIVE_INFINITY));
    }

    @Test
    public void repository_formatValue_matchesFormatter() {
        assertEquals("0.3048", ConversionRepository.getInstance().formatValue(0.3048));
    }

    @Test
    public void parse_acceptsDotAndCommaDecimalSeparators() {
        assertEquals(1.5, NumberFormatter.parse("1.5"), 0.0);
        assertEquals(1.5, NumberFormatter.parse("1,5"), 0.0);
        assertEquals(-0.25, NumberFormatter.parse(" -0,25 "), 0.0);
    }

    @Test(expected = NumberFormatException.class)
    public void parse_rejectsText() {
        NumberFormatter.parse("abc");
    }

    @Test
    public void isIncomplete_recognisesHalfTypedNumbers() {
        assertTrue(NumberFormatter.isIncomplete("-"));
        assertTrue(NumberFormatter.isIncomplete("."));
        assertTrue(NumberFormatter.isIncomplete("-,"));
        assertFalse(NumberFormatter.isIncomplete("1"));
        assertFalse(NumberFormatter.isIncomplete("abc"));
        assertFalse(NumberFormatter.isIncomplete(""));
    }
}
