package com.unitconverter.app.domain.model;

/**
 * Enum representing different categories of units that can be converted.
 * 
 * TEACHING NOTE: Enums are type-safe constants. They prevent bugs by restricting
 * values to a predefined set. Each enum can have properties and methods.
 * 
 * Design Pattern: Type-Safe Enum Pattern
 * - Ensures only valid categories exist
 * - Can add behavior to each category
 * - Compile-time safety vs String constants
 */
public enum UnitCategory {
    LENGTH("Length", "📏", "m", "Convert between meters, feet, inches, etc."),
    WEIGHT("Weight", "⚖️", "kg", "Convert between kilograms, pounds, ounces, etc."),
    TEMPERATURE("Temperature", "🌡️", "°C", "Convert between Celsius, Fahrenheit, Kelvin"),
    VOLUME("Volume", "🧪", "L", "Convert between liters, gallons, cups, etc."),
    AREA("Area", "📐", "m²", "Convert between square meters, acres, hectares, etc."),
    SPEED("Speed", "🚗", "km/h", "Convert between km/h, mph, m/s, etc."),
    TIME("Time", "⏰", "s", "Convert between seconds, minutes, hours, days, etc."),
    DIGITAL_STORAGE("Digital Storage", "💾", "GB", "Convert between bytes, KB, MB, GB, TB");

    private final String displayName;
    private final String icon;
    private final String symbol;
    private final String description;

    /**
     * Constructor for UnitCategory enum.
     * 
     * @param displayName User-friendly name shown in UI
     * @param icon        Emoji icon for visual identification
     * @param symbol      Representative unit symbol shown on the category tile
     * @param description Brief explanation of what units are included
     */
    UnitCategory(String displayName, String icon, String symbol, String description) {
        this.displayName = displayName;
        this.icon = icon;
        this.symbol = symbol;
        this.description = description;
    }

    // Getters with documentation
    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    /** A representative unit symbol (e.g. "km/h"), used as the category's visual mark. */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Display name for a stored enum name such as "DIGITAL_STORAGE";
     * falls back to the raw value if it no longer matches a category.
     */
    public static String displayNameOf(String name) {
        try {
            return valueOf(name).displayName;
        } catch (IllegalArgumentException | NullPointerException e) {
            return name;
        }
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns a formatted string combining icon and display name.
     * Useful for UI elements like buttons and titles.
     */
    public String getFormattedName() {
        return icon + " " + displayName;
    }
}
