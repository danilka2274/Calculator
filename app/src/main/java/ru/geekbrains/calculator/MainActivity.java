package ru.geekbrains.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.switchmaterial.SwitchMaterial;

//1.	Переделайте все кнопки на материал.
//2.	Все размеры и строки сделайте ресурсами.
//3.	Создайте стиль для своего приложения.
//4.	* Создайте светлую и тёмную тему для приложения.

public class MainActivity extends AppCompatActivity {

    // Имя настроек
    private static final String prefs = "prefs.xml";

    // Имя параметра в настройках
    private static final String pref_name = "theme";

    public static final String NAME_ACTIVITY = "MainActivity";

    public static final String KEY_FIRST = NAME_ACTIVITY + ".mFirst";
    public static final String KEY_SECOND = NAME_ACTIVITY + ".mSecond";
    public static final String KEY_RESULT = NAME_ACTIVITY + ".mResult";
    public static final String KEY_OPERATION = NAME_ACTIVITY + ".mOeration";
    public static final String KEY_TV_RESULT = NAME_ACTIVITY + ".tvResult";

    private String mFirst = "";
    private String mSecond = "";
    private String mtvResultText = "0";
    private float mResult = 0;
    private char mOperation = ' ';
    TextView tvResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isDarkTheme = getSharedPreferences(prefs, MODE_PRIVATE).
                getBoolean(pref_name, false);
        if (isDarkTheme) {
            setTheme(R.style.Theme_CalculatorDark);
        } else {
            setTheme(R.style.Theme_Calculator);
        }

        setContentView(R.layout.activity_main);

        SwitchMaterial themeSwitch = findViewById(R.id.SwitchTheme);
        themeSwitch.setOnCheckedChangeListener(
                (CompoundButton buttonView, boolean isChecked) -> {
                    SharedPreferences sharedPreferences = getSharedPreferences(prefs, MODE_PRIVATE);
                    if (sharedPreferences.getBoolean(pref_name, false) != isChecked) {
                        sharedPreferences.edit().
                                putBoolean(pref_name, isChecked).apply();
                        recreate(); // заново попадём в onCreate
                    }
                });


        Button mButton0 = findViewById(R.id.button0);
        Button mButton00 = findViewById(R.id.button00);
        Button mButton1 = findViewById(R.id.button1);
        Button mButton2 = findViewById(R.id.button2);
        Button mButton3 = findViewById(R.id.button3);
        Button mButton4 = findViewById(R.id.button4);
        Button mButton5 = findViewById(R.id.button5);
        Button mButton6 = findViewById(R.id.button6);
        Button mButton7 = findViewById(R.id.button7);
        Button mButton8 = findViewById(R.id.button8);
        Button mButton9 = findViewById(R.id.button9);
        Button mButtonX = findViewById(R.id.buttonX);
        Button mButtonC = findViewById(R.id.buttonC);
        Button mButtonBack = findViewById(R.id.buttonBack);
        Button mButtonDiv = findViewById(R.id.buttonDiv);
        Button mButtonPt = findViewById(R.id.buttonPt);
        Button mButtonEq = findViewById(R.id.buttonEq);
        Button mButtonPlus = findViewById(R.id.buttonPlus);
        Button mButtonMinus = findViewById(R.id.buttonMinus);
        Button mButtonPers = findViewById(R.id.buttonPers);
        tvResult = findViewById(R.id.Result);
        setTvResult("0");

        mButton0.setOnClickListener(keyNumericClickListener);
        mButton00.setOnClickListener(keyNumericClickListener);
        mButton1.setOnClickListener(keyNumericClickListener);
        mButton2.setOnClickListener(keyNumericClickListener);
        mButton3.setOnClickListener(keyNumericClickListener);
        mButton4.setOnClickListener(keyNumericClickListener);
        mButton5.setOnClickListener(keyNumericClickListener);
        mButton6.setOnClickListener(keyNumericClickListener);
        mButton7.setOnClickListener(keyNumericClickListener);
        mButton8.setOnClickListener(keyNumericClickListener);
        mButton9.setOnClickListener(keyNumericClickListener);
        mButtonX.setOnClickListener(buttonXClickListener);
        mButtonC.setOnClickListener(buttonCClickListener);
        mButtonBack.setOnClickListener(ButtonBackClickListener);
        mButtonDiv.setOnClickListener(ButtonDivClickListener);
        mButtonPt.setOnClickListener(ButtonPtClickListener);
        mButtonEq.setOnClickListener(ButtonEqClickListener);
        mButtonPlus.setOnClickListener(ButtonPlusClickListener);
        mButtonMinus.setOnClickListener(ButtonMinusClickListener);
        mButtonPers.setOnClickListener(ButtonPersClickListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(NAME_ACTIVITY, "onStart() ");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(NAME_ACTIVITY, "onRestart() ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(NAME_ACTIVITY, "onResume() ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(NAME_ACTIVITY, "onPause() ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(NAME_ACTIVITY, "onStop() ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(NAME_ACTIVITY, "onDestroy() ");
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        Log.e(NAME_ACTIVITY, "onSaveInstanceState() ");
        super.onSaveInstanceState(state);
        state.putChar(KEY_OPERATION, mOperation);
        state.putFloat(KEY_RESULT, mResult);
        state.putString(KEY_SECOND, mSecond);
        state.putString(KEY_FIRST, mFirst);
        state.putString(KEY_TV_RESULT, mtvResultText);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        Log.e(NAME_ACTIVITY, "onRestoreInstanceState() ");
        super.onRestoreInstanceState(state);
        mOperation = state.getChar(KEY_OPERATION);
        mResult = state.getFloat(KEY_RESULT);
        mSecond = state.getString(KEY_SECOND);
        mFirst = state.getString(KEY_FIRST);
        setTvResult(state.getString(KEY_TV_RESULT));
    }


    private final View.OnClickListener keyNumericClickListener = (view) -> {
        int viewId = view.getId();
        if (viewId == R.id.button0) {
            addCharToParam("0");
        } else if (viewId == R.id.button00) {
            addCharToParam("00");
        } else if (viewId == R.id.button1) {
            addCharToParam("1");
        } else if (viewId == R.id.button2) {
            addCharToParam("2");
        } else if (viewId == R.id.button3) {
            addCharToParam("3");
        } else if (viewId == R.id.button4) {
            addCharToParam("4");
        } else if (viewId == R.id.button5) {
            addCharToParam("5");
        } else if (viewId == R.id.button6) {
            addCharToParam("6");
        } else if (viewId == R.id.button7) {
            addCharToParam("7");
        } else if (viewId == R.id.button8) {
            addCharToParam("8");
        } else if (viewId == R.id.button9) {
            addCharToParam("9");
        }
    };


    private final View.OnClickListener buttonXClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mOperation == '!' || mOperation != ' ' || mFirst.equals("")) return;
            mOperation = 'X';
            addCharToText("x");
        }
    };

    private final View.OnClickListener buttonCClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            initCalc();
        }
    };

    private final View.OnClickListener ButtonBackClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mOperation == '!') return;
            if (mtvResultText.contains("=")) return;
            if (mtvResultText.length() == 1) {
                mFirst = "";
                setTvResult("0");
            } else if (
                    mtvResultText.charAt(mtvResultText.length() - 1) != '+' && mtvResultText.charAt(mtvResultText.length() - 1) != '*' &&
                            mtvResultText.charAt(mtvResultText.length() - 1) != '/' && mtvResultText.charAt(mtvResultText.length() - 1) != '-'
            ) {
                if (mOperation == ' ') {
                    if (!mFirst.equals("")) {
                        mFirst = mFirst.substring(0, mFirst.length() - 1);
                    }
                } else {
                    if (!mSecond.equals("")) {
                        mSecond = mSecond.substring(0, mSecond.length() - 1);
                    }
                }
                setTvResult(mtvResultText.substring(0, mtvResultText.length() - 1));
            } else {
                mOperation = ' ';
                mSecond = "";
                setTvResult(mtvResultText.substring(0, mtvResultText.length() - 1));
            }
        }
    };

    private final View.OnClickListener ButtonDivClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mOperation == '!' || mOperation != ' ' || mFirst.equals("")) return;
            mOperation = '/';
            addCharToText("/");
        }
    };

    private final View.OnClickListener ButtonPtClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            addCharToParam(".");
        }
    };

    private final View.OnClickListener ButtonEqClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mOperation == '!') return;

            String resultText = "";
            try {
                if (!mFirst.equals("") && !mSecond.equals("")) {
                    float first = Float.parseFloat(mFirst);
                    float second = Float.parseFloat(mSecond);

                    if (mOperation != ' ' && mtvResultText.contains("=")) {
                        first = mResult;
                        mFirst = "" + mResult;
                        setTvResult("" + splitZero("" + mResult) + mOperation + mSecond);
                    }
                    addCharToText(" = ");

                    switch (mOperation) {
                        case '+':
                            mResult = first + second;
                            break;
                        case '-':
                            mResult = first - second;
                            break;
                        case 'X':
                            mResult = first * second;
                            break;
                        case '%':
                            mResult = (first * second) / 100;
                            break;
                        case '/':
                            if (mSecond.equals("0")) {
                                throw new ArithmeticException("На ноль делить нельзя!");
                            }
                            mResult = first / second;
                            break;
                        default:
                            mResult = 0;
                    }
                    resultText = "" + mResult;
                    if (Float.isInfinite(mResult)) {
                        throw new ArithmeticException("Переполнение");
                    }
                    resultText = splitZero(resultText);
                    addCharToText(resultText);
                }

            } catch (Exception e) {
                addCharToText(" : " + e.getMessage());
                mOperation = '!';

            }
        }
    };

    private final View.OnClickListener ButtonPlusClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mOperation == '!' || mOperation != ' ') {
                return;
            } else if (mFirst.contains("-") && mFirst.length() == 1) {
                initCalc();
            } else {
                mOperation = '+';
                addCharToText("+");
            }
        }
    };

    private final View.OnClickListener ButtonMinusClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mOperation == '!' || mOperation != ' ') {
                return;
            } else if (mFirst.equals("")) {
                addCharToParam("-");
            } else {
                mOperation = '-';
                addCharToText("-");
            }
        }
    };
    private final View.OnClickListener ButtonPersClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mOperation == '!' || mOperation != ' ' || mFirst.equals("")) return;
            mOperation = '%';
            addCharToText("%");
        }
    };

    private String splitZero(String resultText) {
        boolean exist0 = false;
        if (resultText.contains(".") || resultText.contains(",")) {
            exist0 = true;
        }
        while (exist0) {
            if (resultText.charAt(resultText.length() - 1) == '0' && resultText.length() != 1) {
                resultText = resultText.substring(0, resultText.length() - 1);
            } else if (resultText.charAt(resultText.length() - 1) == ',' || resultText.charAt(resultText.length() - 1) == '.') {
                resultText = resultText.substring(0, resultText.length() - 1);
                exist0 = false;
            } else {
                exist0 = false;
            }
        }
        return resultText;
    }

    private void addCharToParam(String key) {
        if (mtvResultText.contains("=")) {
            initCalc();
        }
        if (mOperation == '!') {
            return;
        } else if (mOperation == ' ') {
            if ((!key.equals(".") || !mFirst.contains(".")) && mFirst.length() < 15) {
                if (mFirst.length() == 0 && key.equals(".")) key = "0" + key;
                mFirst += key;
                addCharToText(key);
            }
        } else {
            if ((!key.equals(".") || !mSecond.contains(".")) && mSecond.length() < 15) {
                if (mSecond.length() == 0 && key.equals(".")) key = "0" + key;
                mSecond += key;
                addCharToText(key);
            }
        }
    }

    private void addCharToText(String key) {

        if (tvResult.getText() == "0") {
            setTvResult(key);
        } else {
            setTvResult(tvResult.getText() + key);
        }
    }

    private void setTvResult(String text) {
        mtvResultText = text;
        tvResult.setText(text);
    }

    private void initCalc() {
        mFirst = "";
        mSecond = "";
        mResult = 0f;
        mOperation = ' ';
        setTvResult("0");
    }

}