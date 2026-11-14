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
    LENGTH("Length", "📏", "Convert between meters, feet, inches, etc."),
    WEIGHT("Weight", "⚖️", "Convert between kilograms, pounds, ounces, etc."),
    TEMPERATURE("Temperature", "🌡️", "Convert between Celsius, Fahrenheit, Kelvin"),
    VOLUME("Volume", "🧪", "Convert between liters, gallons, cups, etc."),
    AREA("Area", "📐", "Convert between square meters, acres, hectares, etc."),
    SPEED("Speed", "🚗", "Convert between km/h, mph, m/s, etc."),
    TIME("Time", "⏰", "Convert between seconds, minutes, hours, days, etc."),
    DIGITAL_STORAGE("Digital Storage", "💾", "Convert between bytes, KB, MB, GB, TB");

    private final String displayName;
    private final String icon;
    private final String description;

    /**
     * Constructor for UnitCategory enum.
     * 
     * @param displayName User-friendly name shown in UI
     * @param icon        Emoji icon for visual identification
     * @param description Brief explanation of what units are included
     */
    UnitCategory(String displayName, String icon, String description) {
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
    }

    // Getters with documentation
    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
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
