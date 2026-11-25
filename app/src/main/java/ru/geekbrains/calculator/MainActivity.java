package ru.geekbrains.calculator;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.math.BigDecimal;
import java.util.List;

import ru.geekbrains.calculator.calc.CalcError;
import ru.geekbrains.calculator.calc.CalculatorSession;
import ru.geekbrains.calculator.calc.HistoryEntry;
import ru.geekbrains.calculator.calc.HistoryStore;
import ru.geekbrains.calculator.calc.NumberFormatter;
import ru.geekbrains.calculator.ui.CalcKeypad;

/**
 * Экран калькулятора.
 * <p>
 * Здесь осталась только «клеевая» логика: привязка кнопок, отображение состояния
 * {@link CalculatorSession} и работа с памятью/историей. Вся арифметика и правила
 * ввода живут в пакете {@code calc} и покрыты unit-тестами.
 */
public class MainActivity extends AppCompatActivity {

    private static final String STATE_INPUT = "state_input";
    private static final String STATE_HISTORY_EXPR = "state_history_expr";
    private static final String STATE_HISTORY_RESULT = "state_history_result";

    private CalculatorSession session;
    private HistoryStore history;
    private CalcPreferences preferences;

    private TextView tvResult;
    private TextView tvPreview;
    private MaterialButton buttonMemory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = CalcPreferences.from(this);
        setContentView(R.layout.activity_main);

        session = new CalculatorSession();
        history = new HistoryStore();

        tvResult = findViewById(R.id.Result);
        tvPreview = findViewById(R.id.Preview);
        buttonMemory = findViewById(R.id.buttonMemory);

        findViewById(R.id.Settings).setOnClickListener(v -> openSettings());

        MaterialButton buttonEq = findViewById(R.id.buttonEq);
        buttonEq.setOnClickListener(v -> {
            playKeyFeedback();
            onEqualsClicked();
        });

        View.OnClickListener keyListener = CalcKeypad.createListener(session, s -> {
            playKeyFeedback();
            render();
        });
        int[] keyIds = {
                R.id.button0, R.id.button00, R.id.button1, R.id.button2, R.id.button3,
                R.id.button4, R.id.button5, R.id.button6, R.id.button7, R.id.button8,
                R.id.button9, R.id.buttonPt, R.id.buttonPlus, R.id.buttonMinus, R.id.buttonX,
                R.id.buttonDiv, R.id.buttonPers, R.id.buttonSqrt, R.id.buttonSign,
                R.id.buttonOpenParen, R.id.buttonCloseParen, R.id.buttonC, R.id.buttonBack
        };
        for (int id : keyIds) {
            findViewById(id).setOnClickListener(keyListener);
        }

        buttonMemory.setOnClickListener(v -> showMemoryDialog());
        buttonMemory.setOnLongClickListener(v -> {
            saveToMemory(session.display());
            return true;
        });
        findViewById(R.id.buttonHistory).setOnClickListener(v -> showHistoryDialog());

        // состояние из Bundle восстановит onRestoreInstanceState
        restoreState(null);
        updateMemoryButton();
        render();
    }

    // ------------------------------------------------------------------
    // Состояние
    // ------------------------------------------------------------------

    /** Звук нажатия (если включён в настройках). */
    private void playKeyFeedback() {
        if (preferences.isKeySoundEnabled()) {
            tvResult.playSoundEffect(android.view.SoundEffectConstants.CLICK);
        }
    }

    private void onEqualsClicked() {
        if (session.isEmpty()) {
            return;
        }
        String expression = session.getExpression();
        CalculatorSession.Result result = session.evaluate();
        if (result.ok) {
            history.add(expression, result.text);
            vibrate();
        } else {
            showError(result.error);
        }
        render();
    }

    private void render() {
        String display = session.display();
        if (!display.contentEquals(tvResult.getText())) {
            tvResult.setText(display);
        }
        tvResult.setContentDescription(getString(R.string.desc_result) + ": " + display);

        String preview = session.preview();
        if (preview == null) {
            tvPreview.setVisibility(View.GONE);
        } else {
            tvPreview.setText(getString(R.string.preview_format, preview));
            tvPreview.setVisibility(View.VISIBLE);
        }
        updateMemoryButton();
    }

    private void showError(CalcError error) {
        tvPreview.setVisibility(View.GONE);
        String message = errorMessage(error);
        Snackbar.make(tvResult, message, Snackbar.LENGTH_SHORT).show();
        vibrate();
    }

    private String errorMessage(CalcError error) {
        if (error == null) {
            return getString(R.string.error_unknown);
        }
        switch (error) {
            case EMPTY:
                return getString(R.string.error_empty);
            case DIVIDE_BY_ZERO:
                return getString(R.string.error_divide_by_zero);
            case INVALID_SQRT:
                return getString(R.string.error_invalid_sqrt);
            case OVERFLOW:
                return getString(R.string.error_overflow);
            case SYNTAX:
            default:
                return getString(R.string.error_syntax);
        }
    }

    private void vibrate() {
        if (!preferences.isVibrationEnabled()) {
            return;
        }
        if (tvResult.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)) {
            return;
        }
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(18);
            }
        }
    }

    // ------------------------------------------------------------------
    // Память
    // ------------------------------------------------------------------

    private String memoryValue() {
        return preferences.getMemory();
    }

    private void saveToMemory(String value) {
        String text = (value == null || value.isEmpty()) ? "0" : value;
        preferences.setMemory(text);
        updateMemoryButton();
        Snackbar.make(tvResult, R.string.memory_saved, Snackbar.LENGTH_SHORT).show();
    }

    private void updateMemoryButton() {
        if (buttonMemory == null) {
            return;
        }
        boolean hasMemory = !"0".equals(memoryValue());
        buttonMemory.setAlpha(hasMemory ? 1f : 0.55f);
    }

    private void showMemoryDialog() {
        String[] items = {
                getString(R.string.memory_recall),
                getString(R.string.memory_add),
                getString(R.string.memory_subtract),
                getString(R.string.memory_clear)
        };
        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.memory_title))
                .setMessage(getString(R.string.memory_value, memoryValue()))
                .setItems(items, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            recallMemory();
                            break;
                        case 1:
                            accumulateMemory(true);
                            break;
                        case 2:
                            accumulateMemory(false);
                            break;
                        default:
                            saveToMemory("0");
                            break;
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void recallMemory() {
        String value = memoryValue();
        if ("0".equals(value) && session.isEmpty()) {
            Snackbar.make(tvResult, R.string.memory_empty, Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (!session.insertNumber(value)) {
            Snackbar.make(tvResult, R.string.memory_empty, Snackbar.LENGTH_SHORT).show();
            return;
        }
        render();
    }

    private void accumulateMemory(boolean add) {
        BigDecimal current = session.currentValue();
        BigDecimal memory = NumberFormatter.parseOrNull(memoryValue());
        if (current == null) {
            Snackbar.make(tvResult, R.string.error_syntax, Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (memory == null) {
            memory = BigDecimal.ZERO;
        }
        BigDecimal updated = add ? memory.add(current) : memory.subtract(current);
        saveToMemory(NumberFormatter.format(updated));
    }

    // ------------------------------------------------------------------
    // История
    // ------------------------------------------------------------------

    private void showHistoryDialog() {
        if (history.isEmpty()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.history_title))
                    .setMessage(getString(R.string.history_empty))
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return;
        }

        List<HistoryEntry> entries = history.getAll();
        final String[] lines = new String[entries.size()];
        for (int i = 0; i < entries.size(); i++) {
            lines[i] = entries.get(i).toString();
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.history_title))
                .setItems(lines, (dialog, which) -> insertHistoryResult(entries.get(which)))
                .setNeutralButton(R.string.history_clear, (dialog, which) -> history.clear())
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void insertHistoryResult(HistoryEntry entry) {
        if (session.insertNumber(entry.result)) {
            render();
        }
    }

    // ------------------------------------------------------------------
    // Настройки / жизненный цикл
    // ------------------------------------------------------------------

    private void openSettings() {
        startActivity(new Intent(this, Settings.class));
    }

    @Override
    protected void onPause() {
        super.onPause();
        preferences.setLastInput(session.getExpression());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_INPUT, session.getExpression());

        StringBuilder expressions = new StringBuilder();
        StringBuilder results = new StringBuilder();
        for (HistoryEntry entry : history.getAll()) {
            expressions.append(entry.expression).append('\n');
            results.append(entry.result).append('\n');
        }
        outState.putString(STATE_HISTORY_EXPR, expressions.toString());
        outState.putString(STATE_HISTORY_RESULT, results.toString());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle state) {
        super.onRestoreInstanceState(state);
        restoreState(state);
        render();
    }

    private void restoreState(Bundle state) {
        String input = null;
        if (state != null) {
            input = state.getString(STATE_INPUT);
            restoreHistory(state);
        }
        if (input == null || input.isEmpty()) {
            input = preferences.getLastInput();
        }
        if (input != null && !input.isEmpty()) {
            session.setExpression(input);
        }
    }

    private void restoreHistory(Bundle state) {
        String expressions = state.getString(STATE_HISTORY_EXPR, "");
        String results = state.getString(STATE_HISTORY_RESULT, "");
        if (expressions.isEmpty() || results.isEmpty()) {
            return;
        }
        String[] exprLines = expressions.split("\n", -1);
        String[] resultLines = results.split("\n", -1);
        int count = Math.min(exprLines.length, resultLines.length);
        // восстанавливаем с конца, чтобы порядок остался тем же
        for (int i = count - 1; i >= 0; i--) {
            if (!exprLines[i].isEmpty() && !resultLines[i].isEmpty()) {
                history.add(exprLines[i], resultLines[i]);
            }
        }
    }
}
