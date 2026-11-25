package ru.geekbrains.calculator.calc;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/** Проверки логики ввода: правила кнопок, стирание, знак, процент, память. */
public class CalculatorSessionTest {

    private static CalculatorSession type(String keys) {
        CalculatorSession session = new CalculatorSession();
        for (char key : keys.toCharArray()) {
            switch (key) {
                case '.':
                    session.appendDot();
                    break;
                case 's':
                    session.toggleSign();
                    break;
                case 'b':
                    session.backspace();
                    break;
                case 'r':
                    session.appendSqrt();
                    break;
                case '%':
                    session.appendPercent();
                    break;
                case 'c':
                    session.clearEntry();
                    break;
                case '(':
                    session.appendOpenParen();
                    break;
                case ')':
                    session.appendCloseParen();
                    break;
                case '=':
                    session.evaluate();
                    break;
                default:
                    if (Character.isDigit(key)) {
                        session.appendDigits(String.valueOf(key));
                    } else if ("+-*/×÷".indexOf(key) >= 0) {
                        session.appendOperator(key);
                    }
            }
        }
        return session;
    }

    private static String typed(String keys) {
        return type(keys).display();
    }

    private static String result(String keys) {
        CalculatorSession.Result r = type(keys).evaluate();
        assertTrue("Ожидался успех для " + keys + ", ошибка " + r.error, r.ok);
        return r.text;
    }

    @Test
    public void startsFromZero() {
        assertEquals("0", new CalculatorSession().display());
    }

    @Test
    public void leadingZerosAreNotDuplicated() {
        assertEquals("7", typed("007"));
        assertEquals("0.5", typed(".5"));
        assertEquals("5.2", typed("5..2"));
    }

    @Test
    public void repeatedOperatorReplacesPrevious() {
        assertEquals("5×", typed("5+*"));
        assertEquals("5-", typed("5+-"));
    }

    @Test
    public void minusAtStartMeansNegativeNumber() {
        assertEquals("-5", typed("-5"));
        assertEquals("5+-3", typed("5+3s"));
        assertEquals("5", typed("5ss"));
        assertEquals("5", typed("-5s"));
    }

    @Test
    public void backspaceRemovesWholeGroups() {
        assertEquals("5+", typed("5+3b"));
        assertEquals("0", typed("5b"));
        assertEquals("0", typed("rbb"));
        assertEquals("5", typed("5+b"));
    }

    @Test
    public void squareRootInput() {
        assertEquals("√(9+16", typed("r9+16"));
        assertEquals("5", result("r9+16="));
        assertEquals("5", result("r(9+16)="));
        assertEquals("3", result("r9="));
    }

    @Test
    public void evaluationShowsResult() {
        assertEquals("8", result("5+3="));
        assertEquals("0.3", result("0.1+0.2="));
        assertEquals("220", result("200+10%="));
    }

    @Test
    public void digitAfterEqualsStartsNewExpression() {
        assertEquals("7", typed("2+2=7"));
    }

    @Test
    public void operatorAfterEqualsContinuesWithResult() {
        assertEquals("7", typed("2+2=+3="));
    }

    @Test
    public void divisionByZeroReturnsError() {
        CalculatorSession.Result r = type("5/0").evaluate();
        assertFalse(r.ok);
        assertEquals(CalcError.DIVIDE_BY_ZERO, r.error);
        // состояние не портится — выражение остаётся на экране
        CalculatorSession session = type("5/0");
        session.evaluate();
        assertEquals("5÷0", session.display());
    }

    @Test
    public void previewShowsLiveResult() {
        CalculatorSession session = type("2+2×2");
        assertEquals("6", session.preview());
        assertNull(type("2+").preview());
        assertNull(type("7").preview());
    }

    @Test
    public void clearEntryKeepsWholeExpression() {
        assertEquals("5+", typed("5+3c"));
        assertEquals("5+3", typed("5+3c" + "3"));
        // после очистки второго числа выражение неполное
        CalculatorSession.Result r = type("5+3c=").evaluate();
        assertFalse(r.ok);
    }

    @Test
    public void insertNumberRespectsNeighbours() {
        CalculatorSession session = type("5+");
        assertTrue(session.insertNumber("10"));
        assertEquals("5+10", session.display());
        assertFalse(session.insertNumber("10"));
    }

    @Test
    public void closedBracketsAreAddedAutomatically() {
        CalculatorSession.Result r = type("(2+3×4").evaluate();
        assertTrue(r.ok);
        assertEquals("14", r.text);
    }

    @Test
    public void historyKeepsLatestFirstAndDeduplicates() {
        HistoryStore history = new HistoryStore(3);
        history.add("1+1", "2");
        history.add("2+2", "4");
        history.add("1+1", "2");

        assertEquals(2, history.size());
        assertEquals("1+1", history.getAll().get(0).expression);
        history.add("3+3", "6");
        history.add("4+4", "8");
        assertEquals(3, history.size());
        assertEquals("4+4", history.getAll().get(0).expression);
    }
}
