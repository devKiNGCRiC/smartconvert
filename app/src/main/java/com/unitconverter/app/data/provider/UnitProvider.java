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
        lengthUnits.add(new Unit("Mile", "mi", UnitCategory.LENGTH, 1609.344).tag(Unit.MeasureSystem.IMPERIAL, ""));
        lengthUnits.add(new Unit("Yard", "yd", UnitCategory.LENGTH, 0.9144).tag(Unit.MeasureSystem.IMPERIAL, ""));
        lengthUnits.add(new Unit("Foot", "ft", UnitCategory.LENGTH, 0.3048).tag(Unit.MeasureSystem.IMPERIAL, ""));
        lengthUnits.add(new Unit("Inch", "in", UnitCategory.LENGTH, 0.0254).tag(Unit.MeasureSystem.IMPERIAL, ""));
        
        // Nautical
        lengthUnits.add(new Unit("Nautical Mile", "nmi", UnitCategory.LENGTH, 1852.0).tag(Unit.MeasureSystem.OTHER, "Sea and air navigation"));
        
        // Indian traditional units
        lengthUnits.add(new Unit("Gaj", "gaj", UnitCategory.LENGTH, 0.9144).tag(Unit.MeasureSystem.INDIAN, "Cloth and land; equal to a yard"));
        lengthUnits.add(new Unit("Hath", "hath", UnitCategory.LENGTH, 0.4572).tag(Unit.MeasureSystem.INDIAN, "Cubit, about 18 inches"));
        lengthUnits.add(new Unit("Angul", "angul", UnitCategory.LENGTH, 0.01905).tag(Unit.MeasureSystem.INDIAN, "Finger width, 3/4 inch"));
        lengthUnits.add(new Unit("Kos", "kos", UnitCategory.LENGTH, 3218.688).tag(Unit.MeasureSystem.INDIAN, "Old road distance, about 2 miles"));
        
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
        weightUnits.add(new Unit("Ton (US)", "ton", UnitCategory.WEIGHT, 907.18474).tag(Unit.MeasureSystem.IMPERIAL, ""));
        weightUnits.add(new Unit("Pound", "lb", UnitCategory.WEIGHT, 0.45359237).tag(Unit.MeasureSystem.IMPERIAL, ""));
        weightUnits.add(new Unit("Ounce", "oz", UnitCategory.WEIGHT, 0.028349523125).tag(Unit.MeasureSystem.IMPERIAL, ""));
        
        // Indian traditional units
        weightUnits.add(new Unit("Tola", "tola", UnitCategory.WEIGHT, 0.0116638038).tag(Unit.MeasureSystem.INDIAN, "Gold and silver, 11.66 g"));
        weightUnits.add(new Unit("Ratti", "ratti", UnitCategory.WEIGHT, 0.00012149795625).tag(Unit.MeasureSystem.INDIAN, "Gems and gold, 1/96 tola"));
        weightUnits.add(new Unit("Masha", "masha", UnitCategory.WEIGHT, 0.00097198365).tag(Unit.MeasureSystem.INDIAN, "1/12 tola"));
        weightUnits.add(new Unit("Chhatank", "chhatank", UnitCategory.WEIGHT, 0.058319019).tag(Unit.MeasureSystem.INDIAN, "5 tola, 1/16 seer"));
        weightUnits.add(new Unit("Seer", "seer", UnitCategory.WEIGHT, 0.933104304).tag(Unit.MeasureSystem.INDIAN, "80 tola"));
        weightUnits.add(new Unit("Maund", "maund", UnitCategory.WEIGHT, 37.32417216).tag(Unit.MeasureSystem.INDIAN, "40 seer"));
        
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
        tempUnits.add(new Unit("Fahrenheit", "°F", UnitCategory.TEMPERATURE, 5.0/9.0, 459.67).tag(Unit.MeasureSystem.IMPERIAL, ""));
        
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
        volumeUnits.add(new Unit("Gallon (US)", "gal", UnitCategory.VOLUME, 3.785411784).tag(Unit.MeasureSystem.IMPERIAL, ""));
        volumeUnits.add(new Unit("Quart (US)", "qt", UnitCategory.VOLUME, 0.946352946).tag(Unit.MeasureSystem.IMPERIAL, ""));
        volumeUnits.add(new Unit("Pint (US)", "pt", UnitCategory.VOLUME, 0.473176473).tag(Unit.MeasureSystem.IMPERIAL, ""));
        volumeUnits.add(new Unit("Cup (US)", "cup", UnitCategory.VOLUME, 0.2365882365).tag(Unit.MeasureSystem.IMPERIAL, ""));
        volumeUnits.add(new Unit("Fluid Ounce (US)", "fl oz", UnitCategory.VOLUME, 0.0295735296).tag(Unit.MeasureSystem.IMPERIAL, ""));
        volumeUnits.add(new Unit("Tablespoon", "tbsp", UnitCategory.VOLUME, 0.01478676478).tag(Unit.MeasureSystem.IMPERIAL, ""));
        volumeUnits.add(new Unit("Teaspoon", "tsp", UnitCategory.VOLUME, 0.00492892159).tag(Unit.MeasureSystem.IMPERIAL, ""));
        
        // Imperial System
        volumeUnits.add(new Unit("Gallon (UK)", "gal (UK)", UnitCategory.VOLUME, 4.54609).tag(Unit.MeasureSystem.IMPERIAL, ""));
        volumeUnits.add(new Unit("Cubic Foot", "ft³", UnitCategory.VOLUME, 28.316846592).tag(Unit.MeasureSystem.IMPERIAL, ""));
        volumeUnits.add(new Unit("Cubic Inch", "in³", UnitCategory.VOLUME, 0.016387064).tag(Unit.MeasureSystem.IMPERIAL, ""));
        
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
        areaUnits.add(new Unit("Square Mile", "mi²", UnitCategory.AREA, 2589988.110336).tag(Unit.MeasureSystem.IMPERIAL, ""));
        areaUnits.add(new Unit("Acre", "ac", UnitCategory.AREA, 4046.8564224).tag(Unit.MeasureSystem.IMPERIAL, ""));
        areaUnits.add(new Unit("Square Yard", "yd²", UnitCategory.AREA, 0.83612736).tag(Unit.MeasureSystem.IMPERIAL, ""));
        areaUnits.add(new Unit("Square Foot", "ft²", UnitCategory.AREA, 0.09290304).tag(Unit.MeasureSystem.IMPERIAL, ""));
        areaUnits.add(new Unit("Square Inch", "in²", UnitCategory.AREA, 0.00064516).tag(Unit.MeasureSystem.IMPERIAL, ""));
        
        // Indian traditional units
        areaUnits.add(new Unit("Square Gaj", "sq gaj", UnitCategory.AREA, 0.83612736).tag(Unit.MeasureSystem.INDIAN, "Plot sizes; equal to a square yard"));
        areaUnits.add(new Unit("Marla", "marla", UnitCategory.AREA, 25.29285264).tag(Unit.MeasureSystem.INDIAN, "Punjab, Haryana; 272.25 sq ft"));
        areaUnits.add(new Unit("Kanal", "kanal", UnitCategory.AREA, 505.8570528).tag(Unit.MeasureSystem.INDIAN, "Punjab, Haryana, Himachal; 20 marla"));
        areaUnits.add(new Unit("Cent", "cent", UnitCategory.AREA, 40.468564224).tag(Unit.MeasureSystem.INDIAN, "Kerala, Tamil Nadu, Andhra; 1/100 acre"));
        areaUnits.add(new Unit("Ground", "ground", UnitCategory.AREA, 222.967296).tag(Unit.MeasureSystem.INDIAN, "Chennai; 2,400 sq ft"));
        areaUnits.add(new Unit("Guntha", "guntha", UnitCategory.AREA, 101.17141056).tag(Unit.MeasureSystem.INDIAN, "Maharashtra, Karnataka, Telangana; 1,089 sq ft"));
        areaUnits.add(new Unit("Biswa", "biswa", UnitCategory.AREA, 126.464263).tag(Unit.MeasureSystem.INDIAN, "UP, Bihar; 1/20 bigha"));
        areaUnits.add(new Unit("Bigha (North India)", "bigha N", UnitCategory.AREA, 2529.285264).tag(Unit.MeasureSystem.INDIAN, "UP, Bihar, Rajasthan; 27,225 sq ft"));
        areaUnits.add(new Unit("Bigha (Bengal)", "bigha B", UnitCategory.AREA, 1337.803776).tag(Unit.MeasureSystem.INDIAN, "West Bengal, Assam; 14,400 sq ft"));
        
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
        speedUnits.add(new Unit("Mile per hour", "mph", UnitCategory.SPEED, 0.44704).tag(Unit.MeasureSystem.IMPERIAL, ""));
        speedUnits.add(new Unit("Foot per second", "ft/s", UnitCategory.SPEED, 0.3048).tag(Unit.MeasureSystem.IMPERIAL, ""));
        speedUnits.add(new Unit("Knot", "kn", UnitCategory.SPEED, 0.514444444).tag(Unit.MeasureSystem.OTHER, ""));
        
        unitsByCategory.put(UnitCategory.SPEED, speedUnits);
    }

    /**
     * TIME UNITS
     * Base unit: Second (s) = 1.0
     */
    private void initializeTimeUnits() {
        List<Unit> timeUnits = new ArrayList<>();
        
        timeUnits.add(new Unit("Year", "yr", UnitCategory.TIME, 31536000.0).tag(Unit.MeasureSystem.OTHER, ""));
        timeUnits.add(new Unit("Month", "mo", UnitCategory.TIME, 2628000.0).tag(Unit.MeasureSystem.OTHER, "")); // 30.4167 days
        timeUnits.add(new Unit("Week", "wk", UnitCategory.TIME, 604800.0).tag(Unit.MeasureSystem.OTHER, ""));
        timeUnits.add(new Unit("Day", "d", UnitCategory.TIME, 86400.0).tag(Unit.MeasureSystem.OTHER, ""));
        timeUnits.add(new Unit("Hour", "h", UnitCategory.TIME, 3600.0).tag(Unit.MeasureSystem.OTHER, ""));
        timeUnits.add(new Unit("Minute", "min", UnitCategory.TIME, 60.0).tag(Unit.MeasureSystem.OTHER, ""));
        timeUnits.add(new Unit("Second", "s", UnitCategory.TIME, 1.0).tag(Unit.MeasureSystem.OTHER, ""));
        timeUnits.add(new Unit("Millisecond", "ms", UnitCategory.TIME, 0.001).tag(Unit.MeasureSystem.OTHER, ""));
        timeUnits.add(new Unit("Microsecond", "μs", UnitCategory.TIME, 0.000001).tag(Unit.MeasureSystem.OTHER, ""));
        timeUnits.add(new Unit("Nanosecond", "ns", UnitCategory.TIME, 0.000000001).tag(Unit.MeasureSystem.OTHER, ""));
        
        // Indian traditional units
        timeUnits.add(new Unit("Pal", "pal", UnitCategory.TIME, 24.0).tag(Unit.MeasureSystem.INDIAN, "Traditional; 24 seconds"));
        timeUnits.add(new Unit("Ghadi", "ghadi", UnitCategory.TIME, 1440.0).tag(Unit.MeasureSystem.INDIAN, "Traditional; 24 minutes"));
        timeUnits.add(new Unit("Prahar", "prahar", UnitCategory.TIME, 10800.0).tag(Unit.MeasureSystem.INDIAN, "Traditional watch of 3 hours"));
        
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
        
        storageUnits.add(new Unit("Bit", "bit", UnitCategory.DIGITAL_STORAGE, 0.125).tag(Unit.MeasureSystem.OTHER, ""));
        storageUnits.add(new Unit("Byte", "B", UnitCategory.DIGITAL_STORAGE, 1.0).tag(Unit.MeasureSystem.OTHER, ""));
        storageUnits.add(new Unit("Kilobyte", "KB", UnitCategory.DIGITAL_STORAGE, 1024.0).tag(Unit.MeasureSystem.OTHER, ""));
        storageUnits.add(new Unit("Megabyte", "MB", UnitCategory.DIGITAL_STORAGE, 1048576.0).tag(Unit.MeasureSystem.OTHER, ""));
        storageUnits.add(new Unit("Gigabyte", "GB", UnitCategory.DIGITAL_STORAGE, 1073741824.0).tag(Unit.MeasureSystem.OTHER, ""));
        storageUnits.add(new Unit("Terabyte", "TB", UnitCategory.DIGITAL_STORAGE, 1099511627776.0).tag(Unit.MeasureSystem.OTHER, ""));
        storageUnits.add(new Unit("Petabyte", "PB", UnitCategory.DIGITAL_STORAGE, 1125899906842624.0).tag(Unit.MeasureSystem.OTHER, ""));
        
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
