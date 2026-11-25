package ru.geekbrains.calculator.calc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * История вычислений в памяти процесса: новые записи сверху, ограниченный размер,
 * повторы схлопываются. Без Android-зависимостей — легко тестируется.
 */
public final class HistoryStore {

    /** По умолчанию храним последние 50 вычислений. */
    public static final int DEFAULT_LIMIT = 50;

    private final int limit;
    private final List<HistoryEntry> entries = new ArrayList<>();

    public HistoryStore() {
        this(DEFAULT_LIMIT);
    }

    public HistoryStore(int limit) {
        this.limit = Math.max(1, limit);
    }

    public void add(String expression, String result) {
        if (expression == null || result == null) {
            return;
        }
        HistoryEntry entry = new HistoryEntry(expression, result);
        entries.remove(entry);
        entries.add(0, entry);
        while (entries.size() > limit) {
            entries.remove(entries.size() - 1);
        }
    }

    public List<HistoryEntry> getAll() {
        return Collections.unmodifiableList(entries);
    }

    public int size() {
        return entries.size();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public void clear() {
        entries.clear();
    }
}
