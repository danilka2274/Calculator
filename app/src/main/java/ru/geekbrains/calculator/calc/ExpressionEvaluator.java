package ru.geekbrains.calculator.calc;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Вычислитель арифметических выражений.
 * <p>
 * Работает с выражениями в «экранной» записи: {@code 12+3×4÷(2-1)}, поддерживает
 * унарный минус, скобки, квадратный корень и проценты.
 * <p>
 * Разбор построен на алгоритме сортировочной станции (shunting-yard) с переводом
 * в обратную польскую запись — это даёт корректный приоритет операций за один
 * проход и без рекурсии.
 * <p>
 * Все вычисления выполняются в {@link BigDecimal}: нет накопления ошибки
 * {@code double} (никаких {@code 0.30000000000000004}).
 * <p>
 * Класс не зависит от Android — покрывается обычными unit-тестами.
 */
public final class ExpressionEvaluator {

    /** Точность промежуточных вычислений (значащих цифр). */
    private static final MathContext MC = new MathContext(24, RoundingMode.HALF_UP);

    /** Предел модуля числа: всё, что больше, считаем переполнением. */
    private static final BigDecimal MAX_ABS = new BigDecimal("1E100");

    /** Знак корня в экранной записи. */
    public static final char SQRT_SIGN = '√';
    /** Знаки операций в экранной записи. */
    public static final char MULTIPLY_SIGN = '×';
    public static final char DIVIDE_SIGN = '÷';
    public static final char MINUS_SIGN = '−';

    private ExpressionEvaluator() {
    }

    /**
     * Вычисляет выражение.
     *
     * @param expression выражение в экранной записи, например {@code "2+2×2"}
     * @return точный результат
     * @throws CalcException если выражение некорректно или вычисление невозможно
     */
    public static BigDecimal evaluate(String expression) throws CalcException {
        if (expression == null || expression.trim().isEmpty()) {
            throw new CalcException(CalcError.EMPTY);
        }
        List<Token> tokens = tokenize(normalize(expression));
        List<Token> rpn = toRpn(tokens);
        return evaluateRpn(rpn);
    }

    /** Проверяет, можно ли вычислить выражение (без исключений). */
    public static boolean isValid(String expression) {
        try {
            evaluate(expression);
            return true;
        } catch (CalcException e) {
            return false;
        }
    }

    /**
     * Приводит экранную запись к внутренней: {@code ×} → {@code *}, {@code ÷} → {@code /},
     * {@code −} → {@code -}, {@code ,} → {@code .}, {@code √} → {@code r}.
     */
    static String normalize(String expression) {
        StringBuilder sb = new StringBuilder(expression.length());
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            switch (c) {
                case MULTIPLY_SIGN:
                case 'x':
                case 'X':
                    sb.append('*');
                    break;
                case DIVIDE_SIGN:
                    sb.append('/');
                    break;
                case MINUS_SIGN:
                    sb.append('-');
                    break;
                case ',':
                    sb.append('.');
                    break;
                case SQRT_SIGN:
                    sb.append('r');
                    break;
                case ' ':
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    // ---------------------------------------------------------------------
    // Токенизация
    // ---------------------------------------------------------------------

    private enum Type {NUMBER, PLUS, MINUS, STAR, SLASH, LPAREN, RPAREN, SQRT, PERCENT, NEG}

    private static final class Token {
        final Type type;
        final String text;

        Token(Type type, String text) {
            this.type = type;
            this.text = text;
        }

        @Override
        public String toString() {
            return type + (text == null ? "" : "(" + text + ")");
        }
    }

    private static List<Token> tokenize(String src) throws CalcException {
        List<Token> tokens = new ArrayList<>(src.length());
        int i = 0;
        while (i < src.length()) {
            char c = src.charAt(i);
            if (Character.isDigit(c) || c == '.') {
                int start = i;
                boolean dotSeen = false;
                while (i < src.length() && (Character.isDigit(src.charAt(i)) || src.charAt(i) == '.')) {
                    if (src.charAt(i) == '.') {
                        if (dotSeen) {
                            throw new CalcException(CalcError.SYNTAX);
                        }
                        dotSeen = true;
                    }
                    i++;
                }
                String number = src.substring(start, i);
                if (number.equals(".")) {
                    throw new CalcException(CalcError.SYNTAX);
                }
                tokens.add(new Token(Type.NUMBER, number));
                continue;
            }
            switch (c) {
                case '+':
                    tokens.add(new Token(Type.PLUS, null));
                    break;
                case '-':
                    tokens.add(new Token(Type.MINUS, null));
                    break;
                case '*':
                    tokens.add(new Token(Type.STAR, null));
                    break;
                case '/':
                    tokens.add(new Token(Type.SLASH, null));
                    break;
                case '(':
                    tokens.add(new Token(Type.LPAREN, null));
                    break;
                case ')':
                    tokens.add(new Token(Type.RPAREN, null));
                    break;
                case '%':
                    tokens.add(new Token(Type.PERCENT, null));
                    break;
                case 'r':
                    tokens.add(new Token(Type.SQRT, null));
                    break;
                default:
                    throw new CalcException(CalcError.SYNTAX);
            }
            i++;
        }
        return tokens;
    }

    // ---------------------------------------------------------------------
    // Сортировочная станция
    // ---------------------------------------------------------------------

    private static List<Token> toRpn(List<Token> tokens) throws CalcException {
        List<Token> output = new ArrayList<>(tokens.size());
        Deque<Token> stack = new ArrayDeque<>();
        boolean expectOperand = true;

        for (Token token : tokens) {
            switch (token.type) {
                case NUMBER:
                    if (!expectOperand) {
                        throw new CalcException(CalcError.SYNTAX);
                    }
                    output.add(token);
                    expectOperand = false;
                    break;

                case SQRT:
                    if (!expectOperand) {
                        throw new CalcException(CalcError.SYNTAX);
                    }
                    stack.push(token);
                    // после корня обязательна скобка
                    expectOperand = true;
                    break;

                case LPAREN:
                    if (!expectOperand) {
                        throw new CalcException(CalcError.SYNTAX);
                    }
                    stack.push(token);
                    break;

                case RPAREN:
                    if (expectOperand) {
                        throw new CalcException(CalcError.SYNTAX);
                    }
                    boolean found = false;
                    while (!stack.isEmpty()) {
                        Token top = stack.pop();
                        if (top.type == Type.LPAREN) {
                            found = true;
                            break;
                        }
                        output.add(top);
                    }
                    if (!found) {
                        throw new CalcException(CalcError.SYNTAX);
                    }
                    if (!stack.isEmpty() && stack.peek().type == Type.SQRT) {
                        output.add(stack.pop());
                    }
                    expectOperand = false;
                    break;

                case PERCENT:
                    if (expectOperand) {
                        throw new CalcException(CalcError.SYNTAX);
                    }
                    output.add(token);
                    break;

                default: // операторы
                    if (expectOperand) {
                        if (token.type == Type.MINUS) {
                            stack.push(new Token(Type.NEG, null));
                            break;
                        }
                        throw new CalcException(CalcError.SYNTAX);
                    }
                    while (!stack.isEmpty() && stack.peek().type != Type.LPAREN
                            && shouldPop(stack.peek(), token)) {
                        output.add(stack.pop());
                    }
                    stack.push(token);
                    expectOperand = true;
                    break;
            }
        }

        if (expectOperand) {
            // выражение заканчивается оператором или скобкой
            throw new CalcException(CalcError.SYNTAX);
        }
        while (!stack.isEmpty()) {
            Token top = stack.pop();
            if (top.type == Type.LPAREN) {
                throw new CalcException(CalcError.SYNTAX);
            }
            output.add(top);
        }
        return output;
    }

    /** Правило «выталкивания» из стека: приоритет и ассоциативность. */
    private static boolean shouldPop(Token top, Token current) {
        int pTop = precedence(top);
        int pCur = precedence(current);
        if (pTop > pCur) {
            return true;
        }
        // унарный минус правоассоциативен, бинарные операторы — левоассоциативны
        return pTop == pCur && current.type != Type.NEG;
    }

    private static int precedence(Token token) {
        switch (token.type) {
            case SQRT:
                return 4;
            case NEG:
                return 3;
            case STAR:
            case SLASH:
                return 2;
            case PLUS:
            case MINUS:
                return 1;
            default:
                return 0;
        }
    }

    // ---------------------------------------------------------------------
    // Вычисление обратной польской записи
    // ---------------------------------------------------------------------

    private static BigDecimal evaluateRpn(List<Token> rpn) throws CalcException {
        Deque<BigDecimal> values = new ArrayDeque<>();
        // Флаг «число получено через процент» нужен для записи вида 200+10% = 220.
        Deque<Boolean> fromPercent = new ArrayDeque<>();

        for (Token token : rpn) {
            switch (token.type) {
                case NUMBER: {
                    BigDecimal value = NumberFormatter.parseOrNull(token.text);
                    if (value == null) {
                        throw new CalcException(CalcError.SYNTAX);
                    }
                    values.push(value);
                    fromPercent.push(Boolean.FALSE);
                    break;
                }
                case PERCENT: {
                    BigDecimal value = pop(values);
                    popFlag(fromPercent);
                    values.push(value.movePointLeft(2));
                    fromPercent.push(Boolean.TRUE);
                    break;
                }
                case NEG: {
                    BigDecimal value = pop(values);
                    popFlag(fromPercent);
                    values.push(value.negate());
                    fromPercent.push(Boolean.FALSE);
                    break;
                }
                case SQRT: {
                    BigDecimal value = pop(values);
                    popFlag(fromPercent);
                    if (value.signum() < 0) {
                        throw new CalcException(CalcError.INVALID_SQRT);
                    }
                    values.push(sqrt(value));
                    fromPercent.push(Boolean.FALSE);
                    break;
                }
                default: {
                    BigDecimal right = pop(values);
                    boolean rightIsPercent = popFlag(fromPercent);
                    BigDecimal left = pop(values);
                    popFlag(fromPercent);
                    BigDecimal result;
                    switch (token.type) {
                        case PLUS:
                            result = rightIsPercent
                                    ? left.add(left.multiply(right, MC), MC)
                                    : left.add(right, MC);
                            break;
                        case MINUS:
                            result = rightIsPercent
                                    ? left.subtract(left.multiply(right, MC), MC)
                                    : left.subtract(right, MC);
                            break;
                        case STAR:
                            result = left.multiply(right, MC);
                            break;
                        case SLASH:
                            if (right.signum() == 0) {
                                throw new CalcException(CalcError.DIVIDE_BY_ZERO);
                            }
                            result = left.divide(right, MC);
                            break;
                        default:
                            throw new CalcException(CalcError.SYNTAX);
                    }
                    values.push(checkRange(result));
                    fromPercent.push(Boolean.FALSE);
                    break;
                }
            }
        }

        if (values.size() != 1) {
            throw new CalcException(CalcError.SYNTAX);
        }
        return checkRange(values.pop());
    }

    private static BigDecimal pop(Deque<BigDecimal> stack) throws CalcException {
        if (stack.isEmpty()) {
            throw new CalcException(CalcError.SYNTAX);
        }
        return stack.pop();
    }

    private static boolean popFlag(Deque<Boolean> stack) {
        return Boolean.TRUE.equals(stack.isEmpty() ? null : stack.pop());
    }

    private static BigDecimal checkRange(BigDecimal value) throws CalcException {
        if (value.abs().compareTo(MAX_ABS) > 0) {
            throw new CalcException(CalcError.OVERFLOW);
        }
        return value;
    }

    /** Квадратный корень методом Ньютона с заданной точностью. */
    static BigDecimal sqrt(BigDecimal value) {
        if (value.signum() == 0) {
            return BigDecimal.ZERO;
        }
        if (value.compareTo(BigDecimal.ONE) == 0) {
            return BigDecimal.ONE;
        }
        final MathContext mc = new MathContext(MC.getPrecision() + 4, RoundingMode.HALF_UP);
        BigDecimal guess = BigDecimal.valueOf(Math.sqrt(value.doubleValue()));
        if (guess.signum() == 0) {
            guess = BigDecimal.ONE;
        }
        for (int i = 0; i < 40; i++) {
            BigDecimal next = guess.add(value.divide(guess, mc), mc).divide(BigDecimal.valueOf(2), mc);
            if (next.compareTo(guess) == 0) {
                break;
            }
            guess = next;
        }
        return guess.round(MC);
    }
}
