package com.unitconverter.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.transition.platform.MaterialContainerTransform;
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;
import com.unitconverter.app.data.provider.UnitProvider;
import com.unitconverter.app.data.repository.ConversionRepository;
import com.unitconverter.app.domain.model.ConversionResult;
import com.unitconverter.app.domain.model.Unit;
import com.unitconverter.app.domain.model.UnitCategory;
import com.unitconverter.app.presentation.sheet.UnitPickerSheet;
import com.unitconverter.app.presentation.viewmodel.ConversionViewModel;
import com.unitconverter.app.presentation.widget.ArcDialView;
import com.unitconverter.app.settings.AppSettings;
import com.unitconverter.app.settings.ThemeManager;
import com.unitconverter.app.util.NumberFormatter;
import com.unitconverter.app.util.WindowInsetsHelper;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

/**
 * Conversion screen.
 *
 * Enters by growing out of the tapped home tile. The hero (in the category
 * hue) shows a magnitude dial, the result in the mood's display face and the
 * result in words ("12.5 lakh"). Units are chosen in a searchable sheet;
 * "In every unit" lists the value converted to all units of the category.
 */
public class ConversionActivity extends AppCompatActivity {

    private ConversionViewModel viewModel;
    private UnitCategory category;
    private AppSettings settings;
    private int hue;

    private TextInputEditText inputValue;
    private TextInputLayout inputLayout;
    private TextView resultValue;
    private TextView resultUnit;
    private TextView resultWords;
    private TextView fromSymbol;
    private TextView fromName;
    private TextView toSymbol;
    private TextView toName;
    private View fromUnitCard;
    private View toUnitCard;
    private View swapButton;
    private View copyButton;
    private View shareButton;
    private ArcDialView dial;
    private AllUnitsAdapter allUnitsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.apply(this);
        setupContainerTransform();
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT));
        setContentView(R.layout.activity_conversion);
        WindowInsetsHelper.applyHeroInsets(findViewById(R.id.conversionRoot), findViewById(R.id.convHeroContent));

        String categoryName = getIntent().getStringExtra("CATEGORY");
        try {
            category = UnitCategory.valueOf(categoryName);
        } catch (IllegalArgumentException | NullPointerException e) {
            Snackbar.make(findViewById(android.R.id.content), R.string.error_no_category, Snackbar.LENGTH_LONG).show();
            finish();
            return;
        }
        settings = AppSettings.of(this);
        hue = ThemeManager.hue(this, category);

        initializeViews();
        styleHero();
        setupQuickValues();
        setupUnitPicking();

        viewModel = new ViewModelProvider(this).get(ConversionViewModel.class);
        viewModel.setCategory(category);
        setupInput(); // before reuse pre-fill, so the pre-filled value converts
        if (savedInstanceState == null) {
            applyReuseExtras();
        }
        observeViewModel();
        setupActions();
    }

    /** The home tile grows into this screen and shrinks back on return. */
    private void setupContainerTransform() {
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        findViewById(android.R.id.content).setTransitionName(MainActivity.TRANSITION_NAME);
        setEnterSharedElementCallback(new MaterialContainerTransformSharedElementCallback());

        MaterialContainerTransform enter = new MaterialContainerTransform();
        enter.addTarget(android.R.id.content);
        enter.setDuration(480);
        enter.setFadeMode(MaterialContainerTransform.FADE_MODE_THROUGH);
        MaterialContainerTransform exit = new MaterialContainerTransform();
        exit.addTarget(android.R.id.content);
        exit.setDuration(380);
        exit.setFadeMode(MaterialContainerTransform.FADE_MODE_THROUGH);
        window.setSharedElementEnterTransition(enter);
        window.setSharedElementReturnTransition(exit);
    }

    private void initializeViews() {
        inputValue = findViewById(R.id.inputValue);
        inputLayout = findViewById(R.id.inputLayout);
        resultValue = findViewById(R.id.resultValue);
        resultUnit = findViewById(R.id.resultUnit);
        resultWords = findViewById(R.id.resultWords);
        fromSymbol = findViewById(R.id.fromSymbol);
        fromName = findViewById(R.id.fromName);
        toSymbol = findViewById(R.id.toSymbol);
        toName = findViewById(R.id.toName);
        fromUnitCard = findViewById(R.id.fromUnitCard);
        toUnitCard = findViewById(R.id.toUnitCard);
        swapButton = findViewById(R.id.swapButton);
        copyButton = findViewById(R.id.copyButton);
        shareButton = findViewById(R.id.shareButton);
        dial = findViewById(R.id.dial);

        findViewById(R.id.backButton).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        RecyclerView all = findViewById(R.id.allUnitsList);
        all.setLayoutManager(new LinearLayoutManager(this));
        allUnitsAdapter = new AllUnitsAdapter();
        all.setAdapter(allUnitsAdapter);

        // Keep the swap knob centred on the From/To divider, wherever it lands
        View divider = findViewById(R.id.fromToDivider);
        divider.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) ->
                swapButton.setY(v.getY() + v.getHeight() / 2f - swapButton.getHeight() / 2f));
    }

    private void styleHero() {
        float dp = getResources().getDisplayMetrics().density;
        MaterialShapeDrawable heroBg = new MaterialShapeDrawable(ShapeAppearanceModel.builder()
                .setBottomLeftCornerSize(36 * dp)
                .setBottomRightCornerSize(36 * dp)
                .build());
        heroBg.setFillColor(ColorStateList.valueOf(hue));
        findViewById(R.id.convHero).setBackground(heroBg);

        ((TextView) findViewById(R.id.categoryTitle)).setText(category.getDisplayName());
        ((TextView) findViewById(R.id.watermark)).setText(category.getSymbol());

        int total = 0, indian = 0;
        for (Unit u : visibleUnits()) {
            total++;
            if (u.getSystem() == Unit.MeasureSystem.INDIAN) indian++;
        }
        ((TextView) findViewById(R.id.categorySubtitle)).setText(indian > 0
                ? getString(R.string.units_count_indian, total, indian)
                : getString(R.string.units_count, total));

        ColorStateList hueList = ColorStateList.valueOf(hue);
        fromSymbol.setBackgroundTintList(hueList);
        toSymbol.setBackgroundTintList(hueList);
        dial.setValue(null);
    }

    private List<Unit> visibleUnits() {
        List<Unit> out = new ArrayList<>();
        boolean showIndian = settings.showIndianUnits();
        for (Unit u : UnitProvider.getInstance().getUnitsForCategory(category)) {
            if (u.getSystem() == Unit.MeasureSystem.INDIAN && !showIndian) continue;
            out.add(u);
        }
        return out;
    }

    // ------------------------------------------------------------------ input

    private void setupQuickValues() {
        ChipGroup group = findViewById(R.id.quickChips);
        String[] values = {"1", "5", "10", "100", "1000"};
        for (String v : values) {
            Chip chip = new Chip(this, null, com.google.android.material.R.attr.chipStyle);
            chip.setText(NumberFormatter.formatDisplay(Double.parseDouble(v)));
            chip.setChipBackgroundColor(ColorStateList.valueOf(ThemeManager.color(this, R.attr.scSurfaceAlt)));
            chip.setTextColor(ThemeManager.color(this, R.attr.scInk));
            chip.setChipStrokeWidth(0);
            chip.setOnClickListener(x -> {
                inputValue.setText(v);
                inputValue.setSelection(inputValue.getText() != null ? inputValue.getText().length() : 0);
            });
            group.addView(chip);
        }
    }

    private void setupInput() {
        inputValue.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) {
                viewModel.setInputValue(s.toString());
                allUnitsAdapter.refresh();
            }
        });
    }

    /** History "Reuse" opens this screen with an earlier conversion's units and value. */
    private void applyReuseExtras() {
        String from = getIntent().getStringExtra("FROM_UNIT");
        String to = getIntent().getStringExtra("TO_UNIT");
        if (from != null && to != null) {
            viewModel.prefillUnits(from, to);
        }
        if (getIntent().hasExtra("INPUT_VALUE")) {
            double value = getIntent().getDoubleExtra("INPUT_VALUE", 0);
            String text = BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
            char separator = DecimalFormatSymbols.getInstance().getDecimalSeparator();
            inputValue.setText(text.replace('.', separator));
        }
    }

    // ------------------------------------------------------------------ units

    private void setupUnitPicking() {
        fromUnitCard.setOnClickListener(v -> openPicker("from", viewModel.getFromUnit().getValue()));
        toUnitCard.setOnClickListener(v -> openPicker("to", viewModel.getToUnit().getValue()));

        getSupportFragmentManager().setFragmentResultListener(UnitPickerSheet.REQUEST_KEY, this, (key, b) -> {
            Unit unit = UnitProvider.getInstance().findUnit(category, b.getString(UnitPickerSheet.KEY_UNIT_NAME));
            if (unit == null) return;
            if ("from".equals(b.getString(UnitPickerSheet.KEY_TARGET))) {
                viewModel.setFromUnit(unit);
            } else {
                viewModel.setToUnit(unit);
            }
            allUnitsAdapter.refresh();
        });
    }

    private void openPicker(String target, Unit selected) {
        UnitPickerSheet.newInstance(category, target, selected != null ? selected.getName() : "")
                .show(getSupportFragmentManager(), "unit_picker");
    }

    private void bindUnitCard(TextView symbol, TextView name, View card, Unit unit, int labelRes) {
        symbol.setText(unit.getSymbol());
        name.setText(unit.getName());
        card.setContentDescription(getString(R.string.cd_unit_picker, getString(labelRes), unit.getName()));
    }

    // -------------------------------------------------------------- observers

    private void observeViewModel() {
        viewModel.getFromUnit().observe(this, unit -> {
            if (unit != null) bindUnitCard(fromSymbol, fromName, fromUnitCard, unit, R.string.from_label);
            allUnitsAdapter.refresh();
        });
        viewModel.getToUnit().observe(this, unit -> {
            if (unit != null) bindUnitCard(toSymbol, toName, toUnitCard, unit, R.string.to_label);
            allUnitsAdapter.refresh();
        });

        viewModel.getResult().observe(this, this::showResult);
        if (viewModel.getResult().getValue() == null) {
            showResult(null); // LiveData starts empty; draw the placeholder state once
        }

        viewModel.getIsValidInput().observe(this, isValid ->
                inputLayout.setError(Boolean.TRUE.equals(isValid) ? null : getString(R.string.error_invalid_input)));

        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void showResult(ConversionResult result) {
        boolean has = result != null;
        if (has) {
            double out = result.getOutputValue();
            resultValue.setText(NumberFormatter.formatDisplay(out));
            resultValue.setAlpha(1f);
            resultValue.setContentDescription(null);
            resultUnit.setText(result.getToUnit().getDisplayText());
            String words = NumberFormatter.inWords(out);
            resultWords.setText(words.isEmpty() ? "" : words + " " + result.getToUnit().getSymbol());
            dial.setValue(out);
        } else {
            resultValue.setText(R.string.result_placeholder);
            resultValue.setAlpha(0.45f);
            resultValue.setContentDescription(getString(R.string.no_result));
            Unit to = viewModel.getToUnit().getValue();
            resultUnit.setText(to != null ? to.getDisplayText() : "");
            resultWords.setText("");
            dial.setValue(null);
        }
        copyButton.setEnabled(has);
        shareButton.setEnabled(has);
        copyButton.setAlpha(has ? 1f : 0.5f);
        shareButton.setAlpha(has ? 1f : 0.5f);
    }

    // ---------------------------------------------------------------- actions

    private void setupActions() {
        swapButton.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            // The knob turns half a revolution; the unit rows trade places
            v.animate().rotationBy(180f).setDuration(420)
                    .setInterpolator(new OvershootInterpolator(1.4f)).start();
            float shift = toUnitCard.getY() - fromUnitCard.getY();
            fromUnitCard.setTranslationY(shift);
            toUnitCard.setTranslationY(-shift);
            viewModel.swapUnits();
            fromUnitCard.animate().translationY(0).setDuration(380).start();
            toUnitCard.animate().translationY(0).setDuration(380).start();
        });

        copyButton.setOnClickListener(v -> {
            String text = shareText(false);
            if (text == null) return;
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText(getString(R.string.result_label), text));
                Snackbar.make(findViewById(android.R.id.content),
                        getString(R.string.result_copied, text), Snackbar.LENGTH_SHORT).show();
            }
        });

        shareButton.setOnClickListener(v -> {
            String text = shareText(true);
            if (text == null) return;
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("text/plain");
            send.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(send, getString(R.string.share_result)));
        });
    }

    /** "1,250 km = 12,50,000 m" (plus words and app name when sharing). */
    private String shareText(boolean full) {
        ConversionResult r = viewModel.getResult().getValue();
        if (r == null) return null;
        String out = NumberFormatter.formatDisplay(r.getOutputValue()) + " " + r.getToUnit().getSymbol();
        if (!full) return out;
        String line = NumberFormatter.formatDisplay(r.getInputValue()) + " " + r.getFromUnit().getSymbol()
                + " = " + out;
        String words = NumberFormatter.inWords(r.getOutputValue());
        if (!words.isEmpty()) line += " (" + words + ")";
        return line + "\n" + getString(R.string.app_name);
    }

    // --------------------------------------------------------- every unit list

    /** The current value converted to every visible unit; tap a row to convert to it. */
    private class AllUnitsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<Unit> units = new ArrayList<>();
        private Double value;
        private Unit from;
        private Unit to;

        void refresh() {
            units = visibleUnits();
            from = viewModel != null ? viewModel.getFromUnit().getValue() : null;
            to = viewModel != null ? viewModel.getToUnit().getValue() : null;
            value = null;
            Editable text = inputValue.getText();
            if (text != null && !text.toString().trim().isEmpty()
                    && !NumberFormatter.isIncomplete(text.toString())) {
                try {
                    value = NumberFormatter.parse(text.toString());
                } catch (NumberFormatException ignored) {
                }
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_unit_value, parent, false);
            return new RecyclerView.ViewHolder(v) { };
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Unit u = units.get(position);
            View v = holder.itemView;
            TextView symbol = v.findViewById(R.id.rowSymbol);
            symbol.setText(u.getSymbol());
            symbol.setBackgroundTintList(ColorStateList.valueOf(hue));
            ((TextView) v.findViewById(R.id.rowName)).setText(u.getName());
            TextView note = v.findViewById(R.id.rowNote);
            String noteText = u.getNote().isEmpty()
                    ? (u.getSystem() == Unit.MeasureSystem.INDIAN ? getString(R.string.system_indian) : "")
                    : u.getNote();
            note.setText(noteText);
            note.setVisibility(noteText.isEmpty() ? View.GONE : View.VISIBLE);

            TextView val = v.findViewById(R.id.rowValue);
            String shown = "–";
            if (value != null && from != null) {
                try {
                    shown = NumberFormatter.formatDisplay(
                            ConversionRepository.getInstance().convert(value, from, u).getOutputValue());
                } catch (IllegalArgumentException ignored) {
                }
            }
            val.setText(shown);
            boolean isTarget = to != null && to.equals(u);
            val.setTextColor(isTarget ? hue : ThemeManager.color(v.getContext(), R.attr.scInk));

            v.setContentDescription(u.getName() + ", " + shown);
            v.setOnClickListener(x -> {
                viewModel.setToUnit(u);
                findViewById(R.id.conversionScroll).post(() ->
                        ((androidx.core.widget.NestedScrollView) findViewById(R.id.conversionScroll)).smoothScrollTo(0, 0));
            });
        }

        @Override
        public int getItemCount() {
            return units.size();
        }
    }
}
