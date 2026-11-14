package com.unitconverter.app.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import com.unitconverter.app.util.NumberFormatter;

import java.util.Locale;

/**
 * User preferences: look (mood + light/dark) and regional behaviour
 * (number grouping, Indian units, Hindi touches).
 *
 * "Auto" options follow the device: India region or Hindi language turns on
 * lakh/crore grouping and the Hindi greeting; everyone else gets
 * international grouping and a greeting in their own language.
 */
public final class AppSettings {

    public enum Mood { JANTAR, BAZAAR, TRUCK }

    /** Light/dark preference, mapped to AppCompatDelegate night modes. */
    public enum Appearance { SYSTEM, LIGHT, DARK }

    public enum NumberSystem { AUTO, INTERNATIONAL, INDIAN }

    private static final String PREFS = "smartconvert_settings";
    private static final String KEY_MOOD = "mood";
    private static final String KEY_APPEARANCE = "appearance";
    private static final String KEY_NUMBERS = "number_system";
    private static final String KEY_INDIAN_UNITS = "indian_units";
    private static final String KEY_HINDI = "hindi_touches";

    private final SharedPreferences prefs;

    private AppSettings(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static AppSettings of(Context context) {
        return new AppSettings(context);
    }

    // ----- Mood -----
    public Mood getMood() {
        return parse(Mood.class, prefs.getString(KEY_MOOD, null), Mood.JANTAR);
    }

    public void setMood(Mood mood) {
        prefs.edit().putString(KEY_MOOD, mood.name()).apply();
    }

    // ----- Appearance -----
    public Appearance getAppearance() {
        return parse(Appearance.class, prefs.getString(KEY_APPEARANCE, null), Appearance.SYSTEM);
    }

    public void setAppearance(Appearance appearance) {
        prefs.edit().putString(KEY_APPEARANCE, appearance.name()).apply();
    }

    public int getNightMode() {
        switch (getAppearance()) {
            case LIGHT: return AppCompatDelegate.MODE_NIGHT_NO;
            case DARK: return AppCompatDelegate.MODE_NIGHT_YES;
            default: return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
    }

    // ----- Numbers -----
    public NumberSystem getNumberSystem() {
        return parse(NumberSystem.class, prefs.getString(KEY_NUMBERS, null), NumberSystem.AUTO);
    }

    public void setNumberSystem(NumberSystem system) {
        prefs.edit().putString(KEY_NUMBERS, system.name()).apply();
    }

    /** The grouping actually in effect after resolving AUTO against the device. */
    public NumberFormatter.Grouping resolveGrouping() {
        switch (getNumberSystem()) {
            case INDIAN: return NumberFormatter.Grouping.INDIAN;
            case INTERNATIONAL: return NumberFormatter.Grouping.INTERNATIONAL;
            default: return isIndianDevice()
                    ? NumberFormatter.Grouping.INDIAN
                    : NumberFormatter.Grouping.INTERNATIONAL;
        }
    }

    // ----- Units and language -----
    public boolean showIndianUnits() {
        return prefs.getBoolean(KEY_INDIAN_UNITS, true);
    }

    public void setShowIndianUnits(boolean show) {
        prefs.edit().putBoolean(KEY_INDIAN_UNITS, show).apply();
    }

    public boolean hindiTouches() {
        // Default on in India, off elsewhere; anyone can switch it.
        return prefs.getBoolean(KEY_HINDI, isIndianDevice());
    }

    public void setHindiTouches(boolean on) {
        prefs.edit().putBoolean(KEY_HINDI, on).apply();
    }

    /** Region India or Hindi language. */
    public static boolean isIndianDevice() {
        Locale locale = Locale.getDefault();
        return "IN".equalsIgnoreCase(locale.getCountry()) || "hi".equalsIgnoreCase(locale.getLanguage());
    }

    /** Applies everything that is process-wide (night mode, number grouping). */
    public void applyGlobal() {
        AppCompatDelegate.setDefaultNightMode(getNightMode());
        NumberFormatter.setGrouping(resolveGrouping());
    }

    private static <E extends Enum<E>> E parse(Class<E> type, String value, E fallback) {
        if (value == null) return fallback;
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
