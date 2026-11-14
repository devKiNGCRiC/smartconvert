package com.unitconverter.app;

import android.app.Application;

import com.unitconverter.app.settings.AppSettings;

/** Applies saved look and number settings before any screen is created. */
public class SmartConvertApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppSettings.of(this).applyGlobal();
    }
}
