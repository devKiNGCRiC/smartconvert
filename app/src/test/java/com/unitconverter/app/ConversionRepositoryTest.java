package com.unitconverter.app;

import com.unitconverter.app.data.repository.ConversionRepository;
import com.unitconverter.app.domain.model.ConversionResult;
import com.unitconverter.app.domain.model.Unit;
import com.unitconverter.app.domain.model.UnitCategory;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit Tests for ConversionRepository
 * 
 * TEACHING NOTE: Unit Testing Best Practices
 * 
 * Why Write Tests?
 * 1. Catch bugs early (before they reach users)
 * 2. Prevent regression (old bugs coming back)
 * 3. Document behavior (tests show how code should work)
 * 4. Enable refactoring (change code safely)
 * 5. Build confidence (know your code works)
 * 
 * Test Structure: AAA Pattern
 * - Arrange: Set up test data
 * - Act: Execute the code being tested
 * - Assert: Verify the result
 * 
 * Naming Convention: methodName_condition_expectedResult
 * Example: convert_lengthUnits_returnsCorrectValue
 * 
 * What to Test:
 * ✓ Normal cases (typical usage)
 * ✓ Edge cases (boundary values)
 * ✓ Error cases (invalid input)
 * ✓ Special cases (temperature with offset)
 * 
 * JUnit Annotations:
 * @Before: Runs before each test (setup)
 * @Test: Marks method as a test
 * @After: Runs after each test (cleanup)
 */
public class ConversionRepositoryTest {

    private ConversionRepository repository;
    
    // Test units for each category
    private Unit meter;
    private Unit kilometer;
    private Unit foot;
    private Unit kilogram;
    private Unit pound;
    private Unit celsius;
    private Unit fahrenheit;
    private Unit kelvin;

    /**
     * Setup method runs before each test.
     * Creates test instances to avoid repetition.
     * 
     * TEACHING NOTE: @Before
     * - Runs before EACH test method
     * - Fresh instance for each test (isolation)
     * - Tests don't affect each other
     */
    @Before
    public void setUp() {
        repository = ConversionRepository.getInstance();
        
        // Create test units
        meter = new Unit("Meter", "m", UnitCategory.LENGTH, 1.0);
        kilometer = new Unit("Kilometer", "km", UnitCategory.LENGTH, 1000.0);
        foot = new Unit("Foot", "ft", UnitCategory.LENGTH, 0.3048);
        
        kilogram = new Unit("Kilogram", "kg", UnitCategory.WEIGHT, 1.0);
        pound = new Unit("Pound", "lb", UnitCategory.WEIGHT, 0.45359237);
        
        kelvin = new Unit("Kelvin", "K", UnitCategory.TEMPERATURE, 1.0, 0.0);
        celsius = new Unit("Celsius", "°C", UnitCategory.TEMPERATURE, 1.0, 273.15);
        fahrenheit = new Unit("Fahrenheit", "°F", UnitCategory.TEMPERATURE, 5.0/9.0, 459.67);
    }

    // ========== BASIC CONVERSION TESTS ==========

    /**
     * Tests simple length conversion.
     * Example: 1000 meters = 1 kilometer
     */
    @Test
    public void convert_metersToKilometers_returnsCorrectValue() {
        // Arrange
        double inputValue = 1000.0;
        
        // Act
        ConversionResult result = repository.convert(inputValue, meter, kilometer);
        
        // Assert
        assertEquals(1.0, result.getOutputValue(), 0.0001);
        assertEquals(meter, result.getFromUnit());
        assertEquals(kilometer, result.getToUnit());
    }

    /**
     * Tests reverse length conversion.
     * Example: 1 kilometer = 1000 meters
     */
    @Test
    public void convert_kilometersToMeters_returnsCorrectValue() {
        // Arrange
        double inputValue = 1.0;
        
        // Act
        ConversionResult result = repository.convert(inputValue, kilometer, meter);
        
        // Assert
        assertEquals(1000.0, result.getOutputValue(), 0.0001);
    }

    /**
     * Tests imperial to metric conversion.
     * Example: 10 feet ≈ 3.048 meters
     */
    @Test
    public void convert_feetToMeters_returnsCorrectValue() {
        // Arrange
        double inputValue = 10.0;
        
        // Act
        ConversionResult result = repository.convert(inputValue, foot, meter);
        
        // Assert
        assertEquals(3.048, result.getOutputValue(), 0.0001);
    }

    /**
     * Tests weight conversion.
     * Example: 1 pound ≈ 0.4536 kilograms
     */
    @Test
    public void convert_poundsToKilograms_returnsCorrectValue() {
        // Arrange
        double inputValue = 1.0;
        
        // Act
        ConversionResult result = repository.convert(inputValue, pound, kilogram);
        
        // Assert
        assertEquals(0.45359237, result.getOutputValue(), 0.0001);
    }

    // ========== TEMPERATURE CONVERSION TESTS ==========

    /**
     * Tests freezing point of water.
     * 0°C = 32°F = 273.15K
     */
    @Test
    public void convert_celsiusToFahrenheit_freezingPoint_returnsCorrectValue() {
        // Arrange: Freezing point of water
        double inputValue = 0.0;
        
        // Act
        ConversionResult result = repository.convert(inputValue, celsius, fahrenheit);
        
        // Assert
        assertEquals(32.0, result.getOutputValue(), 0.01);
    }

    /**
     * Tests boiling point of water.
     * 100°C = 212°F
     */
    @Test
    public void convert_celsiusToFahrenheit_boilingPoint_returnsCorrectValue() {
        // Arrange: Boiling point of water
        double inputValue = 100.0;
        
        // Act
        ConversionResult result = repository.convert(inputValue, celsius, fahrenheit);
        
        // Assert
        assertEquals(212.0, result.getOutputValue(), 0.01);
    }

    /**
     * Tests reverse temperature conversion.
     * 32°F = 0°C
     */
    @Test
    public void convert_fahrenheitToCelsius_freezingPoint_returnsCorrectValue() {
        // Arrange
        double inputValue = 32.0;
        
        // Act
        ConversionResult result = repository.convert(inputValue, fahrenheit, celsius);
        
        // Assert
        assertEquals(0.0, result.getOutputValue(), 0.01);
    }

    /**
     * Tests Celsius to Kelvin.
     * 0°C = 273.15K (absolute zero offset)
     */
    @Test
    public void convert_celsiusToKelvin_returnsCorrectValue() {
        // Arrange
        double inputValue = 0.0;
        
        // Act
        ConversionResult result = repository.convert(inputValue, celsius, kelvin);
        
        // Assert
        assertEquals(273.15, result.getOutputValue(), 0.01);
    }

    /**
     * Tests negative temperature.
     * -40°C = -40°F (they're equal at this point!)
     */
    @Test
    public void convert_negativeTemperature_returnsCorrectValue() {
        // Arrange: The magical point where Celsius and Fahrenheit are equal
        double inputValue = -40.0;
        
        // Act
        ConversionResult result = repository.convert(inputValue, celsius, fahrenheit);
        
        // Assert
        assertEquals(-40.0, result.getOutputValue(), 0.01);
    }

    // ========== EDGE CASE TESTS ==========

    /**
     * Tests converting zero.
     * 0 of any unit should convert to 0 (except temperature).
     */
    @Test
    public void convert_zeroValue_returnsZero() {
        // Arrange
        double inputValue = 0.0;
        
        // Act
        ConversionResult result = repository.convert(inputValue, meter, foot);
        
        // Assert
        assertEquals(0.0, result.getOutputValue(), 0.0001);
    }

    /**
     * Tests converting to same unit.
     * Should return same value.
     */
    @Test
    public void convert_sameUnit_returnsSameValue() {
        // Arrange
        double inputValue = 42.0;
        
        // Act
        ConversionResult result = repository.convert(inputValue, meter, meter);
        
        // Assert
        assertEquals(42.0, result.getOutputValue(), 0.0001);
    }

    /**
     * Tests large numbers.
     * Ensures no overflow or precision issues.
     */
    @Test
    public void convert_largeNumber_handlesCorrectly() {
        // Arrange
        double inputValue = 1000000.0;
        
        // Act
        ConversionResult result = repository.convert(inputValue, meter, kilometer);
        
        // Assert
        assertEquals(1000.0, result.getOutputValue(), 0.01);
    }

    /**
     * Tests small numbers.
     * Ensures precision is maintained.
     */
    @Test
    public void convert_smallNumber_handlesCorrectly() {
        // Arrange
        double inputValue = 0.001;
        
        // Act
        ConversionResult result = repository.convert(inputValue, kilometer, meter);
        
        // Assert
        assertEquals(1.0, result.getOutputValue(), 0.0001);
    }

    /**
     * Tests negative numbers (valid for temperature).
     */
    @Test
    public void convert_negativeNumber_handlesCorrectly() {
        // Arrange
        double inputValue = -10.0;
        
        // Act
        ConversionResult result = repository.convert(inputValue, meter, foot);
        
        // Assert
        assertTrue(result.getOutputValue() < 0);
    }

    // ========== ERROR CASE TESTS ==========

    /**
     * Tests null source unit.
     * Should throw IllegalArgumentException.
     * 
     * TEACHING NOTE: Testing Exceptions
     * expected = ExceptionClass.class tells JUnit this is expected
     */
    @Test(expected = IllegalArgumentException.class)
    public void convert_nullFromUnit_throwsException() {
        repository.convert(100.0, null, meter);
    }

    /**
     * Tests null target unit.
     */
    @Test(expected = IllegalArgumentException.class)
    public void convert_nullToUnit_throwsException() {
        repository.convert(100.0, meter, null);
    }

    /**
     * Tests incompatible units (different categories).
     * Cannot convert length to weight!
     */
    @Test(expected = IllegalArgumentException.class)
    public void convert_incompatibleUnits_throwsException() {
        repository.convert(100.0, meter, kilogram);
    }

    // ========== VALIDATION TESTS ==========

    /**
     * Tests input validation for valid numbers.
     */
    @Test
    public void isValidInput_normalNumber_returnsTrue() {
        assertTrue(repository.isValidInput(42.0));
        assertTrue(repository.isValidInput(0.0));
        assertTrue(repository.isValidInput(-10.5));
    }

    /**
     * Tests input validation for NaN.
     */
    @Test
    public void isValidInput_nan_returnsFalse() {
        assertFalse(repository.isValidInput(Double.NaN));
    }

    /**
     * Tests input validation for infinity.
     */
    @Test
    public void isValidInput_infinity_returnsFalse() {
        assertFalse(repository.isValidInput(Double.POSITIVE_INFINITY));
        assertFalse(repository.isValidInput(Double.NEGATIVE_INFINITY));
    }

    /**
     * Tests canConvert for compatible units.
     */
    @Test
    public void canConvert_compatibleUnits_returnsTrue() {
        assertTrue(repository.canConvert(meter, kilometer));
        assertTrue(repository.canConvert(celsius, fahrenheit));
    }

    /**
     * Tests canConvert for incompatible units.
     */
    @Test
    public void canConvert_incompatibleUnits_returnsFalse() {
        assertFalse(repository.canConvert(meter, kilogram));
    }

    /**
     * Tests canConvert with null units.
     */
    @Test
    public void canConvert_nullUnits_returnsFalse() {
        assertFalse(repository.canConvert(null, meter));
        assertFalse(repository.canConvert(meter, null));
    }

    // ========== FORMATTING TESTS ==========

    /**
     * Tests value formatting.
     */
    @Test
    public void formatValue_normalNumber_formatsCorrectly() {
        String formatted = repository.formatValue(123.456789);
        assertNotNull(formatted);
        assertFalse(formatted.isEmpty());
    }

    /**
     * Tests rounding to significant figures.
     */
    @Test
    public void roundToSignificantFigures_roundsCorrectly() {
        double result = repository.roundToSignificantFigures(123.456789, 4);
        assertEquals(123.5, result, 0.1);
    }

    /**
     * Tests rounding zero.
     */
    @Test
    public void roundToSignificantFigures_zero_returnsZero() {
        double result = repository.roundToSignificantFigures(0.0, 4);
        assertEquals(0.0, result, 0.0001);
    }
}
