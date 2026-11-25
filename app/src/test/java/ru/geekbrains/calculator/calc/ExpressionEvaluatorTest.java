package ru.geekbrains.calculator.calc;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/** Проверки вычислителя: приоритеты, скобки, точность, ошибки. */
public class ExpressionEvaluatorTest {

    private static String eval(String expression) throws CalcException {
        return NumberFormatter.format(ExpressionEvaluator.evaluate(expression));
    }

    @Test
    public void simpleArithmetic() throws Exception {
        assertEquals("4", eval("2+2"));
        assertEquals("2", eval("5-3"));
        assertEquals("18", eval("6×3"));
        assertEquals("2.5", eval("10÷4"));
    }

    @Test
    public void operatorPrecedence() throws Exception {
        assertEquals("6", eval("2+2×2"));
        assertEquals("24", eval("12+3×4÷(2-1)"));
        assertEquals("14", eval("2×(3+4)"));
        assertEquals("20", eval("(2+3)×4"));
        assertEquals("21", eval("(1+2)×(3+4)"));
    }

    @Test
    public void decimalPrecisionHasNoFloatingPointNoise() throws Exception {
        assertEquals("0.3", eval("0.1+0.2"));
        assertEquals("4", eval("1,5+2,5"));
        assertEquals("0.333333333333", eval("1÷3"));
        assertEquals("100", eval("100÷3×3"));
    }

    @Test
    public void unaryMinusAndNegativeNumbers() throws Exception {
        assertEquals("-2", eval("-5+3"));
        assertEquals("5", eval("2--3"));
        assertEquals("-7", eval("−7"));
        assertEquals("-15", eval("3×-5"));
    }

    @Test
    public void squareRoot() throws Exception {
        assertEquals("3", eval("√9"));
        assertEquals("5", eval("√(9+16)"));
        assertEquals("2", eval("√4"));
    }

    @Test
    public void percentBehavesLikeOnRealCalculators() throws Exception {
        // 200 + 10% = 220 (процент от первого слагаемого)
        assertEquals("220", eval("200+10%"));
        assertEquals("180", eval("200-10%"));
        // как самостоятельное число процент — сотая доля
        assertEquals("0.5", eval("50%"));
        assertEquals("1", eval("2×50%"));
    }

    @Test
    public void errorsAreReported() {
        expectError("", CalcError.EMPTY);
        expectError("5+", CalcError.SYNTAX);
        expectError("2+3)", CalcError.SYNTAX);
        expectError("(2+3", CalcError.SYNTAX);
        expectError("2.3.4", CalcError.SYNTAX);
        expectError("5÷0", CalcError.DIVIDE_BY_ZERO);
        expectError("√-4", CalcError.INVALID_SQRT);
    }

    @Test
    public void overflowIsDetected() {
        StringBuilder big = new StringBuilder("9");
        for (int i = 0; i < 60; i++) {
            big.append('9');
        }
        expectError(big + "×" + big, CalcError.OVERFLOW);
    }

    @Test
    public void isValidMatchesEvaluate() {
        assertTrue(ExpressionEvaluator.isValid("2+2×2"));
        assertFalse(ExpressionEvaluator.isValid("2+"));
        assertFalse(ExpressionEvaluator.isValid(""));
    }

    private static void expectError(String expression, CalcError expected) {
        try {
            ExpressionEvaluator.evaluate(expression);
            fail("Ожидалась ошибка " + expected + " для " + expression);
        } catch (CalcException e) {
            assertEquals("Неверный код ошибки для " + expression, expected, e.getError());
        }
    }
}
