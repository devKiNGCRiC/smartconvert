package com.unitconverter.app.domain.model;

import java.util.Locale;

/**
 * Represents the result of a unit conversion operation.
 * 
 * TEACHING NOTE: This is another Value Object that encapsulates conversion data.
 * It's immutable and contains all information about a conversion.
 * 
 * Why create a separate class instead of just returning a double?
 * - Type safety: Can't accidentally mix up conversion results
 * - Rich information: Stores source, target, and values together
 * - Extensible: Easy to add features like history, formatting, etc.
 * - Testable: Clear contract for what conversion returns
 */
public class ConversionResult {
    private final double inputValue;
    private final double outputValue;
    private final Unit fromUnit;
    private final Unit toUnit;
    private final long timestamp;  // When conversion was performed

    /**
     * Creates a new conversion result.
     * 
     * @param inputValue  The original value entered by user
     * @param outputValue The converted value
     * @param fromUnit    The source unit
     * @param toUnit      The target unit
     */
    public ConversionResult(double inputValue, double outputValue, 
                           Unit fromUnit, Unit toUnit) {
        if (fromUnit == null || toUnit == null) {
            throw new IllegalArgumentException("Units cannot be null");
        }
        if (fromUnit.getCategory() != toUnit.getCategory()) {
            throw new IllegalArgumentException(
                "Cannot convert between different categories: " + 
                fromUnit.getCategory() + " and " + toUnit.getCategory()
            );
        }

        this.inputValue = inputValue;
        this.outputValue = outputValue;
        this.fromUnit = fromUnit;
        this.toUnit = toUnit;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    public double getInputValue() {
        return inputValue;
    }

    public double getOutputValue() {
        return outputValue;
    }

    public Unit getFromUnit() {
        return fromUnit;
    }

    public Unit getToUnit() {
        return toUnit;
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Returns a human-readable string representation of the conversion.
     * Example: "5.0 m = 16.404 ft"
     * 
     * TEACHING NOTE: Formatting logic belongs in the domain model when it's
     * part of the business logic. For UI-specific formatting, use a separate
     * formatter class in the presentation layer.
     */
    public String getFormattedConversion() {
        return String.format(Locale.US, "%.4g %s = %.4g %s",
            inputValue, fromUnit.getSymbol(),
            outputValue, toUnit.getSymbol()
        );
    }

    /**
     * Returns a detailed string with full unit names.
     * Example: "5.0 Meter (m) = 16.404 Feet (ft)"
     */
    public String getDetailedConversion() {
        return String.format(Locale.US, "%.4g %s = %.4g %s",
            inputValue, fromUnit.getDisplayText(),
            outputValue, toUnit.getDisplayText()
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ConversionResult that = (ConversionResult) obj;
        return Double.compare(that.inputValue, inputValue) == 0 &&
               Double.compare(that.outputValue, outputValue) == 0 &&
               fromUnit.equals(that.fromUnit) &&
               toUnit.equals(that.toUnit);
    }

    @Override
    public int hashCode() {
        int result = Double.hashCode(inputValue);
        result = 31 * result + Double.hashCode(outputValue);
        result = 31 * result + fromUnit.hashCode();
        result = 31 * result + toUnit.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ConversionResult{" +
                "inputValue=" + inputValue +
                ", outputValue=" + outputValue +
                ", fromUnit=" + fromUnit.getName() +
                ", toUnit=" + toUnit.getName() +
                ", timestamp=" + timestamp +
                '}';
    }
}
