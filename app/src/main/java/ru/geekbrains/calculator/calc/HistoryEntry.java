package ru.geekbrains.calculator.calc;

/** Одна запись истории: исходное выражение и результат. */
public final class HistoryEntry {

    public final String expression;
    public final String result;

    public HistoryEntry(String expression, String result) {
        this.expression = expression;
        this.result = result;
    }

    @Override
    public String toString() {
        return expression + " = " + result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HistoryEntry)) {
            return false;
        }
        HistoryEntry other = (HistoryEntry) o;
        return expression.equals(other.expression) && result.equals(other.result);
    }

    @Override
    public int hashCode() {
        return 31 * expression.hashCode() + result.hashCode();
    }
}
