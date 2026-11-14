package com.unitconverter.app;

import static org.junit.Assert.assertEquals;

import com.unitconverter.app.util.NumberFormatter;
import com.unitconverter.app.util.NumberFormatter.Grouping;

import org.junit.Test;

/** Digit grouping (international and lakh/crore) and numbers in words. */
public class NumberDisplayTest {

    @Test
    public void international_groupsInThrees() {
        assertEquals("1,250,000", NumberFormatter.formatDisplay(1250000, Grouping.INTERNATIONAL));
        assertEquals("999", NumberFormatter.formatDisplay(999, Grouping.INTERNATIONAL));
        assertEquals("1,000", NumberFormatter.formatDisplay(1000, Grouping.INTERNATIONAL));
    }

    @Test
    public void indian_groupsLakhAndCrore() {
        assertEquals("12,50,000", NumberFormatter.formatDisplay(1250000, Grouping.INDIAN));
        assertEquals("1,00,000", NumberFormatter.formatDisplay(100000, Grouping.INDIAN));
        assertEquals("1,23,45,600", NumberFormatter.formatDisplay(12345600, Grouping.INDIAN));
        // Display rounds to 6 significant figures before grouping
        assertEquals("1,23,45,700", NumberFormatter.formatDisplay(12345678, Grouping.INDIAN));
        assertEquals("1,000", NumberFormatter.formatDisplay(1000, Grouping.INDIAN));
    }

    @Test
    public void keepsDecimalsAndSign() {
        assertEquals("-12,345.6", NumberFormatter.formatDisplay(-12345.6, Grouping.INTERNATIONAL));
        assertEquals("0.3048", NumberFormatter.formatDisplay(0.3048, Grouping.INDIAN));
    }

    @Test
    public void scientific_usesTimesTenAndSuperscript() {
        assertEquals("1.13687 × 10⁻¹³",
                NumberFormatter.formatDisplay(1.0 / 8 / 1024 / 1024 / 1024 / 1024, Grouping.INTERNATIONAL));
        assertEquals("1 × 10²⁰", NumberFormatter.formatDisplay(1e20, Grouping.INTERNATIONAL));
    }

    @Test
    public void words_indian() {
        assertEquals("12.5 lakh", NumberFormatter.inWords(1250000, Grouping.INDIAN));
        assertEquals("3.4 crore", NumberFormatter.inWords(34000000, Grouping.INDIAN));
        assertEquals("1,250 crore", NumberFormatter.inWords(12500000000.0, Grouping.INDIAN));
        assertEquals("5 thousand", NumberFormatter.inWords(5000, Grouping.INDIAN));
    }

    @Test
    public void words_international() {
        assertEquals("1.25 million", NumberFormatter.inWords(1250000, Grouping.INTERNATIONAL));
        assertEquals("7.5 billion", NumberFormatter.inWords(7.5e9, Grouping.INTERNATIONAL));
    }

    @Test
    public void words_emptyForSmallNumbers() {
        assertEquals("", NumberFormatter.inWords(999, Grouping.INTERNATIONAL));
        assertEquals("", NumberFormatter.inWords(Double.NaN, Grouping.INDIAN));
    }
}
