package com.unitconverter.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.unitconverter.app.domain.model.Unit;
import com.unitconverter.app.domain.model.UnitCategory;
import com.unitconverter.app.presentation.viewmodel.ConversionViewModel;
import com.unitconverter.app.presentation.widget.RulerView;
import com.unitconverter.app.util.WindowInsetsHelper;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

/**
 * Conversion Activity - Unit Conversion Screen
 * 
 * TEACHING NOTE: This is a more complex Activity that demonstrates:
 * - MVVM with multiple LiveData streams
 * - Real-time input handling with TextWatcher
 * - Spinner (dropdown) interaction
 * - Two-way data flow (user input → ViewModel → UI update)
 * - Input validation and error handling
 * 
 * User Experience Features:
 * - Automatic conversion as user types
 * - Unit swap with one tap
 * - Clear input quickly
 * - Proper keyboard handling
 * - Error feedback
 * 
 * Flow:
 * 1. User selects category (from MainActivity)
 * 2. ViewModel loads units for category
 * 3. User enters value → ViewModel converts → Result displays
 * 4. User can swap units or clear anytime
 */
public class ConversionActivity extends AppCompatActivity {

    // UI Components
    private Toolbar toolbar;
    private Spinner fromUnitSpinner;
    private Spinner toUnitSpinner;
    private TextInputEditText inputValue;
    private TextInputLayout inputLayout;
    private TextView resultValue;
    private TextView resultUnit;
    private Button swapButton;
    private Button clearButton;
    private View copyButton;
    private RulerView tapeRuler;

    // Last shown output, used to slide the tape left or right when it changes
    private Double lastOutput = null;

    // ViewModel
    private ConversionViewModel viewModel;

    // Category for this conversion
    private UnitCategory category;

    // Flags to prevent infinite loops during programmatic updates
    private boolean isUpdatingSpinners = false;

    /**
     * onCreate initializes the conversion screen.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_conversion);
        WindowInsetsHelper.applySystemBarPadding(findViewById(R.id.conversionRoot));

        // Get category from Intent
        String categoryName = getIntent().getStringExtra("CATEGORY");
        if (categoryName == null) {
            // No category provided - shouldn't happen, but handle gracefully
            Snackbar.make(findViewById(android.R.id.content),
                    R.string.error_no_category,
                    Snackbar.LENGTH_LONG).show();
            finish(); // Close this Activity
            return;
        }

        // Convert category name back to enum
        // TEACHING NOTE: Enum.valueOf() recreates enum from string
        try {
            category = UnitCategory.valueOf(categoryName);
        } catch (IllegalArgumentException e) {
            Snackbar.make(findViewById(android.R.id.content),
                    "Invalid category",
                    Snackbar.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize views
        initializeViews();

        // Setup toolbar
        setupToolbar();

        // Get ViewModel
        viewModel = new ViewModelProvider(this).get(ConversionViewModel.class);

        // Set category in ViewModel
        viewModel.setCategory(category);

        // Setup spinners and input
        setupInputHandling();

        if (savedInstanceState == null) {
            applyReuseExtras();
        }

        // Observe ViewModel
        observeViewModel();

        // Setup button clicks
        setupButtons();
    }

    /**
     * Finds and caches all view references.
     */
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        fromUnitSpinner = findViewById(R.id.fromUnitSpinner);
        toUnitSpinner = findViewById(R.id.toUnitSpinner);
        inputValue = findViewById(R.id.inputValue);
        inputLayout = findViewById(R.id.inputLayout);
        resultValue = findViewById(R.id.resultValue);
        resultUnit = findViewById(R.id.resultUnit);
        swapButton = findViewById(R.id.swapButton);
        clearButton = findViewById(R.id.clearButton);
        copyButton = findViewById(R.id.copyButton);
        tapeRuler = findViewById(R.id.tapeRuler);
    }

    /**
     * Sets up toolbar with title and back button.
     * 
     * TEACHING NOTE: Toolbar
     * - Modern replacement for ActionBar
     * - More flexible and customizable
     * - Part of Material Design
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            // Set title with category name
            getSupportActionBar().setTitle(
                    getString(R.string.conversion_title, category.getDisplayName())
            );
        }

        // Handle back button click
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    /**
     * "Reuse" in history opens this screen with the units and value of an earlier
     * conversion; pre-fill them (only on first launch, not after rotation).
     */
    private void applyReuseExtras() {
        String from = getIntent().getStringExtra("FROM_UNIT");
        String to = getIntent().getStringExtra("TO_UNIT");
        if (from != null && to != null) {
            viewModel.prefillUnits(from, to);
        }
        if (getIntent().hasExtra("INPUT_VALUE")) {
            double value = getIntent().getDoubleExtra("INPUT_VALUE", 0);
            String text = BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
            // Use the keyboard's decimal separator so the input filter accepts it
            char separator = DecimalFormatSymbols.getInstance().getDecimalSeparator();
            inputValue.setText(text.replace('.', separator));
        }
    }

    /**
     * Sets up input handling with TextWatcher for real-time conversion.
     * 
     * TEACHING NOTE: TextWatcher
     * - Monitors EditText changes
     * - Three methods: beforeTextChanged, onTextChanged, afterTextChanged
     * - Use afterTextChanged for final text value
     * 
     * Real-time Conversion:
     * - Better UX than "Convert" button
     * - Immediate feedback
     * - More modern and intuitive
     */
    private void setupInputHandling() {
        inputValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed for our use case
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed for our use case
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Text has changed - update ViewModel
                // TEACHING NOTE: Convert Editable to String
                String input = s.toString();
                viewModel.setInputValue(input);
            }
        });
    }

    /**
     * Observes ViewModel LiveData and updates UI.
     * 
     * TEACHING NOTE: Multiple LiveData Streams
     * - Each piece of UI state has its own LiveData
     * - Observers update specific UI elements
     * - Clean separation of concerns
     * - Easy to test individual pieces
     */
    private void observeViewModel() {
        // Observe available units
        viewModel.getAvailableUnits().observe(this, units -> {
            if (units != null && !units.isEmpty()) {
                setupSpinners(units);
            }
        });

        // Observe from unit selection
        viewModel.getFromUnit().observe(this, unit -> {
            if (unit != null && !isUpdatingSpinners) {
                updateFromSpinnerSelection(unit);
            }
        });

        // Observe to unit selection
        viewModel.getToUnit().observe(this, unit -> {
            if (unit != null && !isUpdatingSpinners) {
                updateToSpinnerSelection(unit);
            }
        });

        // Observe formatted result
        viewModel.getFormattedResult().observe(this, formatted -> {
            boolean hasResult = formatted != null && !formatted.isEmpty();
            // Empty state: a faint zero on the tape rather than a sentence in display type
            resultValue.setText(hasResult ? formatted : getString(R.string.result_placeholder));
            resultValue.setAlpha(hasResult ? 1f : 0.35f);
            resultValue.setContentDescription(hasResult ? null : getString(R.string.no_result));
            copyButton.setEnabled(hasResult);
            copyButton.setAlpha(hasResult ? 1f : 0.35f);
        });

        // Observe result to show unit symbol
        viewModel.getResult().observe(this, result -> {
            if (result != null) {
                resultUnit.setText(result.getToUnit().getDisplayText());
                resultUnit.setVisibility(View.VISIBLE);
                // Pull the tape out (or reel it in) as the number grows or shrinks
                double out = result.getOutputValue();
                if (lastOutput != null && out != lastOutput) {
                    tapeRuler.nudge(out > lastOutput ? -5 : 5);
                }
                lastOutput = out;
            } else {
                resultUnit.setVisibility(View.INVISIBLE);
                lastOutput = null;
            }
        });

        // Observe input validation
        viewModel.getIsValidInput().observe(this, isValid -> {
            if (isValid) {
                inputLayout.setError(null);
            } else {
                inputLayout.setError(getString(R.string.error_invalid_input));
            }
        });

        // Observe errors
        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sets up spinners with unit data.
     * 
     * TEACHING NOTE: Spinner (Dropdown)
     * - Android's built-in dropdown component
     * - Needs an Adapter (like RecyclerView)
     * - ArrayAdapter is simplest adapter
     * 
     * @param units List of units to display
     */
    private void setupSpinners(List<Unit> units) {
        // Create display strings for spinner
        // TEACHING NOTE: Stream API (Java 8+)
        // - Functional programming style
        // - map: transforms each element
        // - collect: gathers results into collection
        List<String> unitNames = new ArrayList<>();
        for (Unit unit : units) {
            unitNames.add(unit.getDisplayText());
        }

        // Create adapters
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.item_spinner_selected,
                unitNames
        );
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);

        // Set adapters
        fromUnitSpinner.setAdapter(adapter);
        toUnitSpinner.setAdapter(adapter);

        // Set up selection listeners
        setupSpinnerListeners(units);
    }

    /**
     * Sets up spinner selection listeners.
     * 
     * TEACHING NOTE: AdapterView.OnItemSelectedListener
     * - Fires when user selects item from dropdown
     * - position: index in the list (0-based)
     * - Use position to get corresponding Unit from list
     * 
     * @param units List of units (same order as spinner)
     */
    private void setupSpinnerListeners(List<Unit> units) {
        fromUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isUpdatingSpinners && position >= 0 && position < units.size()) {
                    Unit selectedUnit = units.get(position);
                    viewModel.setFromUnit(selectedUnit);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Not needed
            }
        });

        toUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isUpdatingSpinners && position >= 0 && position < units.size()) {
                    Unit selectedUnit = units.get(position);
                    viewModel.setToUnit(selectedUnit);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Not needed
            }
        });
    }

    /**
     * Updates from spinner selection programmatically.
     * 
     * TEACHING NOTE: Flag Pattern to Prevent Loops
     * Problem: Spinner selection triggers listener → Updates ViewModel → 
     *          ViewModel updates LiveData → Observer updates Spinner → Triggers listener again!
     * Solution: Use flag to skip listener when updating programmatically
     * 
     * @param unit Unit to select
     */
    private void updateFromSpinnerSelection(Unit unit) {
        isUpdatingSpinners = true;
        
        // Find position of unit in spinner
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) fromUnitSpinner.getAdapter();
        if (adapter != null) {
            int position = adapter.getPosition(unit.getDisplayText());
            if (position >= 0) {
                fromUnitSpinner.setSelection(position);
            }
        }
        
        isUpdatingSpinners = false;
    }

    /**
     * Updates to spinner selection programmatically.
     */
    private void updateToSpinnerSelection(Unit unit) {
        isUpdatingSpinners = true;
        
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) toUnitSpinner.getAdapter();
        if (adapter != null) {
            int position = adapter.getPosition(unit.getDisplayText());
            if (position >= 0) {
                toUnitSpinner.setSelection(position);
            }
        }
        
        isUpdatingSpinners = false;
    }

    /**
     * Sets up button click listeners.
     */
    private void setupButtons() {
        // Swap button
        swapButton.setOnClickListener(v -> viewModel.swapUnits());

        // Copy button: puts "value unit-symbol" on the clipboard
        copyButton.setOnClickListener(v -> copyResult());

        // Clear button
        clearButton.setOnClickListener(v -> {
            viewModel.clear();
            inputValue.setText("");
            inputValue.clearFocus();
        });
    }

    /** Copies the current result, e.g. "4,101.05 ft", to the clipboard. */
    private void copyResult() {
        com.unitconverter.app.domain.model.ConversionResult result = viewModel.getResult().getValue();
        CharSequence value = resultValue.getText();
        if (result == null || value == null || value.length() == 0) return;

        String text = value + " " + result.getToUnit().getSymbol();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.result_label), text));
            Snackbar.make(findViewById(android.R.id.content),
                    getString(R.string.result_copied, text), Snackbar.LENGTH_SHORT).show();
        }
    }
}
