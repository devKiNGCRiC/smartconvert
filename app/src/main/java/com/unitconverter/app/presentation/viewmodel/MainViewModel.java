package com.unitconverter.app.presentation.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.unitconverter.app.data.database.ConversionHistoryEntity;
import com.unitconverter.app.data.provider.UnitProvider;
import com.unitconverter.app.data.repository.HistoryRepository;
import com.unitconverter.app.domain.model.UnitCategory;

import java.util.List;

/**
 * ViewModel for MainActivity.
 * Manages UI data for category selection screen.
 * 
 * TEACHING NOTE: MVVM (Model-View-ViewModel) Pattern
 * 
 * Traditional Android (without MVVM):
 * Activity → Directly manipulates data → Gets destroyed on rotation → Data lost!
 * 
 * With MVVM:
 * Activity → Observes ViewModel → ViewModel survives rotation → Data preserved!
 * 
 * Benefits:
 * 1. Separation of Concerns: UI logic separate from business logic
 * 2. Testability: Can test ViewModel without Android framework
 * 3. Lifecycle Awareness: Survives configuration changes
 * 4. Reactive: UI automatically updates when data changes (LiveData)
 * 
 * Key Concepts:
 * - ViewModel: Holds UI data, survives configuration changes
 * - LiveData: Observable data holder, lifecycle-aware
 * - MutableLiveData: LiveData that can be modified
 * 
 * Design Pattern: Observer Pattern + MVVM
 * - View (Activity) observes ViewModel
 * - When data changes, View is automatically notified
 * - No need to manually update UI
 */
public class MainViewModel extends AndroidViewModel {
    
    // LiveData for categories list
    // TEACHING NOTE: Why MutableLiveData privately, LiveData publicly?
    // - Internal: Can modify (private MutableLiveData)
    // - External: Can only observe (public LiveData)
    // - Encapsulation: Outside code can't accidentally change data
    private final MutableLiveData<UnitCategory[]> categoriesLiveData;
    
    // LiveData for selected category
    private final MutableLiveData<UnitCategory> selectedCategoryLiveData;
    
    // LiveData for error messages
    private final MutableLiveData<String> errorLiveData;
    
    // LiveData for loading state
    private final MutableLiveData<Boolean> loadingLiveData;

    private final UnitProvider unitProvider;
    private final HistoryRepository historyRepository;
    
    // LiveData for recent conversions
    private final LiveData<List<ConversionHistoryEntity>> recentConversionsLiveData;

    /**
     * Constructor initializes LiveData and loads categories.
     * 
     * TEACHING NOTE: ViewModel is created by ViewModelProvider,
     * not by you directly. This ensures proper lifecycle management.
     */
    public MainViewModel(@NonNull Application application) {
        super(application);
        // Initialize LiveData objects
        categoriesLiveData = new MutableLiveData<>();
        selectedCategoryLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
        loadingLiveData = new MutableLiveData<>(false);
        
        // Get unit provider instance
        unitProvider = UnitProvider.getInstance();
        
        // Initialize history repository
        historyRepository = new HistoryRepository(application);
        
        // Get recent conversions (auto-updates from database)
        recentConversionsLiveData = historyRepository.getAllHistory();
        
        // Load categories immediately
        loadCategories();
    }

    /**
     * Loads all available unit categories.
     * 
     * TEACHING NOTE: This simulates async operation.
     * In real app with network/database:
     * 1. Set loading = true
     * 2. Fetch data in background
     * 3. Update LiveData on success
     * 4. Set error on failure
     * 5. Set loading = false
     */
    private void loadCategories() {
        try {
            loadingLiveData.setValue(true);
            
            // Get categories from provider
            UnitCategory[] categories = unitProvider.getAllCategories();
            
            // Update LiveData (this will notify all observers)
            categoriesLiveData.setValue(categories);
            
            loadingLiveData.setValue(false);
        } catch (Exception e) {
            // If something goes wrong, update error LiveData
            errorLiveData.setValue("Failed to load categories: " + e.getMessage());
            loadingLiveData.setValue(false);
        }
    }

    /**
     * User selected a category.
     * Updates the selected category LiveData.
     * 
     * @param category The selected category
     */
    public void selectCategory(UnitCategory category) {
        if (category == null) {
            errorLiveData.setValue("Invalid category selected");
            return;
        }
        
        selectedCategoryLiveData.setValue(category);
    }

    /**
     * Gets the categories LiveData for observation.
     * Activity will observe this to update UI.
     * 
     * @return LiveData containing all categories
     */
    public LiveData<UnitCategory[]> getCategories() {
        return categoriesLiveData;
    }

    /**
     * Gets the selected category LiveData.
     * Used to navigate to conversion screen.
     * 
     * @return LiveData containing selected category
     */
    public LiveData<UnitCategory> getSelectedCategory() {
        return selectedCategoryLiveData;
    }

    /**
     * Gets error LiveData for displaying error messages.
     * 
     * @return LiveData containing error message
     */
    public LiveData<String> getError() {
        return errorLiveData;
    }

    /**
     * Gets loading state LiveData.
     * Used to show/hide progress indicators.
     * 
     * @return LiveData containing loading state
     */
    public LiveData<Boolean> getLoading() {
        return loadingLiveData;
    }
    
    /**
     * Gets recent conversions LiveData.
     * Auto-updates from database.
     * 
     * @return LiveData containing list of recent conversions
     */
    public LiveData<List<ConversionHistoryEntity>> getRecentConversions() {
        return recentConversionsLiveData;
    }
    
    /**
     * Toggle favorite status for a conversion.
     */
    public void toggleFavorite(ConversionHistoryEntity conversion) {
        historyRepository.toggleFavorite(conversion);
    }
    
    /**
     * Get favorites LiveData.
     * @return LiveData containing list of favorite conversions
     */
    public LiveData<List<ConversionHistoryEntity>> getFavorites() {
        return historyRepository.getFavorites();
    }
    
    /**
     * Delete a single history item.
     */
    public void deleteHistoryItem(ConversionHistoryEntity item) {
        historyRepository.deleteConversion(item);
    }
    
    /**
     * Restore a deleted history item (for undo functionality).
     */
    public void restoreHistoryItem(ConversionHistoryEntity item) {
        historyRepository.restoreConversion(item);
    }
    
    /** Current text in the history search box; fragments re-filter when it changes. */
    private final MutableLiveData<String> searchQueryLiveData = new MutableLiveData<>("");
    
    public LiveData<String> getSearchQuery() {
        return searchQueryLiveData;
    }
    
    public void setSearchQuery(String query) {
        searchQueryLiveData.setValue(query == null ? "" : query);
    }
    
    /**
     * Clear all non-favorite history.
     */
    public void clearNonFavoriteHistory() {
        historyRepository.clearNonFavoriteHistory();
    }
    
    /**
     * Clear all favorites.
     */
    public void clearFavorites() {
        historyRepository.clearFavorites();
    }

    /**
     * Refreshes the categories list.
     * Called when user pulls to refresh.
     */
    public void refresh() {
        loadCategories();
    }

    /**
     * Called when ViewModel is destroyed.
     * Use this to clean up resources (cancel network requests, close connections, etc.)
     * 
     * TEACHING NOTE: ViewModel lifecycle
     * - Created: When first accessed via ViewModelProvider
     * - Survives: Configuration changes (rotation, language change)
     * - Destroyed: When associated Activity finishes permanently
     * - onCleared(): Last chance to clean up before destruction
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up if needed (close database connections, cancel operations, etc.)
        // In this case, no cleanup needed
    }
}
