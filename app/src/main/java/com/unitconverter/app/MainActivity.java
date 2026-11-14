package com.unitconverter.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.unitconverter.app.data.database.ConversionHistoryEntity;
import com.unitconverter.app.domain.model.UnitCategory;
import com.unitconverter.app.presentation.adapter.CategoryAdapter;
import com.unitconverter.app.presentation.adapter.RecentConversionsAdapter;
import com.unitconverter.app.presentation.viewmodel.MainViewModel;

/**
 * Main Activity - Enhanced Category Selection Screen
 * 
 * MODERN FEATURES ADDED:
 * - Android 12+ Splash Screen API
 * - Smooth entrance animations
 * - Edge-to-edge display
 * - Enhanced Material Design 3 styling
 * 
 * TEACHING NOTE: Activity in MVVM Architecture
 * 
 * Activity's Responsibilities:
 * ✓ Inflate layout and find views
 * ✓ Observe ViewModel LiveData
 * ✓ Update UI based on data changes
 * ✓ Handle user interactions (clicks)
 * ✓ Navigate to other screens
 * ✗ NO business logic here!
 * ✗ NO direct data manipulation!
 * 
 * MVVM Flow:
 * 1. User clicks → Activity tells ViewModel
 * 2. ViewModel processes → Updates LiveData
 * 3. LiveData changes → Activity observes → Updates UI
 * 
 * Benefits:
 * - Testable: ViewModel can be tested without Android framework
 * - Maintainable: Clear separation of concerns
 * - Lifecycle-aware: Survives configuration changes
 * 
 * Design Patterns:
 * - MVVM: Separation of UI and business logic
 * - Observer: Activity observes ViewModel
 * - Dependency Injection: ViewModel provided by ViewModelProvider
 */
public class MainActivity extends AppCompatActivity implements 
        CategoryAdapter.OnCategoryClickListener,
        RecentConversionsAdapter.OnConversionClickListener {

    // UI Components
    private RecyclerView categoriesRecyclerView;
    private RecyclerView recentConversionsRecyclerView;
    private LinearLayout recentConversionsSection;
    private MaterialButton viewAllHistoryButton;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    
    // ViewModel - survives configuration changes
    private MainViewModel viewModel;
    
    // Adapters
    private CategoryAdapter adapter;
    private RecentConversionsAdapter recentAdapter;

    /**
     * onCreate is called when Activity is created.
     * 
     * TEACHING NOTE: Activity Lifecycle
     * onCreate → onStart → onResume → [RUNNING] → onPause → onStop → onDestroy
     * 
     * onCreate:
     * - Called once when Activity is first created
     * - Initialize UI, get ViewModel, set up observers
     * - Don't do heavy work here (slow startup)
     * 
     * @param savedInstanceState Saved state from previous instance (null if first time)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Install Splash Screen (Android 12+)
        // TEACHING NOTE: Must be called before super.onCreate()
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        
        super.onCreate(savedInstanceState);
        
        // Enable edge-to-edge display (modern Android UI)
        EdgeToEdge.enable(this);
        
        // Inflate layout
        setContentView(R.layout.activity_main);
        
        // Handle system bars insets for edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Initialize views
        initializeViews();
        
        // Get or create ViewModel
        // TEACHING NOTE: ViewModelProvider ensures same instance across config changes
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        
        // Set up RecyclerView
        setupRecyclerView();
        
        // Observe ViewModel LiveData
        observeViewModel();
    }

    /**
     * Finds and caches all view references.
     * 
     * TEACHING NOTE: findViewById
     * - Traverses view hierarchy to find view by ID
     * - Slow operation, so cache the result
     * - Do this in onCreate, not in loops or frequent methods
     */
    private void initializeViews() {
        categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView);
        recentConversionsRecyclerView = findViewById(R.id.recentConversionsRecyclerView);
        recentConversionsSection = findViewById(R.id.recentConversionsSection);
        viewAllHistoryButton = findViewById(R.id.viewAllHistoryButton);
        progressBar = findViewById(R.id.progressBar);
        emptyStateText = findViewById(R.id.emptyStateText);
        
        // Set up recent conversions RecyclerView
        recentConversionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recentAdapter = new RecentConversionsAdapter(this);
        recentConversionsRecyclerView.setAdapter(recentAdapter);
        
        // View All History button click
        viewAllHistoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Sets up RecyclerView with GridLayoutManager and adapter.
     * 
     * TEACHING NOTE: RecyclerView Setup
     * 1. Create LayoutManager (Grid, Linear, or Staggered)
     * 2. Create Adapter with data
     * 3. Set both on RecyclerView
     * 
     * GridLayoutManager:
     * - spanCount: Number of columns
     * - Automatically handles sizing and positioning
     */
    private void setupRecyclerView() {
        // Create grid layout with 2 columns
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        categoriesRecyclerView.setLayoutManager(layoutManager);
        
        // Adapter will be set when data arrives from ViewModel
    }

    /**
     * Observes ViewModel LiveData and updates UI accordingly.
     * 
     * TEACHING NOTE: Observer Pattern with LiveData
     * 
     * observe(lifecycleOwner, observer)
     * - lifecycleOwner: Activity (this) - automatically handles lifecycle
     * - observer: Lambda that receives data updates
     * 
     * Benefits:
     * - Automatic lifecycle management (no memory leaks!)
     * - Only updates when Activity is active
     * - No need to manually unsubscribe
     * 
     * How it works:
     * 1. ViewModel updates LiveData
     * 2. LiveData notifies all active observers
     * 3. Observer callback runs on main thread
     * 4. UI updates with new data
     */
    private void observeViewModel() {
        // Observe categories data
        viewModel.getCategories().observe(this, categories -> {
            if (categories != null && categories.length > 0) {
                // Data available - show RecyclerView
                showCategories(categories);
            } else {
                // No data - show empty state
                showEmptyState();
            }
        });
        
        // Observe loading state
        viewModel.getLoading().observe(this, isLoading -> {
            // Show/hide progress bar
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        
        // Observe errors
        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                // Show error message using Snackbar
                // TEACHING NOTE: Snackbar vs Toast
                // Snackbar: Material Design, can have actions, non-intrusive
                // Toast: Simple, can't be dismissed, no actions
                Snackbar.make(categoriesRecyclerView, error, Snackbar.LENGTH_LONG)
                        .setAction("Retry", v -> viewModel.refresh())
                        .show();
            }
        });
        
        // Observe selected category (for navigation)
        viewModel.getSelectedCategory().observe(this, category -> {
            if (category != null) {
                // Navigate to conversion screen
                navigateToConversion(category);
            }
        });
        
        // Observe recent conversions
        viewModel.getRecentConversions().observe(this, conversions -> {
            if (conversions != null && !conversions.isEmpty()) {
                recentConversionsSection.setVisibility(View.VISIBLE);
                recentAdapter.setConversions(conversions);
            } else {
                recentConversionsSection.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Displays categories in RecyclerView.
     * 
     * @param categories Array of categories to display
     */
    private void showCategories(UnitCategory[] categories) {
        // Hide empty state and progress
        emptyStateText.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        
        // Show RecyclerView
        categoriesRecyclerView.setVisibility(View.VISIBLE);
        
        // Create and set adapter
        adapter = new CategoryAdapter(categories, this);
        categoriesRecyclerView.setAdapter(adapter);
    }

    /**
     * Shows empty state when no categories available.
     */
    private void showEmptyState() {
        categoriesRecyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.VISIBLE);
    }

    /**
     * Handles category click from adapter.
     * Implements CategoryAdapter.OnCategoryClickListener interface.
     * 
     * TEACHING NOTE: Callback Pattern
     * - Adapter calls this method when item clicked
     * - Activity delegates to ViewModel
     * - ViewModel updates selected category LiveData
     * - Observer triggers navigation
     * 
     * Why this flow?
     * - ViewModel holds UI state (selected category)
     * - Survives configuration changes
     * - If phone rotates during navigation, state is preserved
     * 
     * @param category The clicked category
     */
    @Override
    public void onCategoryClick(UnitCategory category) {
        // Tell ViewModel about selection
        viewModel.selectCategory(category);
    }
    
    @Override
    public void onConversionClick(ConversionHistoryEntity conversion) {
        // Navigate to conversion screen with this conversion's category
        try {
            UnitCategory category = UnitCategory.valueOf(conversion.getCategory());
            navigateToConversion(category);
        } catch (IllegalArgumentException e) {
            Snackbar.make(categoriesRecyclerView, "Invalid category", Snackbar.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onFavoriteClick(ConversionHistoryEntity conversion) {
        // Toggle favorite status
        viewModel.toggleFavorite(conversion);
        
        // Show feedback
        String message = conversion.isFavorite() ? "Removed from favorites" : "Added to favorites";
        Snackbar.make(categoriesRecyclerView, message, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Navigates to conversion screen with selected category.
     * 
     * TEACHING NOTE: Intent Navigation
     * - Intent: Message object for communication
     * - Explicit Intent: Specifies exact target Activity
     * - putExtra: Passes data to target Activity
     * 
     * @param category Selected category
     */
    private void navigateToConversion(UnitCategory category) {
        Intent intent = new Intent(this, ConversionActivity.class);
        
        // Pass category name as extra
        // TEACHING NOTE: We pass the enum name, not the object
        // - Enums are serializable by default
        // - Enum.name() returns the constant name as String
        // - Target Activity can recreate enum using valueOf()
        intent.putExtra("CATEGORY", category.name());
        
        startActivity(intent);
    }

    /**
     * onResume is called when Activity becomes visible and interactive.
     * Good place to refresh data if needed.
     * 
     * TEACHING NOTE: Lifecycle method
     * - Called after onStart
     * - Activity is now fully visible and interactive
     * - Resume animations, refresh data, etc.
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Could refresh categories here if needed
        // viewModel.refresh();
    }
}
