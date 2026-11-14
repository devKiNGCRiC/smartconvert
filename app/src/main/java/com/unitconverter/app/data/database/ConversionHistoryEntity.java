package com.unitconverter.app.data.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.unitconverter.app.util.NumberFormatter;

/**
 * Room Entity for Conversion History
 * 
 * Stores every conversion the user makes for:
 * - History viewing
 * - Quick repeat conversions
 * - Usage analytics
 * - Favorites marking
 */
@Entity(tableName = "conversion_history")
public class ConversionHistoryEntity {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String category;
    private String fromUnit;
    private String toUnit;
    private double inputValue;
    private double resultValue;
    private long timestamp;
    private boolean isFavorite;

    // Constructor
    public ConversionHistoryEntity(String category, String fromUnit, String toUnit, 
                                   double inputValue, double resultValue, long timestamp) {
        this.category = category;
        this.fromUnit = fromUnit;
        this.toUnit = toUnit;
        this.inputValue = inputValue;
        this.resultValue = resultValue;
        this.timestamp = timestamp;
        this.isFavorite = false;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFromUnit() {
        return fromUnit;
    }

    public void setFromUnit(String fromUnit) {
        this.fromUnit = fromUnit;
    }

    public String getToUnit() {
        return toUnit;
    }

    public void setToUnit(String toUnit) {
        this.toUnit = toUnit;
    }

    public double getInputValue() {
        return inputValue;
    }

    public void setInputValue(double inputValue) {
        this.inputValue = inputValue;
    }

    public double getResultValue() {
        return resultValue;
    }

    public void setResultValue(double resultValue) {
        this.resultValue = resultValue;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    /**
     * Format for display: "5 meters = 16.4 feet"
     */
    public String getDisplayText() {
        return NumberFormatter.format(inputValue) + " " + fromUnit + " = "
                + NumberFormatter.format(resultValue) + " " + toUnit;
    }
}
