package ru.geekbrains.calculator.calc;

/** Исключение вычислителя: содержит только код ошибки, без текста. */
public class CalcException extends Exception {

    private static final long serialVersionUID = 1L;

    private final CalcError error;

    public CalcException(CalcError error) {
        super(error.name());
        this.error = error;
    }

    public CalcError getError() {
        return error;
    }
}
