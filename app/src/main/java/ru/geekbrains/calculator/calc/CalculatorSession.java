package ru.geekbrains.calculator.calc;

import java.math.BigDecimal;

/**
 * Состояние и логика ввода калькулятора — без единого обращения к Android.
 * <p>
 * Благодаря этому вся «математика кнопок» (запятая, повторные операторы,
 * стирание, знак, проценты) проверяется обычными JVM-тестами, а Activity
 * занимается только отображением.
 */
public final class CalculatorSession {

    /** Максимум цифр в одном числе. */
    public static final int MAX_NUMBER_LENGTH = 15;

    private static final char PLUS = '+';
    private static final char MINUS = '-';
    private static final char MULTIPLY = ExpressionEvaluator.MULTIPLY_SIGN;
    private static final char DIVIDE = ExpressionEvaluator.DIVIDE_SIGN;
    private static final char PERCENT = '%';
    private static final char SQRT = ExpressionEvaluator.SQRT_SIGN;
    private static final char DOT = '.';
    private static final char OPEN = '(';
    private static final char CLOSE = ')';

    private final StringBuilder expression = new StringBuilder();
    private boolean evaluated;

    public CalculatorSession() {
        reset();
    }

    public void reset() {
        expression.setLength(0);
        evaluated = false;
    }

    /** Текст для экрана. */
    public String display() {
        return expression.length() == 0 ? "0" : expression.toString();
    }

    /** true, если на экране показан результат вычисления. */
    public boolean isEvaluated() {
        return evaluated;
    }

    public boolean isEmpty() {
        return expression.length() == 0;
    }

    // ------------------------------------------------------------------
    // Ввод
    // ------------------------------------------------------------------

    /** Ввод цифр (для кнопок «00» и т. п. поддержана строка длиной больше 1). */
    public void appendDigits(String digits) {
        if (digits == null || digits.isEmpty()) {
            return;
        }
        prepareForInput();
        for (int i = 0; i < digits.length(); i++) {
            appendDigit(digits.charAt(i));
        }
    }

    private void appendDigit(char digit) {
        if (!Character.isDigit(digit)) {
            return;
        }
        String current = currentNumber();
        if (current.length() >= MAX_NUMBER_LENGTH) {
            return;
        }
        // «0» не должен становиться ведущим: 0 → 5 даёт 5, а не 05
        if (current.equals("0")) {
            replaceLast(1, String.valueOf(digit));
        } else if (current.startsWith("0") && current.length() > 1 && current.charAt(1) != DOT) {
            replaceLast(current.length(), String.valueOf(digit));
        } else {
            expression.append(digit);
        }
        evaluated = false;
    }

    /** Десятичная запятая/точка. */
    public void appendDot() {
        prepareForInput();
        String current = currentNumber();
        if (current.indexOf(DOT) >= 0) {
            return;
        }
        if (current.isEmpty()) {
            expression.append('0');
        }
        if (current.length() >= MAX_NUMBER_LENGTH) {
            return;
        }
        expression.append(DOT);
        evaluated = false;
    }

    /** Оператор: + — × ÷. */
    public void appendOperator(char op) {
        if (op == 'x' || op == 'X' || op == '*' || op == MULTIPLY) {
            op = MULTIPLY;
        } else if (op == '/' || op == DIVIDE) {
            op = DIVIDE;
        } else if (op != PLUS && op != MINUS && op != MULTIPLY && op != DIVIDE) {
            return;
        }

        if (evaluated) {
            // продолжаем вычисления от результата
            evaluated = false;
        }
        if (expression.length() == 0) {
            // в начале допустим только минус (отрицательное число)
            if (op == MINUS) {
                expression.append(MINUS);
            }
            return;
        }

        char last = expression.charAt(expression.length() - 1);
        if (last == PLUS || last == MINUS || last == MULTIPLY || last == DIVIDE) {
            if (last == MINUS && isSignMinus(expression, expression.length() - 1)) {
                // «-» — знак числа. Если перед ним уже есть оператор,
                // меняем именно его: 5×-  →  5+ (а не «5×+»).
                expression.setLength(expression.length() - 1);
                appendOperator(op);
                return;
            }
            // двойной ввод оператора — заменяем предыдущий
            expression.setCharAt(expression.length() - 1, op);
            return;
        }
        if (last == DOT) {
            expression.append('0');
        }
        if (last == OPEN) {
            expression.append(op == MINUS ? MINUS : '0').append(op == MINUS ? "" : op);
            return;
        }
        expression.append(op);
    }

    /** Процент (постфиксный оператор). */
    public void appendPercent() {
        prepareForInput();
        if (expression.length() == 0) {
            return;
        }
        char last = expression.charAt(expression.length() - 1);
        if (Character.isDigit(last) || last == CLOSE) {
            expression.append(PERCENT);
        }
    }

    /** Корень: вставляет «√(» — скобка закрывается автоматически при «=». */
    public void appendSqrt() {
        prepareForInput();
        if (expression.length() > 0) {
            char last = expression.charAt(expression.length() - 1);
            if (Character.isDigit(last) || last == DOT || last == CLOSE || last == PERCENT) {
                // корень из уже введённого числа: √(5)
                // (неявное умножение не поддерживаем — нужен оператор)
                return;
            }
        }
        expression.append(SQRT).append(OPEN);
        evaluated = false;
    }

    /**
     * Кнопка «±»: меняет знак последнего числа (или последней скобочной группы).
     */
    public void toggleSign() {
        if (expression.length() == 0) {
            return;
        }
        if (evaluated) {
            evaluated = false;
        }
        int start = lastOperandStart(expression);
        if (start < 0 || start >= expression.length()) {
            return;
        }
        if (isMinus(expression, start)) {
            // знак уже есть — убираем его; двойной минус схлопываем
            expression.deleteCharAt(start);
            if (isMinus(expression, start)) {
                expression.deleteCharAt(start);
            }
        } else {
            // ставим унарный минус: «5×3» → «5×-3», «7» → «-7»
            expression.insert(start, MINUS);
        }
        evaluated = false;
    }

    /** Удаление последнего символа. */
    public void backspace() {
        if (evaluated) {
            reset();
            return;
        }
        if (expression.length() == 0) {
            return;
        }
        char last = expression.charAt(expression.length() - 1);
        expression.setLength(expression.length() - 1);
        if (last == ')' && expression.length() > 0 && expression.charAt(expression.length() - 1) == OPEN) {
            expression.setLength(expression.length() - 1);
            if (expression.length() > 0
                    && expression.charAt(expression.length() - 1) == SQRT) {
                expression.setLength(expression.length() - 1);
            }
        } else if (last == OPEN && expression.length() > 0
                && expression.charAt(expression.length() - 1) == SQRT) {
            expression.setLength(expression.length() - 1);
        }
    }

    /** Явная открывающая скобка. */
    public void appendOpenParen() {
        prepareForInput();
        if (expression.length() > 0) {
            char last = expression.charAt(expression.length() - 1);
            if (last == OPEN) {
                // «√((» и «((» — лишние скобки, игнорируем
                return;
            }
        } else {
            return;
        }
        expression.append(OPEN);
        evaluated = false;
    }

    /** Явная закрывающая скобка (только если есть что закрывать). */
    public void appendCloseParen() {
        prepareForInput();
        int balance = 0;
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == OPEN) {
                balance++;
            } else if (c == CLOSE) {
                balance--;
            }
        }
        if (balance <= 0 || expression.length() == 0) {
            return;
        }
        char last = expression.charAt(expression.length() - 1);
        if (Character.isDigit(last) || last == DOT || last == CLOSE || last == PERCENT) {
            expression.append(CLOSE);
            evaluated = false;
        }
    }

    /** Вставляет готовое число (например, значение из памяти). */
    public boolean insertNumber(String text) {
        BigDecimal value = NumberFormatter.parseOrNull(text);
        if (value == null) {
            return false;
        }
        prepareForInput();
        if (expression.length() > 0) {
            char last = expression.charAt(expression.length() - 1);
            if (Character.isDigit(last) || last == DOT || last == CLOSE || last == PERCENT) {
                // нельзя «приклеивать» число к числу
                return false;
            }
        }
        String plain = NumberFormatter.format(value);
        if (plain.length() > MAX_NUMBER_LENGTH + 1) {
            return false;
        }
        expression.append(plain);
        evaluated = false;
        return true;
    }

    /** Полная очистка вводимого числа (кнопка «C» в режиме ввода). */
    public void clearEntry() {
        if (evaluated) {
            reset();
            return;
        }
        int start = lastOperandStart(expression);
        if (start < 0) {
            reset();
            return;
        }
        expression.setLength(start);
    }

    /** Текущее выражение без изменения состояния. */
    public String getExpression() {
        return expression.toString();
    }

    /** Восстанавливает выражение (используется при пересоздании Activity). */
    public void setExpression(String value) {
        reset();
        if (value != null) {
            expression.append(value);
        }
    }

    /**
     * Значение текущего выражения, если его можно посчитать прямо сейчас.
     *
     * @return {@code null}, если выражение неполное или некорректное
     */
    public BigDecimal currentValue() {
        if (expression.length() == 0) {
            return null;
        }
        try {
            return ExpressionEvaluator.evaluate(withClosedBrackets(expression.toString()));
        } catch (CalcException e) {
            return null;
        }
    }

    /**
     * Подсказка «= результат» для живого предпросмотра.
     *
     * @return отформатированный результат или {@code null}, если показывать нечего
     */
    public String preview() {
        if (evaluated || !containsOperator()) {
            return null;
        }
        BigDecimal value = currentValue();
        if (value == null) {
            return null;
        }
        String formatted = NumberFormatter.format(value);
        // если ответ совпадает с введённым числом — подсказка бесполезна
        return formatted.equals(expression.toString()) ? null : formatted;
    }

    /** true, если в выражении есть хоть одна операция. */
    private boolean containsOperator() {
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (isOperator(c) || c == PERCENT || c == SQRT || c == OPEN) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------
    // Вычисление
    // ------------------------------------------------------------------

    /** Результат «равно». */
    public static final class Result {
        public final boolean ok;
        public final String text;
        public final CalcError error;

        private Result(boolean ok, String text, CalcError error) {
            this.ok = ok;
            this.text = text;
            this.error = error;
        }

        static Result success(String text) {
            return new Result(true, text, null);
        }

        static Result failure(CalcError error) {
            return new Result(false, null, error);
        }
    }

    /**
     * Вычисляет выражение. При успехе на экран выводится результат,
     * а выражение заменяется им — можно продолжать считать дальше.
     */
    public Result evaluate() {
        String prepared = withClosedBrackets(expression.toString());
        if (prepared.isEmpty()) {
            return Result.failure(CalcError.EMPTY);
        }
        try {
            BigDecimal value = ExpressionEvaluator.evaluate(prepared);
            String text = NumberFormatter.format(value);
            expression.setLength(0);
            expression.append(text);
            evaluated = true;
            return Result.success(text);
        } catch (CalcException e) {
            return Result.failure(e.getError());
        }
    }

    /** Дописывает забытые закрывающие скобки: {@code √(9+16} → {@code √(9+16)}. */
    static String withClosedBrackets(String src) {
        int balance = 0;
        for (int i = 0; i < src.length(); i++) {
            char c = src.charAt(i);
            if (c == OPEN) {
                balance++;
            } else if (c == CLOSE) {
                balance--;
            }
        }
        StringBuilder sb = new StringBuilder(src);
        for (int i = 0; i < balance; i++) {
            sb.append(CLOSE);
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------
    // Вспомогательное
    // ------------------------------------------------------------------

    /** После результата любая цифра/точка начинает новое выражение. */
    private void prepareForInput() {
        if (evaluated) {
            reset();
        }
    }

    private static boolean isOperator(char c) {
        return c == PLUS || c == MINUS || c == MULTIPLY || c == DIVIDE;
    }

    private static boolean isMinus(CharSequence src, int index) {
        return index >= 0 && index < src.length() && src.charAt(index) == MINUS;
    }

    /** true, если символ по индексу — унарный минус (знак числа), а не вычитание. */
    private static boolean isSignMinus(CharSequence src, int index) {
        if (index < 0 || index >= src.length() || src.charAt(index) != MINUS) {
            return false;
        }
        if (index == 0) {
            return true;
        }
        char prev = src.charAt(index - 1);
        return prev == OPEN || isOperator(prev);
    }

    /** Хвост цифр (с точкой) текущего вводимого числа. */
    private String currentNumber() {
        int i = expression.length();
        while (i > 0) {
            char c = expression.charAt(i - 1);
            if (Character.isDigit(c) || c == DOT) {
                i--;
            } else {
                break;
            }
        }
        return expression.substring(i);
    }

    private void replaceLast(int count, String replacement) {
        expression.setLength(expression.length() - count);
        expression.append(replacement);
    }

    /** Начало последнего операнда: числа или скобочной группы. */
    private static int lastOperandStart(CharSequence src) {
        int end = src.length() - 1;
        if (end < 0) {
            return -1;
        }
        if (src.charAt(end) == CLOSE) {
            int depth = 0;
            for (int i = end; i >= 0; i--) {
                char c = src.charAt(i);
                if (c == CLOSE) {
                    depth++;
                } else if (c == OPEN) {
                    depth--;
                    if (depth == 0) {
                        int start = i;
                        if (start > 0 && src.charAt(start - 1) == SQRT) {
                            start--;
                        }
                        return start;
                    }
                }
            }
            return -1;
        }
        int i = end;
        while (i >= 0) {
            char c = src.charAt(i);
            if (Character.isDigit(c) || c == DOT) {
                i--;
            } else {
                break;
            }
        }
        int start = i + 1;
        // унарный минус — часть числа: «-5» переключается в «5», а не в «--5»
        if (start > 0 && isSignMinus(src, start - 1)) {
            start--;
        }
        return start;
    }
}
