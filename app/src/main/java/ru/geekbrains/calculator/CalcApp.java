package ru.geekbrains.calculator;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

/** Точка входа приложения: применяет сохранённый режим темы до старта экранов. */
public class CalcApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        int mode = CalcPreferences.from(this).getThemeMode();
        if (AppCompatDelegate.getDefaultNightMode() != mode) {
            AppCompatDelegate.setDefaultNightMode(mode);
        }
    }
}
