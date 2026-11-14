package com.unitconverter.app.domain.model;

/**
 * Represents a single unit of measurement.
 * 
 * TEACHING NOTE: This is a Value Object from Domain-Driven Design (DDD).
 * - Immutable (all fields final, no setters)
 * - Equality based on value, not identity
 * - Represents a concept from the problem domain
 * 
 * Why immutable?
 * - Thread-safe by default
 * - Prevents accidental modifications
 * - Can be safely shared across the app
 * - Easier to reason about
 */
public class Unit {
    private final String name;                // e.g., "Meter", "Kilogram"
    private final String symbol;              // e.g., "m", "kg"
    private final UnitCategory category;      // Which category this unit belongs to
    private final double conversionFactor;    // Multiplier to convert to base unit
    private final double offset;              // Offset for temperature conversions

    /**
     * Standard constructor for most units (no offset needed).
     * 
     * @param name             Full name of the unit (e.g., "Meter")
     * @param symbol           Abbreviation (e.g., "m")
     * @param category         Category this unit belongs to
     * @param conversionFactor Factor to multiply to convert to base unit
     */
    public Unit(String name, String symbol, UnitCategory category, double conversionFactor) {
        this(name, symbol, category, conversionFactor, 0.0);
    }

    /**
     * Full constructor including offset (needed for temperature).
     * 
     * TEACHING NOTE: Constructor overloading allows flexible object creation.
     * The simpler constructor delegates to this one with default offset of 0.
     * 
     * Example: Converting Celsius to Fahrenheit requires both multiplication and addition:
     * F = C × 1.8 + 32
     * 
     * @param name             Full name of the unit
     * @param symbol           Abbreviation
     * @param category         Category this unit belongs to
     * @param conversionFactor Multiplication factor
     * @param offset           Addition offset (used for temperature)
     */
    public Unit(String name, String symbol, UnitCategory category, 
                double conversionFactor, double offset) {
        // Validation: Fail fast with clear error messages
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Unit name cannot be null or empty");
        }
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Unit symbol cannot be null or empty");
        }
        if (category == null) {
            throw new IllegalArgumentException("Unit category cannot be null");
        }
        if (conversionFactor <= 0) {
            throw new IllegalArgumentException("Conversion factor must be positive");
        }

        this.name = name;
        this.symbol = symbol;
        this.category = category;
        this.conversionFactor = conversionFactor;
        this.offset = offset;
    }

    // Getters only (immutable - no setters)
    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public UnitCategory getCategory() {
        return category;
    }

    public double getConversionFactor() {
        return conversionFactor;
    }

    public double getOffset() {
        return offset;
    }

    /**
     * Returns formatted display text for UI (e.g., "Meter (m)").
     */
    public String getDisplayText() {
        return name + " (" + symbol + ")";
    }

    /**
     * Equals and hashCode are crucial for Value Objects.
     * Two units with same properties should be considered equal.
     * 
     * TEACHING NOTE: Always override equals() and hashCode() together.
     * Used by collections (HashMap, HashSet) and for comparison.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Unit unit = (Unit) obj;
        return Double.compare(unit.conversionFactor, conversionFactor) == 0 &&
               Double.compare(unit.offset, offset) == 0 &&
               name.equals(unit.name) &&
               symbol.equals(unit.symbol) &&
               category == unit.category;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + symbol.hashCode();
        result = 31 * result + category.hashCode();
        result = 31 * result + Double.hashCode(conversionFactor);
        result = 31 * result + Double.hashCode(offset);
        return result;
    }

    @Override
    public String toString() {
        return "Unit{" +
                "name='" + name + '\'' +
                ", symbol='" + symbol + '\'' +
                ", category=" + category +
                ", conversionFactor=" + conversionFactor +
                ", offset=" + offset +
                '}';
    }
}
