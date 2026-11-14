package com.unitconverter.app;

import com.unitconverter.app.data.repository.ConversionRepository;
import com.unitconverter.app.domain.model.ConversionResult;
import com.unitconverter.app.domain.model.Unit;
import com.unitconverter.app.domain.model.UnitCategory;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for ConversionRepository
 * 
 * TEACHING NOTE: Unit Testing Best Practices
 * 
 * What to test:
 * ✓ Business logic (conversion algorithms)
 * ✓ Edge cases (zero, negative, very large numbers)
 * ✓ Error conditions (invalid input, mismatched units)
 * ✓ Boundary values (exactly at limits)
 * 
 * Test Structure (AAA Pattern):
 * 1. Arrange: Set up test data
 * 2. Act: Execute the code being tested
 * 3. Assert: Verify the result
 */
public class ExampleUnitTest {
    
    private ConversionRepository repository;

    @Before
    public void setUp() {
        repository = ConversionRepository.getInstance();
    }

    // ========== LENGTH CONVERSIONS ==========

    @Test
    public void testMeterToKilometer() {
        Unit meter = new Unit("Meter", "m", UnitCategory.LENGTH, 1.0);
        Unit kilometer = new Unit("Kilometer", "km", UnitCategory.LENGTH, 1000.0);
        
        ConversionResult result = repository.convert(1000, meter, kilometer);
        
        assertEquals(1.0, result.getOutputValue(), 0.0001);
    }

    @Test
    public void testMeterToFeet() {
        Unit meter = new Unit("Meter", "m", UnitCategory.LENGTH, 1.0);
        Unit feet = new Unit("Foot", "ft", UnitCategory.LENGTH, 0.3048);
        
        ConversionResult result = repository.convert(1, meter, feet);
        
        // 1 meter = 3.28084 feet
        assertEquals(3.28084, result.getOutputValue(), 0.0001);
    }

    // ========== TEMPERATURE CONVERSIONS ==========

    @Test
    public void testCelsiusToFahrenheit() {
        Unit celsius = new Unit("Celsius", "°C", UnitCategory.TEMPERATURE, 1.0, 273.15);
        Unit fahrenheit = new Unit("Fahrenheit", "°F", UnitCategory.TEMPERATURE, 5.0/9.0, 459.67);
        
        ConversionResult result = repository.convert(0, celsius, fahrenheit);
        
        // 0°C = 32°F
        assertEquals(32.0, result.getOutputValue(), 0.0001);
    }

    @Test
    public void testFahrenheitToCelsius() {
        Unit fahrenheit = new Unit("Fahrenheit", "°F", UnitCategory.TEMPERATURE, 5.0/9.0, 459.67);
        Unit celsius = new Unit("Celsius", "°C", UnitCategory.TEMPERATURE, 1.0, 273.15);
        
        ConversionResult result = repository.convert(32, fahrenheit, celsius);
        
        // 32°F = 0°C
        assertEquals(0.0, result.getOutputValue(), 0.0001);
    }

    // ========== ERROR CASES ==========

    @Test(expected = IllegalArgumentException.class)
    public void testIncompatibleCategories() {
        Unit meter = new Unit("Meter", "m", UnitCategory.LENGTH, 1.0);
        Unit kilogram = new Unit("Kilogram", "kg", UnitCategory.WEIGHT, 1.0);
        
        repository.convert(5, meter, kilogram);
    }

    // ========== VALIDATION TESTS ==========

    @Test
    public void testIsValidInput_ValidNumber() {
        assertTrue(repository.isValidInput(5.0));
        assertTrue(repository.isValidInput(0.0));
        assertTrue(repository.isValidInput(-5.0));
    }

    @Test
    public void testIsValidInput_Invalid() {
        assertFalse(repository.isValidInput(Double.NaN));
        assertFalse(repository.isValidInput(Double.POSITIVE_INFINITY));
    }
}