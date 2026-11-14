package com.unitconverter.app.presentation.sheet;

import android.app.Activity;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.unitconverter.app.R;
import com.unitconverter.app.settings.AppSettings;
import com.unitconverter.app.settings.ThemeManager;

/**
 * Look and feel: mood (with live previews drawn in each mood's own theme),
 * light/dark, number grouping, Indian units and Hindi greetings.
 *
 * Mood and appearance changes close the sheet and play the circular reveal
 * from the tapped control. Other changes are applied in place through
 * {@link Host}.
 */
public class SettingsSheet extends BottomSheetDialogFragment {

    /** Implemented by screens that need to refresh after a display setting changes. */
    public interface Host {
        void onDisplaySettingsChanged();
    }

    private AppSettings settings;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sheet_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        settings = AppSettings.of(requireContext());
        buildMoods(view.findViewById(R.id.moodRow));
        bindAppearance(view.findViewById(R.id.appearanceGroup));
        bindNumbers(view.findViewById(R.id.numbersGroup));

        MaterialSwitch indian = view.findViewById(R.id.indianUnitsSwitch);
        indian.setChecked(settings.showIndianUnits());
        indian.setOnCheckedChangeListener((b, on) -> {
            settings.setShowIndianUnits(on);
            notifyHost();
        });

        MaterialSwitch hindi = view.findViewById(R.id.hindiSwitch);
        hindi.setChecked(settings.hindiTouches());
        hindi.setOnCheckedChangeListener((b, on) -> {
            settings.setHindiTouches(on);
            notifyHost();
        });
    }

    private void buildMoods(LinearLayout row) {
        AppSettings.Mood current = settings.getMood();
        AppSettings.Mood[] moods = AppSettings.Mood.values();
        int[] names = {R.string.mood_jantar, R.string.mood_bazaar, R.string.mood_truck};
        int[] hints = {R.string.mood_jantar_hint, R.string.mood_bazaar_hint, R.string.mood_truck_hint};
        float dp = getResources().getDisplayMetrics().density;

        for (int i = 0; i < moods.length; i++) {
            final AppSettings.Mood mood = moods[i];
            // Inflate with the mood's own theme so the card previews the real thing
            ContextThemeWrapper themed = new ContextThemeWrapper(requireContext(), ThemeManager.themeFor(mood));
            View card = LayoutInflater.from(themed).inflate(R.layout.item_mood_option, row, false);
            ((TextView) card.findViewById(R.id.moodName)).setText(names[i]);
            ((TextView) card.findViewById(R.id.moodHint)).setText(hints[i]);
            boolean selected = mood == current;
            card.findViewById(R.id.moodCheck).setVisibility(selected ? View.VISIBLE : View.GONE);
            card.setSelected(selected);
            card.setContentDescription(getString(names[i]) + ". " + getString(hints[i])
                    + (selected ? ". " + getString(R.string.cd_selected) : ""));

            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) card.getLayoutParams();
            if (i < moods.length - 1) lp.setMarginEnd(Math.round(10 * dp));
            card.setLayoutParams(lp);

            card.setOnClickListener(v -> {
                if (mood == settings.getMood()) return;
                changeLook(v, () -> settings.setMood(mood));
            });
            row.addView(card);
        }
    }

    private void bindAppearance(MaterialButtonToggleGroup group) {
        switch (settings.getAppearance()) {
            case LIGHT: group.check(R.id.appearanceLight); break;
            case DARK: group.check(R.id.appearanceDark); break;
            default: group.check(R.id.appearanceSystem);
        }
        group.addOnButtonCheckedListener((g, id, checked) -> {
            if (!checked) return;
            AppSettings.Appearance a = id == R.id.appearanceLight ? AppSettings.Appearance.LIGHT
                    : id == R.id.appearanceDark ? AppSettings.Appearance.DARK
                    : AppSettings.Appearance.SYSTEM;
            if (a == settings.getAppearance()) return;
            changeLook(g.findViewById(id), () -> settings.setAppearance(a));
        });
    }

    private void bindNumbers(MaterialButtonToggleGroup group) {
        switch (settings.getNumberSystem()) {
            case INTERNATIONAL: group.check(R.id.numbersInternational); break;
            case INDIAN: group.check(R.id.numbersIndian); break;
            default: group.check(R.id.numbersAuto);
        }
        group.addOnButtonCheckedListener((g, id, checked) -> {
            if (!checked) return;
            settings.setNumberSystem(id == R.id.numbersInternational ? AppSettings.NumberSystem.INTERNATIONAL
                    : id == R.id.numbersIndian ? AppSettings.NumberSystem.INDIAN
                    : AppSettings.NumberSystem.AUTO);
            settings.applyGlobal();
            notifyHost();
        });
    }

    /** Closes the sheet and reveals the new look from the centre of {@code origin}. */
    private void changeLook(View origin, Runnable change) {
        Activity activity = requireActivity();
        int[] loc = new int[2];
        origin.getLocationInWindow(loc);
        // The sheet is its own window; offset by where that window sits on screen
        int[] win = new int[2];
        origin.getRootView().getLocationOnScreen(win);
        int[] act = new int[2];
        activity.getWindow().getDecorView().getLocationOnScreen(act);
        float x = loc[0] + win[0] - act[0] + origin.getWidth() / 2f;
        float y = loc[1] + win[1] - act[1] + origin.getHeight() / 2f;
        dismissAllowingStateLoss();
        // Post on the activity window: the sheet's own views are detached by now
        activity.getWindow().getDecorView().postDelayed(
                () -> ThemeManager.changeLook(activity, x, y, change), 180);
    }

    private void notifyHost() {
        if (getActivity() instanceof Host) {
            ((Host) getActivity()).onDisplaySettingsChanged();
        }
    }
}
