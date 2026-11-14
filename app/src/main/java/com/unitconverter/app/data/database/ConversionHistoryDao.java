package com.unitconverter.app.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object (DAO) for Conversion History
 * 
 * Defines database operations for history management
 */
@Dao
public interface ConversionHistoryDao {
    
    /**
     * Insert a new conversion into history
     */
    @Insert
    long insert(ConversionHistoryEntity history);
    
    /**
     * Update existing history entry (for favorites toggle)
     */
    @Update
    void update(ConversionHistoryEntity history);
    
    /**
     * Delete a history entry
     */
    @Delete
    void delete(ConversionHistoryEntity history);
    
    /**
     * Get all history ordered by most recent first
     */
    @Query("SELECT * FROM conversion_history ORDER BY timestamp DESC")
    LiveData<List<ConversionHistoryEntity>> getAllHistory();
    
    /**
     * Get only favorites
     */
    @Query("SELECT * FROM conversion_history WHERE isFavorite = 1 ORDER BY timestamp DESC")
    LiveData<List<ConversionHistoryEntity>> getFavorites();
    
    /**
     * Get history for specific category
     */
    @Query("SELECT * FROM conversion_history WHERE category = :category ORDER BY timestamp DESC")
    LiveData<List<ConversionHistoryEntity>> getHistoryByCategory(String category);
    
    /**
     * Search history by unit names
     */
    @Query("SELECT * FROM conversion_history WHERE fromUnit LIKE '%' || :query || '%' " +
           "OR toUnit LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    LiveData<List<ConversionHistoryEntity>> searchHistory(String query);
    
    /**
     * Delete all history
     */
    @Query("DELETE FROM conversion_history")
    void deleteAllHistory();
    
    /**
     * Delete all non-favorite history
     */
    @Query("DELETE FROM conversion_history WHERE isFavorite = 0")
    void deleteNonFavorites();
    
    /**
     * Delete all favorites
     */
    @Query("DELETE FROM conversion_history WHERE isFavorite = 1")
    void deleteFavorites();
    
    /**
     * Delete non-favorite history older than X days
     */
    @Query("DELETE FROM conversion_history WHERE isFavorite = 0 AND timestamp < :timestampBefore")
    void deleteOldHistory(long timestampBefore);
    
    /**
     * Get count of total conversions (for stats)
     */
    @Query("SELECT COUNT(*) FROM conversion_history")
    LiveData<Integer> getTotalConversions();
    
    /**
     * Get most used conversion (for suggestions)
     */
    @Query("SELECT * FROM conversion_history GROUP BY fromUnit, toUnit " +
           "ORDER BY COUNT(*) DESC LIMIT 5")
    LiveData<List<ConversionHistoryEntity>> getMostUsedConversions();
}
