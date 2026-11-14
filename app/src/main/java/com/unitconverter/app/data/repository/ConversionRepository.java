package com.unitconverter.app.data.repository;

import com.unitconverter.app.domain.model.ConversionResult;
import com.unitconverter.app.util.NumberFormatter;
import com.unitconverter.app.domain.model.Unit;
import com.unitconverter.app.domain.model.UnitCategory;

/**
 * Repository class that handles all unit conversion logic.
 * 
 * TEACHING NOTE: Repository Pattern
 * - Abstracts data operations from business logic
 * - Single source of truth for conversions
 * - Easy to test (can mock this class)
 * - Can add caching, logging, analytics here
 * 
 * Why Repository?
 * - Separates "how we convert" from "what we display"
 * - If conversion logic changes, only this class changes
 * - Makes the code modular and maintainable
 * 
 * Design Pattern: Repository Pattern + Strategy Pattern
 * - Repository: Manages data and business logic
 * - Strategy: Different conversion strategies for different unit types
 */
public class ConversionRepository {
    private static ConversionRepository instance;

    /**
     * Private constructor for Singleton pattern.
     */
    private ConversionRepository() {
        // Can initialize caching, preferences, etc. here
    }

    /**
     * Gets the singleton instance.
     * Thread-safe implementation.
     */
    public static synchronized ConversionRepository getInstance() {
        if (instance == null) {
            instance = new ConversionRepository();
        }
        return instance;
    }

    /**
     * Converts a value from one unit to another.
     * 
     * TEACHING NOTE: The Two-Step Conversion Strategy
     * Step 1: Convert input to base unit (e.g., feet → meters)
     * Step 2: Convert base unit to output (e.g., meters → centimeters)
     * 
     * Why?
     * - Simpler than maintaining conversion for every pair (n² complexity)
     * - Only need n conversions (to/from base) instead of n² conversions
     * - Example: 10 units = 10 conversions, not 90 pair conversions
     * - Easy to add new units (just define one conversion factor)
     * 
     * Formula for regular units:
     * 1. toBase = inputValue × fromUnit.conversionFactor
     * 2. result = toBase ÷ toUnit.conversionFactor
     * 
     * Formula for temperature (has offset):
     * 1. toBase = (inputValue + fromUnit.offset) × fromUnit.conversionFactor
     * 2. result = (toBase ÷ toUnit.conversionFactor) - toUnit.offset
     * 
     * @param value    The value to convert
     * @param fromUnit Source unit
     * @param toUnit   Target unit
     * @return ConversionResult object with all conversion data
     * @throws IllegalArgumentException if units are incompatible
     */
    public ConversionResult convert(double value, Unit fromUnit, Unit toUnit) {
        // Validation: Ensure units are compatible
        if (fromUnit == null || toUnit == null) {
            throw new IllegalArgumentException("Units cannot be null");
        }
        
        if (fromUnit.getCategory() != toUnit.getCategory()) {
            throw new IllegalArgumentException(
                String.format("Cannot convert %s to %s: different categories (%s vs %s)",
                    fromUnit.getName(), toUnit.getName(),
                    fromUnit.getCategory(), toUnit.getCategory())
            );
        }

        // Special case: converting to same unit
        if (fromUnit.equals(toUnit)) {
            return new ConversionResult(value, value, fromUnit, toUnit);
        }

        // Determine which conversion strategy to use
        double result;
        if (fromUnit.getCategory() == UnitCategory.TEMPERATURE) {
            result = convertTemperature(value, fromUnit, toUnit);
        } else {
            result = convertRegularUnit(value, fromUnit, toUnit);
        }

        return new ConversionResult(value, result, fromUnit, toUnit);
    }

    /**
     * Converts regular units (non-temperature) using the two-step method.
     * 
     * TEACHING NOTE: This is a pure function - no side effects.
     * - Same input always produces same output
     * - Doesn't modify any state
     * - Easy to test and reason about
     * 
     * @param value    Input value
     * @param fromUnit Source unit
     * @param toUnit   Target unit
     * @return Converted value
     */
    private double convertRegularUnit(double value, Unit fromUnit, Unit toUnit) {
        // Step 1: Convert to base unit
        double baseValue = value * fromUnit.getConversionFactor();
        
        // Step 2: Convert from base unit to target unit
        double result = baseValue / toUnit.getConversionFactor();
        
        return result;
    }

    /**
     * Converts temperature units with special handling for offsets.
     * 
     * TEACHING NOTE: Temperature is special because:
     * - Zero in different scales means different things
     * - Celsius 0° = Fahrenheit 32° (not 0°)
     * - Need both multiplication AND addition
     * 
     * Mathematical explanation:
     * - First add offset to shift to absolute zero reference
     * - Then multiply to adjust scale
     * - Then divide to target scale
     * - Finally subtract offset to shift from absolute zero
     * 
     * Example: 32°F to °C
     * 1. Add offset: 32 + 459.67 = 491.67
     * 2. Multiply: 491.67 × (5/9) = 273.15 K (base)
     * 3. Divide: 273.15 ÷ 1.0 = 273.15 K
     * 4. Subtract offset: 273.15 - 273.15 = 0°C ✓
     * 
     * @param value    Input temperature
     * @param fromUnit Source temperature unit
     * @param toUnit   Target temperature unit
     * @return Converted temperature
     */
    private double convertTemperature(double value, Unit fromUnit, Unit toUnit) {
        // Step 1: Convert to Kelvin (base unit for temperature)
        // Formula: K = (value + offset) × factor
        double kelvin = (value + fromUnit.getOffset()) * fromUnit.getConversionFactor();
        
        // Step 2: Convert from Kelvin to target unit
        // Formula: target = (K ÷ factor) - offset
        double result = (kelvin / toUnit.getConversionFactor()) - toUnit.getOffset();
        
        return result;
    }

    /**
     * Validates if a conversion is possible between two units.
     * Useful for UI to enable/disable convert button.
     * 
     * @param fromUnit Source unit
     * @param toUnit   Target unit
     * @return true if conversion is possible, false otherwise
     */
    public boolean canConvert(Unit fromUnit, Unit toUnit) {
        if (fromUnit == null || toUnit == null) {
            return false;
        }
        return fromUnit.getCategory() == toUnit.getCategory();
    }

    /**
     * Validates if an input value is valid for conversion.
     * 
     * TEACHING NOTE: Input Validation
     * - Fail fast: Check invalid input before processing
     * - User-friendly: Return boolean for UI feedback
     * - Secure: Prevent injection, overflow, invalid data
     * 
     * @param value The value to validate
     * @return true if value is valid, false otherwise
     */
    public boolean isValidInput(double value) {
        // Check for NaN (Not a Number)
        if (Double.isNaN(value)) {
            return false;
        }
        
        // Check for infinity
        if (Double.isInfinite(value)) {
            return false;
        }
        
        // All other values are valid (including negative for temperature)
        return true;
    }

    /**
     * Rounds result to reasonable precision.
     * Prevents showing too many decimal places.
     * 
     * TEACHING NOTE: Precision vs Accuracy
     * - Precision: Number of decimal places shown
     * - Accuracy: How close to true value
     * - Too many decimals confuse users
     * - Too few lose important information
     * - Use significant figures instead of fixed decimals
     * 
     * @param value  The value to round
     * @param digits Number of significant figures
     * @return Rounded value
     */
    public double roundToSignificantFigures(double value, int digits) {
        if (value == 0) {
            return 0;
        }
        
        double magnitude = Math.pow(10, digits - Math.ceil(Math.log10(Math.abs(value))));
        return Math.round(value * magnitude) / magnitude;
    }

    /**
     * Formats a number for display with appropriate precision.
     * 
     * @param value The value to format
     * @return Formatted string
     */
    public String formatValue(double value) {
        return NumberFormatter.format(value);
    }
}
