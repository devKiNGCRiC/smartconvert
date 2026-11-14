package com.unitconverter.app.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.unitconverter.app.data.database.AppDatabase;
import com.unitconverter.app.data.database.ConversionHistoryDao;
import com.unitconverter.app.data.database.ConversionHistoryEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for History Management
 * 
 * Handles all history-related operations:
 * - Save conversions
 * - Retrieve history
 * - Manage favorites
 * - Search functionality
 */
public class HistoryRepository {
    
    private final ConversionHistoryDao historyDao;
    // One shared writer thread for the whole app. Every ViewModel creates its own
    // repository, so a per-instance executor would leak a thread per screen opened.
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    
    private final ExecutorService executorService = EXECUTOR;
    
    public HistoryRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        historyDao = database.historyDao();
    }
    
    /**
     * Save a conversion to history (async)
     */
    public void saveConversion(String category, String fromUnit, String toUnit,
                               double inputValue, double resultValue) {
        executorService.execute(() -> {
            ConversionHistoryEntity entity = new ConversionHistoryEntity(
                    category, fromUnit, toUnit, inputValue, resultValue,
                    System.currentTimeMillis()
            );
            historyDao.insert(entity);
        });
    }
    
    /**
     * Re-insert a previously deleted entry exactly as it was (id, timestamp, favorite flag).
     * Used by "Undo" after a swipe-to-delete.
     */
    public void restoreConversion(ConversionHistoryEntity history) {
        executorService.execute(() -> historyDao.insert(history));
    }
    
    /**
     * Toggle favorite status
     */
    public void toggleFavorite(ConversionHistoryEntity history) {
        executorService.execute(() -> {
            history.setFavorite(!history.isFavorite());
            historyDao.update(history);
        });
    }
    
    /**
     * Delete a history entry
     */
    public void deleteHistory(ConversionHistoryEntity history) {
        executorService.execute(() -> historyDao.delete(history));
    }
    
    /**
     * Delete a single conversion (for HistoryActivity)
     */
    public void deleteConversion(ConversionHistoryEntity history) {
        executorService.execute(() -> historyDao.delete(history));
    }
    
    /**
     * Clear all history
     */
    public void clearAllHistory() {
        executorService.execute(() -> historyDao.deleteAllHistory());
    }
    
    /**
     * Clear all non-favorite history
     */
    public void clearNonFavoriteHistory() {
        executorService.execute(() -> historyDao.deleteNonFavorites());
    }
    
    /**
     * Clear all favorites
     */
    public void clearFavorites() {
        executorService.execute(() -> historyDao.deleteFavorites());
    }
    
    /**
     * Delete old non-favorite history (e.g., older than 30 days)
     */
    public void deleteOldHistory(int daysOld) {
        long cutoffTime = System.currentTimeMillis() - (daysOld * 24L * 60 * 60 * 1000);
        executorService.execute(() -> historyDao.deleteOldHistory(cutoffTime));
    }
    
    /**
     * Get all history (LiveData - auto-updates UI)
     */
    public LiveData<List<ConversionHistoryEntity>> getAllHistory() {
        return historyDao.getAllHistory();
    }
    
    /**
     * Get only favorites
     */
    public LiveData<List<ConversionHistoryEntity>> getFavorites() {
        return historyDao.getFavorites();
    }
    
    /**
     * Get history for specific category
     */
    public LiveData<List<ConversionHistoryEntity>> getHistoryByCategory(String category) {
        return historyDao.getHistoryByCategory(category);
    }
    
    /**
     * Search history
     */
    public LiveData<List<ConversionHistoryEntity>> searchHistory(String query) {
        return historyDao.searchHistory(query);
    }
    
    /**
     * Get total conversion count
     */
    public LiveData<Integer> getTotalConversions() {
        return historyDao.getTotalConversions();
    }
    
    /**
     * Get most used conversions (for suggestions)
     */
    public LiveData<List<ConversionHistoryEntity>> getMostUsedConversions() {
        return historyDao.getMostUsedConversions();
    }
}
