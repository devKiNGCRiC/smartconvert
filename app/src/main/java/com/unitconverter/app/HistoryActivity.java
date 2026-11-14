package com.unitconverter.app;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.unitconverter.app.data.database.ConversionHistoryEntity;
import com.unitconverter.app.presentation.viewmodel.MainViewModel;
import com.unitconverter.app.util.WindowInsetsHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Activity for displaying conversion history and favorites with tabbed interface.
 * 
 * FEATURES:
 * - TabLayout with "All History" and "Favorites" tabs
 * - Search functionality to filter conversions
 * - Swipe-to-delete with undo functionality
 * - Empty state views for both tabs
 * - Clear all button with confirmation dialog
 * 
 * ARCHITECTURE:
 * - Uses ViewPager2 with FragmentStateAdapter for tab content
 * - Shares MainViewModel with MainActivity for consistent state
 * - Implements custom RecyclerView adapter for history items
 */
public class HistoryActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputLayout searchBarLayout;
    private TextInputEditText searchInput;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ExtendedFloatingActionButton fabClearAll;
    
    private MainViewModel viewModel;
    private int currentTabPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);
        WindowInsetsHelper.applySystemBarPadding(findViewById(R.id.historyRoot));

        initializeViews();
        setupViewModel();
        setupToolbar();
        setupViewPager();
        setupSearch();
        setupFab();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        searchBarLayout = findViewById(R.id.searchBarLayout);
        searchInput = findViewById(R.id.searchInput);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        fabClearAll = findViewById(R.id.fabClearAll);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void setupViewPager() {
        HistoryPagerAdapter pagerAdapter = new HistoryPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.tab_all_history);
                    break;
                case 1:
                    tab.setText(R.string.tab_favorites);
                    break;
            }
        }).attach();

        // Track current tab for clear all functionality
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentTabPosition = position;
                updateFabText();
            }
        });
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s.toString());
                // Notify fragments to update their lists
                // This will be handled by the fragments observing the search query
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFab() {
        updateFabText();
        fabClearAll.setOnClickListener(v -> showClearAllDialog());
    }

    private void updateFabText() {
        if (currentTabPosition == 0) {
            fabClearAll.setText(R.string.clear_all);
        } else {
            fabClearAll.setText(R.string.clear_favorites);
        }
    }

    private void showClearAllDialog() {
        String title = getString(R.string.confirm_clear_all);
        String message = currentTabPosition == 0 
            ? getString(R.string.confirm_clear_message)
            : getString(R.string.confirm_clear_all_including_favorites);

        new MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.clear_all, (dialog, which) -> clearAll())
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void clearAll() {
        if (currentTabPosition == 0) {
            // Clear all non-favorite history
            viewModel.clearNonFavoriteHistory();
        } else {
            // Clear all favorites
            viewModel.clearFavorites();
        }
        Snackbar.make(fabClearAll, R.string.history_cleared, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * ViewPager2 adapter for managing history fragments.
     * Creates two fragments: All History and Favorites.
     */
    private class HistoryPagerAdapter extends FragmentStateAdapter {

        public HistoryPagerAdapter(AppCompatActivity activity) {
            super(activity);
        }

        @Override
        public Fragment createFragment(int position) {
            return HistoryListFragment.newInstance(position == 1);
        }

        @Override
        public int getItemCount() {
            return 2; // All History and Favorites
        }
    }

    /**
     * Fragment for displaying a list of history items.
     * Can show either all history or just favorites.
     */
    public static class HistoryListFragment extends Fragment {
        
        private static final String ARG_SHOW_FAVORITES = "show_favorites";
        
        private RecyclerView recyclerView;
        private LinearLayout emptyStateLayout;
        private TextView emptyStateTitle;
        private TextView emptyStateMessage;
        private ImageView emptyStateIcon;
        
        private HistoryAdapter adapter;
        private MainViewModel viewModel;
        private List<ConversionHistoryEntity> latestHistory = new ArrayList<>();
        private String searchQuery = "";
        private boolean showFavoritesOnly;

        public static HistoryListFragment newInstance(boolean showFavoritesOnly) {
            HistoryListFragment fragment = new HistoryListFragment();
            Bundle args = new Bundle();
            args.putBoolean(ARG_SHOW_FAVORITES, showFavoritesOnly);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                showFavoritesOnly = getArguments().getBoolean(ARG_SHOW_FAVORITES);
            }
        }

        @Override
        public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_history_list, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            initializeViews(view);
            setupViewModel();
            setupRecyclerView();
            setupSwipeToDelete();
            observeHistory();
        }

        private void initializeViews(View view) {
            recyclerView = view.findViewById(R.id.historyRecyclerView);
            emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
            emptyStateTitle = view.findViewById(R.id.emptyStateTitle);
            emptyStateMessage = view.findViewById(R.id.emptyStateMessage);
            emptyStateIcon = view.findViewById(R.id.emptyStateIcon);

            // Update empty state text based on tab
            if (showFavoritesOnly) {
                emptyStateTitle.setText(R.string.empty_favorites);
                emptyStateMessage.setText(R.string.empty_favorites_message);
                emptyStateIcon.setImageResource(R.drawable.ic_star_border);
            }
        }

        private void setupViewModel() {
            viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        }

        private void setupRecyclerView() {
            adapter = new HistoryAdapter(new ArrayList<>(), viewModel, getContext());
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);
        }

        private void setupSwipeToDelete() {
            ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                    RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    int position = viewHolder.getAdapterPosition();
                    ConversionHistoryEntity deletedItem = adapter.getItem(position);
                    
                    // Delete from database
                    viewModel.deleteHistoryItem(deletedItem);

                    // Show undo snackbar
                    Snackbar.make(recyclerView, R.string.item_deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo, v -> {
                            viewModel.restoreHistoryItem(deletedItem);
                        })
                        .show();
                }
            };

            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeCallback);
            itemTouchHelper.attachToRecyclerView(recyclerView);
        }

        private void observeHistory() {
            if (showFavoritesOnly) {
                viewModel.getFavorites().observe(getViewLifecycleOwner(), this::onHistoryChanged);
            } else {
                viewModel.getRecentConversions().observe(getViewLifecycleOwner(), this::onHistoryChanged);
            }
            viewModel.getSearchQuery().observe(getViewLifecycleOwner(), query -> {
                searchQuery = query == null ? "" : query;
                updateList();
            });
        }

        private void onHistoryChanged(List<ConversionHistoryEntity> history) {
            latestHistory = history == null ? new ArrayList<>() : history;
            updateList();
        }

        /** Applies the current search text to the latest data and shows the list or empty state. */
        private void updateList() {
            List<ConversionHistoryEntity> visible = new ArrayList<>();
            for (ConversionHistoryEntity item : latestHistory) {
                if (searchQuery.isEmpty() || matchesSearch(item, searchQuery)) {
                    visible.add(item);
                }
            }

            boolean empty = visible.isEmpty();
            if (empty && !latestHistory.isEmpty()) {
                // Data exists but the search matched nothing
                emptyStateTitle.setText(R.string.no_search_results);
                emptyStateMessage.setText(R.string.no_search_results_message);
            } else if (showFavoritesOnly) {
                emptyStateTitle.setText(R.string.empty_favorites);
                emptyStateMessage.setText(R.string.empty_favorites_message);
            } else {
                emptyStateTitle.setText(R.string.empty_history);
                emptyStateMessage.setText(R.string.empty_history_message);
            }
            recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
            emptyStateLayout.setVisibility(empty ? View.VISIBLE : View.GONE);
            adapter.setHistoryList(visible);
        }

        private boolean matchesSearch(ConversionHistoryEntity item, String query) {
            String lowerQuery = query.toLowerCase(Locale.ROOT);
            return item.getCategory().toLowerCase(Locale.ROOT).contains(lowerQuery)
                || item.getFromUnit().toLowerCase(Locale.ROOT).contains(lowerQuery)
                || item.getToUnit().toLowerCase(Locale.ROOT).contains(lowerQuery)
                || String.valueOf(item.getInputValue()).contains(lowerQuery)
                || String.valueOf(item.getResultValue()).contains(lowerQuery);
        }
    }
}
