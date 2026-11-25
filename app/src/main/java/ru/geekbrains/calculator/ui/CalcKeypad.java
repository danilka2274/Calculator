package ru.geekbrains.calculator.ui;

import android.view.View;
import ru.geekbrains.calculator.R;
import ru.geekbrains.calculator.calc.CalculatorSession;

/**
 * Единая обработка нажатий всей клавиатуры: вместо двух десятков отдельных
 * слушателей в Activity достаточно вызвать {@link #onKey} (или повесить
 * {@link #createListener}).
 * <p>
 * Кнопки «=» и служебные (настройки, память) обрабатываются в Activity —
 * они меняют не только состояние сессии, но и UI.
 */
public final class CalcKeypad {

    /** Вызывается после того, как клавиша обработана. */
    public interface OnKeyListener {
        void onKeyHandled(CalculatorSession session);
    }

    private CalcKeypad() {
    }

    /**
     * Обрабатывает нажатие по id кнопки.
     *
     * @return {@code true}, если это была клавиша калькулятора
     */
    public static boolean onKey(CalculatorSession session, int viewId) {
        // if/else, а не switch: id ресурсов не обязаны быть константами времени компиляции
        if (viewId == R.id.button0) {
            session.appendDigits("0");
        } else if (viewId == R.id.button00) {
            session.appendDigits("00");
        } else if (viewId == R.id.button1) {
            session.appendDigits("1");
        } else if (viewId == R.id.button2) {
            session.appendDigits("2");
        } else if (viewId == R.id.button3) {
            session.appendDigits("3");
        } else if (viewId == R.id.button4) {
            session.appendDigits("4");
        } else if (viewId == R.id.button5) {
            session.appendDigits("5");
        } else if (viewId == R.id.button6) {
            session.appendDigits("6");
        } else if (viewId == R.id.button7) {
            session.appendDigits("7");
        } else if (viewId == R.id.button8) {
            session.appendDigits("8");
        } else if (viewId == R.id.button9) {
            session.appendDigits("9");
        } else if (viewId == R.id.buttonPt) {
            session.appendDot();
        } else if (viewId == R.id.buttonPlus) {
            session.appendOperator('+');
        } else if (viewId == R.id.buttonMinus) {
            session.appendOperator('-');
        } else if (viewId == R.id.buttonX) {
            session.appendOperator('*');
        } else if (viewId == R.id.buttonDiv) {
            session.appendOperator('/');
        } else if (viewId == R.id.buttonPers) {
            session.appendPercent();
        } else if (viewId == R.id.buttonSqrt) {
            session.appendSqrt();
        } else if (viewId == R.id.buttonSign) {
            session.toggleSign();
        } else if (viewId == R.id.buttonOpenParen) {
            session.appendOpenParen();
        } else if (viewId == R.id.buttonCloseParen) {
            session.appendCloseParen();
        } else if (viewId == R.id.buttonC) {
            session.clearEntry();
        } else if (viewId == R.id.buttonBack) {
            session.backspace();
        } else {
            return false;
        }
        return true;
    }

    /** Готовый слушатель для всех клавиш клавиатуры. */
    public static View.OnClickListener createListener(CalculatorSession session,
                                                      OnKeyListener listener) {
        return view -> {
            if (onKey(session, view.getId()) && listener != null) {
                listener.onKeyHandled(session);
            }
        };
    }
}
