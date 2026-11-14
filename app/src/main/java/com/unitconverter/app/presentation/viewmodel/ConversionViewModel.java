package com.unitconverter.app.presentation.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.unitconverter.app.data.provider.UnitProvider;
import com.unitconverter.app.data.repository.ConversionRepository;
import com.unitconverter.app.data.repository.HistoryRepository;
import com.unitconverter.app.domain.model.ConversionResult;
import com.unitconverter.app.domain.model.Unit;
import com.unitconverter.app.domain.model.UnitCategory;
import com.unitconverter.app.util.NumberFormatter;

import java.util.List;

/**
 * ViewModel for ConversionActivity.
 * Handles unit conversion logic and UI state.
 * 
 * TEACHING NOTE: This is a more complex ViewModel that demonstrates:
 * - Multiple LiveData streams
 * - Business logic delegation to Repository
 * Responsibilities:
 * ✓ Hold UI state (selected units, input value, result)
 * ✓ Coordinate between Repository and View
 * ✓ Validate user input
 * ✓ Handle errors gracefully
 * ✓ Save conversion history automatically
 * ✗ NOT responsible for: UI rendering, navigation decisions
 */
public class ConversionViewModel extends AndroidViewModel {
    
    // Repository and provider instances
    private final ConversionRepository conversionRepository;
    private final UnitProvider unitProvider;
    private final HistoryRepository historyRepository;
    
    // A conversion is only written to history after the user stops typing/selecting
    // for this long, so "12345" is saved once instead of as 1, 12, 123, ...
    private static final long HISTORY_SAVE_DELAY_MS = 1500L;

    private final Handler saveHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingHistorySave;
    private String lastSavedKey;

    // Current category being converted
    private UnitCategory currentCategory;
    
    // LiveData for available units in current category
    private final MutableLiveData<List<Unit>> availableUnitsLiveData;
    
    // LiveData for selected units
    private final MutableLiveData<Unit> fromUnitLiveData;
    private final MutableLiveData<Unit> toUnitLiveData;
    
    // LiveData for input value
    private final MutableLiveData<String> inputValueLiveData;
    
    // LiveData for conversion result
    private final MutableLiveData<ConversionResult> resultLiveData;
    
    // LiveData for formatted result string (ready to display)
    private final MutableLiveData<String> formattedResultLiveData;
    
    // LiveData for error messages
    private final MutableLiveData<String> errorLiveData;
    
    // LiveData for validation state
    private final MutableLiveData<Boolean> isValidInputLiveData;

    /**
     * Constructor initializes repositories and LiveData.
     */
    public ConversionViewModel(@NonNull Application application) {
        super(application);
        conversionRepository = ConversionRepository.getInstance();
        unitProvider = UnitProvider.getInstance();
        historyRepository = new HistoryRepository(application);
        
        // Initialize all LiveData
        availableUnitsLiveData = new MutableLiveData<>();
        fromUnitLiveData = new MutableLiveData<>();
        toUnitLiveData = new MutableLiveData<>();
        inputValueLiveData = new MutableLiveData<>("");
        resultLiveData = new MutableLiveData<>();
        formattedResultLiveData = new MutableLiveData<>("");
        errorLiveData = new MutableLiveData<>();
        isValidInputLiveData = new MutableLiveData<>(true);
    }

    /**
     * Sets the category for conversion.
     * Loads available units for this category.
     * 
     * TEACHING NOTE: This method is called from Activity's onCreate().
     * It initializes the ViewModel with necessary context.
     * 
     * @param category The category to convert in
     */
    public void setCategory(UnitCategory category) {
        if (category == null) {
            errorLiveData.setValue("Invalid category");
            return;
        }
        
        this.currentCategory = category;
        
        // Load units for this category
        List<Unit> units = unitProvider.getUnitsForCategory(category);
        availableUnitsLiveData.setValue(units);
        
        // Set default units (first two in the list)
        if (units != null && units.size() >= 2) {
            fromUnitLiveData.setValue(units.get(0));
            toUnitLiveData.setValue(units.get(1));
        }
    }

    /**
     * Pre-selects units (matched by their display text as stored in history),
     * used by the "Reuse" action in history.
     */
    public void prefillUnits(String fromDisplayText, String toDisplayText) {
        List<Unit> units = availableUnitsLiveData.getValue();
        if (units == null) {
            return;
        }
        for (Unit unit : units) {
            if (unit.getDisplayText().equals(fromDisplayText)) {
                fromUnitLiveData.setValue(unit);
            }
            if (unit.getDisplayText().equals(toDisplayText)) {
                toUnitLiveData.setValue(unit);
            }
        }
    }

    /**
     * User selected a "from" unit.
     * 
     * @param unit The unit to convert from
     */
    public void setFromUnit(Unit unit) {
        if (unit == null) {
            errorLiveData.setValue("Invalid unit selected");
            return;
        }
        
        fromUnitLiveData.setValue(unit);
        
        // If there's input, recalculate conversion
        performConversionIfReady();
    }

    /**
     * User selected a "to" unit.
     * 
     * @param unit The unit to convert to
     */
    public void setToUnit(Unit unit) {
        if (unit == null) {
            errorLiveData.setValue("Invalid unit selected");
            return;
        }
        
        toUnitLiveData.setValue(unit);
        
        // If there's input, recalculate conversion
        performConversionIfReady();
    }

    /**
     * User entered a value to convert.
     * 
     * TEACHING NOTE: Real-time validation and conversion
     * - Validate as user types
     * - Show immediate feedback
     * - Don't wait for button click
     * - Better UX than traditional form submission
     * 
     * @param input The input string from EditText
     */
    public void setInputValue(String input) {
        inputValueLiveData.setValue(input);
        
        if (input == null || input.trim().isEmpty() || NumberFormatter.isIncomplete(input)) {
            // Empty or half-typed ("-", ".") is not an error, there is just no result yet
            isValidInputLiveData.setValue(true);
            showNoResult();
            return;
        }
        
        try {
            double value = NumberFormatter.parse(input);
            
            if (conversionRepository.isValidInput(value)) {
                isValidInputLiveData.setValue(true);
                performConversionIfReady();
            } else {
                isValidInputLiveData.setValue(false);
                showNoResult();
                errorLiveData.setValue("Invalid number: cannot be infinite or NaN");
            }
        } catch (NumberFormatException e) {
            isValidInputLiveData.setValue(false);
            showNoResult();
            errorLiveData.setValue("Invalid number format");
        }
    }

    private void showNoResult() {
        cancelPendingHistorySave();
        formattedResultLiveData.setValue("");
        resultLiveData.setValue(null);
    }

    /**
     * Swaps from and to units.
     * Useful feature for quick reverse conversion.
     * 
     * TEACHING NOTE: Small features like this greatly improve UX
     * - Saves user time
     * - Common use case (convert both ways)
     * - Easy to implement
     * - Professional touch
     */
    public void swapUnits() {
        Unit from = fromUnitLiveData.getValue();
        Unit to = toUnitLiveData.getValue();
        
        if (from != null && to != null) {
            fromUnitLiveData.setValue(to);
            toUnitLiveData.setValue(from);
            
            // Recalculate with swapped units
            performConversionIfReady();
        }
    }

    /**
     * Performs conversion if all required data is available.
     * 
     * TEACHING NOTE: Guard Clauses Pattern
     * - Check preconditions first
     * - Early return if not ready
     * - Keeps main logic clean and readable
     * - Prevents NullPointerException
     */
    private void performConversionIfReady() {
        String inputStr = inputValueLiveData.getValue();
        Unit fromUnit = fromUnitLiveData.getValue();
        Unit toUnit = toUnitLiveData.getValue();
        
        if (inputStr == null || inputStr.trim().isEmpty() || NumberFormatter.isIncomplete(inputStr)) {
            return; // No input yet
        }
        
        if (fromUnit == null || toUnit == null) {
            return; // Units not selected yet
        }
        
        try {
            double value = NumberFormatter.parse(inputStr);
            
            ConversionResult result = conversionRepository.convert(value, fromUnit, toUnit);
            resultLiveData.setValue(result);
            
            String formatted = conversionRepository.formatValue(result.getOutputValue());
            formattedResultLiveData.setValue(formatted);
            
            scheduleHistorySave(fromUnit, toUnit, value, result.getOutputValue());
            
            errorLiveData.setValue(null);
            
        } catch (NumberFormatException e) {
            errorLiveData.setValue("Invalid number format");
        } catch (IllegalArgumentException e) {
            errorLiveData.setValue(e.getMessage());
        } catch (Exception e) {
            errorLiveData.setValue("Conversion error: " + e.getMessage());
        }
    }

    /**
     * Saves the conversion to history once the user has stopped changing it.
     * Each new change replaces the pending save; identical consecutive
     * conversions are not saved twice.
     */
    private void scheduleHistorySave(Unit fromUnit, Unit toUnit, double value, double outputValue) {
        if (currentCategory == null) {
            return;
        }
        cancelPendingHistorySave();

        final String category = currentCategory.name();
        final String from = fromUnit.getDisplayText();
        final String to = toUnit.getDisplayText();
        final String key = category + "|" + from + "|" + to + "|" + value;

        pendingHistorySave = () -> {
            pendingHistorySave = null;
            if (key.equals(lastSavedKey)) {
                return;
            }
            lastSavedKey = key;
            historyRepository.saveConversion(category, from, to, value, outputValue);
        };
        saveHandler.postDelayed(pendingHistorySave, HISTORY_SAVE_DELAY_MS);
    }

    private void cancelPendingHistorySave() {
        if (pendingHistorySave != null) {
            saveHandler.removeCallbacks(pendingHistorySave);
            pendingHistorySave = null;
        }
    }

    /**
     * Manually triggers conversion.
     * Called when user presses "Convert" button.
     */
    public void performConversion() {
        performConversionIfReady();
    }

    /**
     * Clears all input and results.
     * Called when user presses "Clear" button.
     */
    public void clear() {
        cancelPendingHistorySave();
        inputValueLiveData.setValue("");
        formattedResultLiveData.setValue("");
        resultLiveData.setValue(null);
        errorLiveData.setValue(null);
        isValidInputLiveData.setValue(true);
    }

    // ========== GETTERS FOR LIVEDATA (for Activity to observe) ==========

    public LiveData<List<Unit>> getAvailableUnits() {
        return availableUnitsLiveData;
    }

    public LiveData<Unit> getFromUnit() {
        return fromUnitLiveData;
    }

    public LiveData<Unit> getToUnit() {
        return toUnitLiveData;
    }

    public LiveData<String> getInputValue() {
        return inputValueLiveData;
    }

    public LiveData<ConversionResult> getResult() {
        return resultLiveData;
    }

    public LiveData<String> getFormattedResult() {
        return formattedResultLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public LiveData<Boolean> getIsValidInput() {
        return isValidInputLiveData;
    }

    public UnitCategory getCurrentCategory() {
        return currentCategory;
    }

    /**
     * Cleanup when ViewModel is destroyed.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        // Don't lose a conversion the user made just before leaving the screen
        if (pendingHistorySave != null) {
            Runnable save = pendingHistorySave;
            saveHandler.removeCallbacks(save);
            save.run();
        }
    }
}
