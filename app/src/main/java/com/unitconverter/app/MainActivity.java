package com.unitconverter.app;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.widget.TextViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;
import com.unitconverter.app.data.database.ConversionHistoryEntity;
import com.unitconverter.app.domain.model.UnitCategory;
import com.unitconverter.app.presentation.adapter.CategoryAdapter;
import com.unitconverter.app.presentation.adapter.RecentConversionsAdapter;
import com.unitconverter.app.presentation.sheet.SettingsSheet;
import com.unitconverter.app.presentation.viewmodel.MainViewModel;
import com.unitconverter.app.settings.AppSettings;
import com.unitconverter.app.settings.ThemeManager;
import com.unitconverter.app.util.Greetings;
import com.unitconverter.app.util.WindowInsetsHelper;

import java.util.Calendar;
import java.util.List;

/**
 * Home screen.
 *
 * Hero: a sundial showing the real time, a greeting that cycles through
 * hellos from around the world and settles on the viewer's own, and a
 * measurement fact of the day. Below: the bento category grid (tiles grow
 * into the conversion screen) and recent conversions as tickets.
 */
public class MainActivity extends AppCompatActivity implements
        CategoryAdapter.OnCategoryClickListener,
        RecentConversionsAdapter.OnConversionClickListener,
        SettingsSheet.Host {

    static final String TRANSITION_NAME = "category_container";

    private RecyclerView categoriesRecyclerView;
    private LinearLayout recentConversionsSection;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private TextSwitcher greeting;
    private TextView greetingSub;

    private MainViewModel viewModel;
    private AppSettings settings;
    private CategoryAdapter adapter;
    private RecentConversionsAdapter recentAdapter;
    private boolean firstShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        ThemeManager.apply(this);
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        setExitSharedElementCallback(new MaterialContainerTransformSharedElementCallback());
        getWindow().setSharedElementsUseOverlay(false);
        super.onCreate(savedInstanceState);

        // Light status-bar icons: they sit on the dark hero in every mood
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT));
        setContentView(R.layout.activity_main);
        View scrim = findViewById(R.id.statusScrim);
        WindowInsetsHelper.applyHeroInsets(findViewById(R.id.main), findViewById(R.id.heroContent), scrim);
        View hero = findViewById(R.id.hero);
        ((androidx.core.widget.NestedScrollView) findViewById(R.id.homeScroll)).setOnScrollChangeListener(
                (androidx.core.widget.NestedScrollView.OnScrollChangeListener) (v, x, y, ox, oy) -> {
                    // Fade the status-bar backing in over the last 48dp of the hero
                    float fadeStart = hero.getHeight() - scrim.getHeight() - 48 * getResources().getDisplayMetrics().density;
                    float t = (y - fadeStart) / (48 * getResources().getDisplayMetrics().density);
                    scrim.setAlpha(Math.max(0f, Math.min(1f, t)));
                });

        settings = AppSettings.of(this);
        firstShow = savedInstanceState == null;

        initializeViews();
        setupHero();
        setupCategoryGrid();

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        observeViewModel();

        ThemeManager.playPendingReveal(this);
    }

    private void initializeViews() {
        categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView);
        recentConversionsSection = findViewById(R.id.recentConversionsSection);
        progressBar = findViewById(R.id.progressBar);
        emptyStateText = findViewById(R.id.emptyStateText);
        greeting = findViewById(R.id.greeting);
        greetingSub = findViewById(R.id.greetingSub);

        RecyclerView recent = findViewById(R.id.recentConversionsRecyclerView);
        recent.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recentAdapter = new RecentConversionsAdapter(this);
        recent.setAdapter(recentAdapter);

        View.OnClickListener openHistory = v -> startActivity(new Intent(this, HistoryActivity.class));
        MaterialButton viewAll = findViewById(R.id.viewAllHistoryButton);
        viewAll.setOnClickListener(openHistory);
        findViewById(R.id.historyButton).setOnClickListener(openHistory);
        findViewById(R.id.settingsButton).setOnClickListener(v ->
                new SettingsSheet().show(getSupportFragmentManager(), "settings"));
    }

    // ------------------------------------------------------------------ hero

    private void setupHero() {
        greeting.setFactory(() -> {
            TextView tv = new TextView(this);
            TextViewCompat.setTextAppearance(tv, R.style.TextAppearance_SC_Display);
            tv.setTextColor(ThemeManager.color(this, R.attr.scOnHero));
            tv.setTextSize(46);
            tv.setGravity(Gravity.START);
            tv.setMaxLines(1);
            return tv;
        });
        greeting.setInAnimation(slide(true));
        greeting.setOutAnimation(slide(false));

        boolean indianGreeting = AppSettings.isIndianDevice() || settings.hindiTouches();
        if (firstShow) {
            playGreetingIntro(Greetings.intro(indianGreeting));
        } else {
            greeting.setCurrentText(Greetings.home(indianGreeting));
        }
        bindGreetingSub();

        String[] facts = getResources().getStringArray(R.array.facts);
        int day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        ((TextView) findViewById(R.id.factText)).setText(facts[day % facts.length]);
    }

    private void bindGreetingSub() {
        Greetings.DayPart part = Greetings.dayPart();
        String english = Greetings.english(part);
        greetingSub.setText(settings.hindiTouches()
                ? Greetings.hindi(part) + ", " + english.toLowerCase()
                : english);
    }

    /** "Namaste" → "Hola" → "Bonjour" … settling on the viewer's own hello. */
    private void playGreetingIntro(List<String> words) {
        greeting.setCurrentText(words.get(0));
        greeting.setContentDescription(words.get(words.size() - 1));
        for (int i = 1; i < words.size(); i++) {
            final String w = words.get(i);
            greeting.postDelayed(() -> greeting.setText(w), 700L + (i - 1) * 520L);
        }
    }

    private Animation slide(boolean in) {
        AnimationSet set = new AnimationSet(true);
        set.addAnimation(new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, in ? 0.6f : 0f, Animation.RELATIVE_TO_SELF, in ? 0f : -0.6f));
        set.addAnimation(new AlphaAnimation(in ? 0f : 1f, in ? 1f : 0f));
        set.setDuration(320);
        set.setInterpolator(new DecelerateInterpolator());
        return set;
    }

    // ------------------------------------------------------------- categories

    private void setupCategoryGrid() {
        GridLayoutManager grid = new GridLayoutManager(this, 6);
        grid.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return CategoryAdapter.spanFor(position);
            }
        });
        categoriesRecyclerView.setLayoutManager(grid);
        adapter = new CategoryAdapter(CategoryAdapter.ORDER, this,
                settings.showIndianUnits(), firstShow);
        categoriesRecyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getLoading().observe(this, isLoading ->
                progressBar.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE));

        viewModel.getCategories().observe(this, categories -> {
            boolean empty = categories == null || categories.length == 0;
            emptyStateText.setVisibility(empty ? View.VISIBLE : View.GONE);
            categoriesRecyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
        });

        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(categoriesRecyclerView, error, Snackbar.LENGTH_LONG)
                        .setAction("Retry", v -> viewModel.refresh())
                        .show();
            }
        });

        viewModel.getRecentConversions().observe(this, conversions -> {
            boolean has = conversions != null && !conversions.isEmpty();
            recentConversionsSection.setVisibility(has ? View.VISIBLE : View.GONE);
            recentAdapter.setConversions(conversions);
        });
    }

    @Override
    public void onCategoryClick(UnitCategory category, View tile) {
        Intent intent = new Intent(this, ConversionActivity.class);
        intent.putExtra("CATEGORY", category.name());
        // The tile grows into the conversion screen
        tile.setTransitionName(TRANSITION_NAME);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, tile, TRANSITION_NAME);
        startActivity(intent, options.toBundle());
        tile.postDelayed(() -> tile.setTransitionName("tile_" + category.name()), 1000);
    }

    @Override
    public void onConversionClick(ConversionHistoryEntity conversion) {
        try {
            UnitCategory.valueOf(conversion.getCategory());
        } catch (IllegalArgumentException e) {
            Snackbar.make(categoriesRecyclerView, "Invalid category", Snackbar.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, ConversionActivity.class);
        intent.putExtra("CATEGORY", conversion.getCategory());
        intent.putExtra("FROM_UNIT", conversion.getFromUnit());
        intent.putExtra("TO_UNIT", conversion.getToUnit());
        intent.putExtra("INPUT_VALUE", conversion.getInputValue());
        startActivity(intent);
    }

    @Override
    public void onFavoriteClick(ConversionHistoryEntity conversion) {
        viewModel.toggleFavorite(conversion);
        Snackbar.make(categoriesRecyclerView,
                conversion.isFavorite() ? R.string.removed_from_favorites : R.string.added_to_favorites,
                Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDisplaySettingsChanged() {
        adapter.setShowIndianUnits(settings.showIndianUnits());
        recentAdapter.notifyDataSetChanged();
        boolean indianGreeting = AppSettings.isIndianDevice() || settings.hindiTouches();
        greeting.setText(Greetings.home(indianGreeting));
        bindGreetingSub();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Relative times and number style may have changed while away
        recentAdapter.notifyDataSetChanged();
    }
}
