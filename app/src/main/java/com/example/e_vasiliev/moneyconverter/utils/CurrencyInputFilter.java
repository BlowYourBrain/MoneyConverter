package com.example.e_vasiliev.moneyconverter.utils;

import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.text.Spanned;

import java.util.Timer;
import java.util.TimerTask;


public class CurrencyInputFilter implements android.text.InputFilter {
    private Timer timer;
    private long TIMER_DELAY = 2000;

    private TextInputLayout textInputLayout;
    private String errorMessage;

    private Handler handler;
    //максимальная длина поля ввода
    private final byte MAX_LENGTH = 3;


    /**
     * @param textInputLayout - layout в который обернут дочерний элемент
     */
    public CurrencyInputFilter(TextInputLayout textInputLayout, String errorMessage, Handler handler) {
        this.textInputLayout = textInputLayout;
        this.errorMessage = errorMessage;
        this.handler = handler;
        dismissError();
    }


    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        if ((end > dend) && Utils.checkText(source)) {
            if (source.length() > MAX_LENGTH) {
                return source.subSequence(0, MAX_LENGTH);
            }
            return null;
        }

        if (source.length() != 0 && end > dend) {
            textInputLayout.setError(errorMessage);
            setTimer();
        }
        return source.subSequence(start, end > dend ? dend : end);
    }


    /**
     * Таймер по истечении которого сообщение об ошибке будет убрано
     */
    private void setTimer() {
        if (timer != null) {
            timer.cancel();
        }

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                dismissError();
            }
        }, TIMER_DELAY);
    }


    private void dismissError() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                textInputLayout.setError(null);
            }
        });
    }
}
