package ru.geekbrains.calculator;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class Settings extends AppCompatActivity {

    // Имя настроек
    public static final String prefs = "prefs.xml";
    // Имя параметра в настройках
    public static final String pref_name = "theme";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialButton btnReturn = findViewById(R.id.buttonReturn);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent runMain = new Intent(Settings.this, MainActivity.class);
                startActivity(runMain);
            }
        });


        SwitchMaterial themeSwitch = findViewById(R.id.SwitchTheme);
        themeSwitch.setOnCheckedChangeListener(
                (CompoundButton buttonView, boolean isChecked) -> {
                    Intent switchTheme = new Intent(Settings.this, MainActivity.class);
                    SharedPreferences sharedPreferences = getSharedPreferences(prefs, MODE_PRIVATE);
                    if (sharedPreferences.getBoolean(pref_name, false) != isChecked) {
                        sharedPreferences.edit().
                                putBoolean(pref_name, isChecked).apply();
                        startActivity(switchTheme);
                    }
                });

    }
}