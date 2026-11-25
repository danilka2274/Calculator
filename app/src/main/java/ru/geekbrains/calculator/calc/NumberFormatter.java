package ru.geekbrains.calculator.calc;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Единая точка форматирования чисел для отображения.
 * <p>
 * Используется {@link BigDecimal}, поэтому нет «мусора» вроде {@code 0.30000000000000004}
 * и результат всегда выводится в обычной записи (без экспоненты).
 */
public final class NumberFormatter {

    /** Сколько значащих цифр показываем пользователю. */
    public static final int SIGNIFICANT_DIGITS = 12;

    /** Максимальная длина строки результата на экране. */
    private static final int MAX_DISPLAY_LENGTH = 18;

    private static final MathContext MC = new MathContext(SIGNIFICANT_DIGITS, RoundingMode.HALF_UP);

    private NumberFormatter() {
    }

    /**
     * Форматирует число: убирает хвостовые нули, нормализует {@code -0},
     * при необходимости округляет до {@link #SIGNIFICANT_DIGITS} значащих цифр.
     */
    public static String format(BigDecimal value) {
        if (value == null || value.signum() == 0) {
            return "0";
        }
        BigDecimal rounded = value.round(MC).stripTrailingZeros();
        if (rounded.scale() < 0) {
            rounded = rounded.setScale(0);
        }
        String plain = rounded.toPlainString();
        if (plain.length() > MAX_DISPLAY_LENGTH) {
            // Очень «длинное» число: жертвуем точностью ради читаемости.
            MathContext shorter = new MathContext(8, RoundingMode.HALF_UP);
            rounded = value.round(shorter).stripTrailingZeros();
            if (rounded.scale() < 0) {
                rounded = rounded.setScale(0);
            }
            plain = rounded.toPlainString();
            if (plain.length() > MAX_DISPLAY_LENGTH) {
                plain = plain.substring(0, MAX_DISPLAY_LENGTH);
            }
        }
        return plain;
    }

    /** Форматирование «сырого» числа, пришедшего из вычислителя. */
    public static String format(double value) {
        return format(BigDecimal.valueOf(value));
    }

    /**
     * Безопасно разбирает число из выражения.
     *
     * @return {@code null}, если строка не является числом
     */
    public static BigDecimal parseOrNull(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
