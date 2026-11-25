package ru.geekbrains.calculator;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Хранилище настроек приложения: тема, память калькулятора (MR/M+/M−),
 * звук и вибрация клавиш, последний ввод.
 * <p>
 * Тонкая обёртка над {@link SharedPreferences}: ключи описаны в одном месте,
 * а не размазаны по Activity.
 */
public final class CalcPreferences {

    /** Режимы темы. */
    public static final int THEME_SYSTEM = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    public static final int THEME_LIGHT = AppCompatDelegate.MODE_NIGHT_NO;
    public static final int THEME_DARK = AppCompatDelegate.MODE_NIGHT_YES;

    private static final String PREFS_NAME = "prefs.xml";

    /** Ключ «тёмная тема». Имя оставлено прежним ради совместимости со старыми версиями. */
    public static final String pref_name = "theme";

    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_INPUT = "last_input";
    private static final String KEY_MEMORY = "memory";
    private static final String KEY_SOUND = "key_sound";
    private static final String KEY_VIBRATION = "key_vibration";

    private final SharedPreferences preferences;

    private CalcPreferences(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public static CalcPreferences from(Context context) {
        return new CalcPreferences(context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE));
    }

    // ------------------------------------------------------------------
    // Тема
    // ------------------------------------------------------------------

    /** Текущий режим темы. */
    public int getThemeMode() {
        if (preferences.contains(KEY_THEME_MODE)) {
            return preferences.getInt(KEY_THEME_MODE, THEME_SYSTEM);
        }
        // Миграция со старой версии: был только флажок «тёмная тема».
        if (preferences.contains(pref_name)) {
            int migrated = preferences.getBoolean(pref_name, false) ? THEME_DARK : THEME_SYSTEM;
            setThemeMode(migrated);
            return migrated;
        }
        return THEME_SYSTEM;
    }

    public void setThemeMode(int mode) {
        preferences.edit()
                .putInt(KEY_THEME_MODE, mode)
                .putBoolean(pref_name, mode == THEME_DARK)
                .apply();
    }

    // ------------------------------------------------------------------
    // Данные калькулятора
    // ------------------------------------------------------------------

    /** Последний ввод, который нужно восстановить после перезапуска. */
    public String getLastInput() {
        return preferences.getString(KEY_INPUT, "");
    }

    public void setLastInput(String input) {
        preferences.edit().putString(KEY_INPUT, input == null ? "" : input).apply();
    }

    /** Значение памяти калькулятора (M). */
    public String getMemory() {
        return preferences.getString(KEY_MEMORY, "0");
    }

    public void setMemory(String value) {
        preferences.edit().putString(KEY_MEMORY, value == null ? "0" : value).apply();
    }

    public boolean isKeySoundEnabled() {
        return preferences.getBoolean(KEY_SOUND, true);
    }

    public void setKeySoundEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_SOUND, enabled).apply();
    }

    public boolean isVibrationEnabled() {
        return preferences.getBoolean(KEY_VIBRATION, true);
    }

    public void setVibrationEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_VIBRATION, enabled).apply();
    }
}
