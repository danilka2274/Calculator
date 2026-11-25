package ru.geekbrains.calculator;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * Экран настроек: тема (системная / светлая / тёмная), звук клавиш, вибрация.
 * Изменения применяются сразу и сохраняются в {@link CalcPreferences}.
 */
public class Settings extends AppCompatActivity {

    /** Имя настроек (оставлено для совместимости со старыми версиями). */
    public static final String prefs = "prefs.xml";
    /** Имя параметра темы (оставлено для совместимости со старыми версиями). */
    public static final String pref_name = CalcPreferences.pref_name;

    private CalcPreferences preferences;
    private MaterialButtonToggleGroup themeGroup;
    private boolean binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = CalcPreferences.from(this);
        setContentView(R.layout.activity_settings);

        binding = true;

        themeGroup = findViewById(R.id.themeGroup);
        updateThemeSelection(preferences.getThemeMode());
        themeGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (binding || !isChecked) {
                return;
            }
            preferences.setThemeMode(themeModeOf(checkedId));
        });

        SwitchMaterial soundSwitch = findViewById(R.id.SwitchSound);
        soundSwitch.setChecked(preferences.isKeySoundEnabled());
        soundSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                preferences.setKeySoundEnabled(isChecked));

        SwitchMaterial vibrationSwitch = findViewById(R.id.SwitchVibration);
        vibrationSwitch.setChecked(preferences.isVibrationEnabled());
        vibrationSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                preferences.setVibrationEnabled(isChecked));

        TextView version = findViewById(R.id.version);
        version.setText(getString(R.string.settings_about, BuildConfig.VERSION_NAME));

        MaterialButton btnReturn = findViewById(R.id.buttonReturn);
        btnReturn.setOnClickListener(v -> finish());

        binding = false;
    }

    private void updateThemeSelection(int mode) {
        int id;
        if (mode == CalcPreferences.THEME_LIGHT) {
            id = R.id.themeLight;
        } else if (mode == CalcPreferences.THEME_DARK) {
            id = R.id.themeDark;
            // id выше объявлен в разметке; здесь только выбор нужной кнопки
        } else {
            id = R.id.themeSystem;
        }
        themeGroup.check(id);
    }

    private int themeModeOf(int buttonId) {
        if (buttonId == R.id.themeLight) {
            return CalcPreferences.THEME_LIGHT;
        }
        if (buttonId == R.id.themeDark) {
            return CalcPreferences.THEME_DARK;
        }
        return CalcPreferences.THEME_SYSTEM;
    }
}
