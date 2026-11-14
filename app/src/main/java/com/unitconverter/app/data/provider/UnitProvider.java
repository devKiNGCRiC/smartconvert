package com.unitconverter.app.data.provider;

import com.unitconverter.app.domain.model.Unit;
import com.unitconverter.app.domain.model.UnitCategory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provider class that supplies all available units for conversion.
 * 
 * TEACHING NOTE: This is an implementation of the Factory Pattern.
 * - Centralizes unit creation in one place
 * - Easy to add new units without modifying other code
 * - Follows Single Responsibility Principle (SRP)
 * - Makes testing easier (can mock this provider)
 * 
 * Design Pattern: Factory Pattern + Singleton Pattern
 * - Factory: Creates complex objects (Units)
 * - Singleton: Only one instance needed (use getInstance())
 */
public class UnitProvider {
    private static UnitProvider instance;
    private final Map<UnitCategory, List<Unit>> unitsByCategory;

    /**
     * Private constructor implements Singleton pattern.
     * Only one UnitProvider instance should exist in the app.
     */
    private UnitProvider() {
        unitsByCategory = new HashMap<>();
        initializeUnits();
    }

    /**
     * Singleton getInstance() method.
     * Thread-safe lazy initialization.
     * 
     * TEACHING NOTE: synchronized ensures thread safety.
     * Multiple threads calling this simultaneously won't create multiple instances.
     */
    public static synchronized UnitProvider getInstance() {
        if (instance == null) {
            instance = new UnitProvider();
        }
        return instance;
    }

    /**
     * Initializes all units with their conversion factors.
     * 
     * TEACHING NOTE: Conversion factors are relative to a "base unit":
     * - LENGTH: meter (m) is base = 1.0
     * - WEIGHT: kilogram (kg) is base = 1.0
     * - etc.
     * 
     * Formula: toBaseUnit = value × conversionFactor
     * Example: 5 feet × 0.3048 = 1.524 meters
     */
    private void initializeUnits() {
        initializeLengthUnits();
        initializeWeightUnits();
        initializeTemperatureUnits();
        initializeVolumeUnits();
        initializeAreaUnits();
        initializeSpeedUnits();
        initializeTimeUnits();
        initializeDigitalStorageUnits();
    }

    /**
     * LENGTH UNITS
     * Base unit: Meter (m) = 1.0
     */
    private void initializeLengthUnits() {
        List<Unit> lengthUnits = new ArrayList<>();
        
        // Metric System
        lengthUnits.add(new Unit("Kilometer", "km", UnitCategory.LENGTH, 1000.0));
        lengthUnits.add(new Unit("Meter", "m", UnitCategory.LENGTH, 1.0));
        lengthUnits.add(new Unit("Centimeter", "cm", UnitCategory.LENGTH, 0.01));
        lengthUnits.add(new Unit("Millimeter", "mm", UnitCategory.LENGTH, 0.001));
        lengthUnits.add(new Unit("Micrometer", "μm", UnitCategory.LENGTH, 0.000001));
        lengthUnits.add(new Unit("Nanometer", "nm", UnitCategory.LENGTH, 0.000000001));
        
        // Imperial System
        lengthUnits.add(new Unit("Mile", "mi", UnitCategory.LENGTH, 1609.344));
        lengthUnits.add(new Unit("Yard", "yd", UnitCategory.LENGTH, 0.9144));
        lengthUnits.add(new Unit("Foot", "ft", UnitCategory.LENGTH, 0.3048));
        lengthUnits.add(new Unit("Inch", "in", UnitCategory.LENGTH, 0.0254));
        
        // Nautical
        lengthUnits.add(new Unit("Nautical Mile", "nmi", UnitCategory.LENGTH, 1852.0));
        
        unitsByCategory.put(UnitCategory.LENGTH, lengthUnits);
    }

    /**
     * WEIGHT UNITS
     * Base unit: Kilogram (kg) = 1.0
     */
    private void initializeWeightUnits() {
        List<Unit> weightUnits = new ArrayList<>();
        
        // Metric System
        weightUnits.add(new Unit("Metric Ton", "t", UnitCategory.WEIGHT, 1000.0));
        weightUnits.add(new Unit("Kilogram", "kg", UnitCategory.WEIGHT, 1.0));
        weightUnits.add(new Unit("Gram", "g", UnitCategory.WEIGHT, 0.001));
        weightUnits.add(new Unit("Milligram", "mg", UnitCategory.WEIGHT, 0.000001));
        weightUnits.add(new Unit("Microgram", "μg", UnitCategory.WEIGHT, 0.000000001));
        
        // Imperial System
        weightUnits.add(new Unit("Ton (US)", "ton", UnitCategory.WEIGHT, 907.18474));
        weightUnits.add(new Unit("Pound", "lb", UnitCategory.WEIGHT, 0.45359237));
        weightUnits.add(new Unit("Ounce", "oz", UnitCategory.WEIGHT, 0.028349523125));
        
        unitsByCategory.put(UnitCategory.WEIGHT, weightUnits);
    }

    /**
     * TEMPERATURE UNITS
     * Base unit: Kelvin (K) = 1.0
     * 
     * TEACHING NOTE: Temperature is special!
     * - Uses both multiplication AND addition (offset)
     * - Formula: K = (°C + 273.15), K = (°F + 459.67) × 5/9
     * - Can't use simple multiplication like other units
     */
    private void initializeTemperatureUnits() {
        List<Unit> tempUnits = new ArrayList<>();
        
        // Kelvin is base (absolute zero = 0 K)
        tempUnits.add(new Unit("Kelvin", "K", UnitCategory.TEMPERATURE, 1.0, 0.0));
        
        // Celsius: K = C + 273.15, so C = K - 273.15
        tempUnits.add(new Unit("Celsius", "°C", UnitCategory.TEMPERATURE, 1.0, 273.15));
        
        // Fahrenheit: K = (F + 459.67) × 5/9
        tempUnits.add(new Unit("Fahrenheit", "°F", UnitCategory.TEMPERATURE, 5.0/9.0, 459.67));
        
        unitsByCategory.put(UnitCategory.TEMPERATURE, tempUnits);
    }

    /**
     * VOLUME UNITS
     * Base unit: Liter (L) = 1.0
     */
    private void initializeVolumeUnits() {
        List<Unit> volumeUnits = new ArrayList<>();
        
        // Metric System
        volumeUnits.add(new Unit("Cubic Meter", "m³", UnitCategory.VOLUME, 1000.0));
        volumeUnits.add(new Unit("Liter", "L", UnitCategory.VOLUME, 1.0));
        volumeUnits.add(new Unit("Milliliter", "mL", UnitCategory.VOLUME, 0.001));
        volumeUnits.add(new Unit("Cubic Centimeter", "cm³", UnitCategory.VOLUME, 0.001));
        
        // US System
        volumeUnits.add(new Unit("Gallon (US)", "gal", UnitCategory.VOLUME, 3.785411784));
        volumeUnits.add(new Unit("Quart (US)", "qt", UnitCategory.VOLUME, 0.946352946));
        volumeUnits.add(new Unit("Pint (US)", "pt", UnitCategory.VOLUME, 0.473176473));
        volumeUnits.add(new Unit("Cup (US)", "cup", UnitCategory.VOLUME, 0.2365882365));
        volumeUnits.add(new Unit("Fluid Ounce (US)", "fl oz", UnitCategory.VOLUME, 0.0295735296));
        volumeUnits.add(new Unit("Tablespoon", "tbsp", UnitCategory.VOLUME, 0.01478676478));
        volumeUnits.add(new Unit("Teaspoon", "tsp", UnitCategory.VOLUME, 0.00492892159));
        
        // Imperial System
        volumeUnits.add(new Unit("Gallon (UK)", "gal (UK)", UnitCategory.VOLUME, 4.54609));
        volumeUnits.add(new Unit("Cubic Foot", "ft³", UnitCategory.VOLUME, 28.316846592));
        volumeUnits.add(new Unit("Cubic Inch", "in³", UnitCategory.VOLUME, 0.016387064));
        
        unitsByCategory.put(UnitCategory.VOLUME, volumeUnits);
    }

    /**
     * AREA UNITS
     * Base unit: Square Meter (m²) = 1.0
     */
    private void initializeAreaUnits() {
        List<Unit> areaUnits = new ArrayList<>();
        
        // Metric System
        areaUnits.add(new Unit("Square Kilometer", "km²", UnitCategory.AREA, 1000000.0));
        areaUnits.add(new Unit("Hectare", "ha", UnitCategory.AREA, 10000.0));
        areaUnits.add(new Unit("Square Meter", "m²", UnitCategory.AREA, 1.0));
        areaUnits.add(new Unit("Square Centimeter", "cm²", UnitCategory.AREA, 0.0001));
        areaUnits.add(new Unit("Square Millimeter", "mm²", UnitCategory.AREA, 0.000001));
        
        // Imperial System
        areaUnits.add(new Unit("Square Mile", "mi²", UnitCategory.AREA, 2589988.110336));
        areaUnits.add(new Unit("Acre", "ac", UnitCategory.AREA, 4046.8564224));
        areaUnits.add(new Unit("Square Yard", "yd²", UnitCategory.AREA, 0.83612736));
        areaUnits.add(new Unit("Square Foot", "ft²", UnitCategory.AREA, 0.09290304));
        areaUnits.add(new Unit("Square Inch", "in²", UnitCategory.AREA, 0.00064516));
        
        unitsByCategory.put(UnitCategory.AREA, areaUnits);
    }

    /**
     * SPEED UNITS
     * Base unit: Meter per second (m/s) = 1.0
     */
    private void initializeSpeedUnits() {
        List<Unit> speedUnits = new ArrayList<>();
        
        speedUnits.add(new Unit("Kilometer per hour", "km/h", UnitCategory.SPEED, 0.277777778));
        speedUnits.add(new Unit("Meter per second", "m/s", UnitCategory.SPEED, 1.0));
        speedUnits.add(new Unit("Mile per hour", "mph", UnitCategory.SPEED, 0.44704));
        speedUnits.add(new Unit("Foot per second", "ft/s", UnitCategory.SPEED, 0.3048));
        speedUnits.add(new Unit("Knot", "kn", UnitCategory.SPEED, 0.514444444));
        
        unitsByCategory.put(UnitCategory.SPEED, speedUnits);
    }

    /**
     * TIME UNITS
     * Base unit: Second (s) = 1.0
     */
    private void initializeTimeUnits() {
        List<Unit> timeUnits = new ArrayList<>();
        
        timeUnits.add(new Unit("Year", "yr", UnitCategory.TIME, 31536000.0));
        timeUnits.add(new Unit("Month", "mo", UnitCategory.TIME, 2628000.0)); // 30.4167 days
        timeUnits.add(new Unit("Week", "wk", UnitCategory.TIME, 604800.0));
        timeUnits.add(new Unit("Day", "d", UnitCategory.TIME, 86400.0));
        timeUnits.add(new Unit("Hour", "h", UnitCategory.TIME, 3600.0));
        timeUnits.add(new Unit("Minute", "min", UnitCategory.TIME, 60.0));
        timeUnits.add(new Unit("Second", "s", UnitCategory.TIME, 1.0));
        timeUnits.add(new Unit("Millisecond", "ms", UnitCategory.TIME, 0.001));
        timeUnits.add(new Unit("Microsecond", "μs", UnitCategory.TIME, 0.000001));
        timeUnits.add(new Unit("Nanosecond", "ns", UnitCategory.TIME, 0.000000001));
        
        unitsByCategory.put(UnitCategory.TIME, timeUnits);
    }

    /**
     * DIGITAL STORAGE UNITS
     * Base unit: Byte (B) = 1.0
     * 
     * TEACHING NOTE: Using binary (1024) not decimal (1000) for computer storage.
     * 1 KB = 1024 bytes (not 1000)
     */
    private void initializeDigitalStorageUnits() {
        List<Unit> storageUnits = new ArrayList<>();
        
        storageUnits.add(new Unit("Bit", "bit", UnitCategory.DIGITAL_STORAGE, 0.125));
        storageUnits.add(new Unit("Byte", "B", UnitCategory.DIGITAL_STORAGE, 1.0));
        storageUnits.add(new Unit("Kilobyte", "KB", UnitCategory.DIGITAL_STORAGE, 1024.0));
        storageUnits.add(new Unit("Megabyte", "MB", UnitCategory.DIGITAL_STORAGE, 1048576.0));
        storageUnits.add(new Unit("Gigabyte", "GB", UnitCategory.DIGITAL_STORAGE, 1073741824.0));
        storageUnits.add(new Unit("Terabyte", "TB", UnitCategory.DIGITAL_STORAGE, 1099511627776.0));
        storageUnits.add(new Unit("Petabyte", "PB", UnitCategory.DIGITAL_STORAGE, 1125899906842624.0));
        
        unitsByCategory.put(UnitCategory.DIGITAL_STORAGE, storageUnits);
    }

    /**
     * Returns all units for a specific category.
     * Returns unmodifiable list to prevent external modification.
     * 
     * @param category The unit category
     * @return List of units in that category
     */
    public List<Unit> getUnitsForCategory(UnitCategory category) {
        List<Unit> units = unitsByCategory.get(category);
        return units != null ? Collections.unmodifiableList(units) : new ArrayList<>();
    }

    /**
     * Returns all available categories.
     */
    public UnitCategory[] getAllCategories() {
        return UnitCategory.values();
    }

    /**
     * Finds a unit by name within a category.
     * Useful for restoring saved preferences.
     * 
     * @param category  The category to search in
     * @param unitName  Name of the unit to find
     * @return The Unit if found, null otherwise
     */
    public Unit findUnit(UnitCategory category, String unitName) {
        List<Unit> units = getUnitsForCategory(category);
        for (Unit unit : units) {
            if (unit.getName().equals(unitName)) {
                return unit;
            }
        }
        return null;
    }
}
