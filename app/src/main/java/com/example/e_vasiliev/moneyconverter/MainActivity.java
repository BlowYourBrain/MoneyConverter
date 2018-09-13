package com.example.e_vasiliev.moneyconverter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.e_vasiliev.moneyconverter.network.retrofit.RetrofitBuilder;
import com.example.e_vasiliev.moneyconverter.network.retrofit.models.ConverterOutput;
import com.example.e_vasiliev.moneyconverter.network.retrofit.models.ConverterOutputMeta;
import com.example.e_vasiliev.moneyconverter.network.retrofit.models.CurrencyModel;
import com.example.e_vasiliev.moneyconverter.utils.Utils;

import java.util.LinkedHashMap;
import java.util.Set;

import okhttp3.internal.Util;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.e_vasiliev.moneyconverter.utils.Utils.getFromCache;

public class MainActivity extends AppCompatActivity {
    private final String DEBUG_KEY = "debugkey";
    private final String CURRENT_STATE = "currentstate";
    private final String RESULT_VALUE = "resultvalue";
    /**
     * View в которую вводится информация, из какой валюты провести конвертацию
     */
    private AutoCompleteTextView mViewFrom;

    /**
     * View в которую вводится информация, в какую валюту провести конвертацию
     */
    private AutoCompleteTextView mViewTo;
    /**
     * View в которой выводится результат конвертации
     */
    private TextView mResultView;

    /**
     * View с константной информацией
     */
    private TextView mDataView;

    private Button mConvertButton;

    private ProgressBar mProgressBar;

    private CurrencyModel mCurrencyModel;

    /**
     * Запрос, позволяющий получить ответ {@link CurrencyModel}
     */
    private Call<CurrencyModel> mCurrencyModelCall;

    /**
     * Запрос, позволяющий получить ответ {@link ConverterOutput}
     */
    private Call<ConverterOutput> mConverterOutputCall;


    private enum State {
        DEFAULT,
        IN_PROGRESS,
        SHOWING_RESULT,
        GET_ERROR,
        ON_DATA_CHANGE
    }

    private State mCurrentState = State.DEFAULT;
    private String mResultValue;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(CURRENT_STATE, mCurrentState);
        outState.putString(RESULT_VALUE, mResultValue);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mCurrentState = (State) savedInstanceState.getSerializable(CURRENT_STATE);
            mResultValue = savedInstanceState.getString(RESULT_VALUE, null);
        }

        mViewFrom = findViewById(R.id.convert_from);
        mViewTo = findViewById(R.id.convert_to);
        mResultView = findViewById(R.id.result);
        mDataView = findViewById(R.id.output_field);
        mConvertButton = findViewById(R.id.convert);
        mProgressBar = findViewById(R.id.progressBar);
        mCurrencyModelCall = RetrofitBuilder.getCurrencyRequest().getCurrency();

        setupViews();
        setupData();
        setupButton();
        setState(true, mCurrentState);

    }


    @Override
    protected void onDestroy() {
//        if (mCurrencyModelCall != null) mCurrencyModelCall.cancel();
//        if (mConverterOutputCall != null) mConverterOutputCall.cancel();
        super.onDestroy();
    }


    private void setupButton() {
        mConvertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonAction();
            }
        });
    }


    private void buttonAction() {
        if (mCurrentState == State.SHOWING_RESULT) {
            setState(State.DEFAULT);
            return;
        }

        if (mCurrentState != State.IN_PROGRESS) {
            startConvertation();
            return;
        }
    }


    /**
     * Получить данные о существующих валютах
     */
    private void setupData() {
        if (mCurrencyModel == null) {


            //проверить доступ в интернет
            if (Utils.isOnline(this)) {
                mCurrencyModelCall.enqueue(new Callback<CurrencyModel>() {
                    @Override
                    public void onResponse(Call<CurrencyModel> call, Response<CurrencyModel> response) {
                        mCurrencyModel = response.body();
                        if (mCurrencyModel != null && mCurrencyModel.getResults() != null) {
                            fillAutoCompleteDataIntoViews(mCurrencyModel.getResults().keySet());
                        }
                        Utils.saveInCache(MainActivity.this, mCurrencyModel);
                    }


                    @Override
                    public void onFailure(Call<CurrencyModel> call, Throwable t) {
                        t.fillInStackTrace();
                    }
                });
            } else {
                // TODO: 12.09.18 взять данные из кэша
                //получить даныне из кэша
//                mCurrencyModel = getFromCache();
                if (mCurrencyModel != null && mCurrencyModel.getResults() != null) {
                    fillAutoCompleteDataIntoViews(mCurrencyModel.getResults().keySet());
                }
            }
        } else if (mCurrencyModel.getResults() != null) {
            fillAutoCompleteDataIntoViews(mCurrencyModel.getResults().keySet());
        }
    }


    private void setupViews() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }


            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setState(State.ON_DATA_CHANGE);
            }


            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        mViewFrom.addTextChangedListener(textWatcher);
        mViewTo.addTextChangedListener(textWatcher);


        final byte MAX_LENGTH = 3;
        //фильтр для ввода текста
        InputFilter inputFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if ((end > dend) && Utils.checkText(source)) {
                    if (source.length() > MAX_LENGTH) {
                        return source.subSequence(0, MAX_LENGTH);
                    }
                    return null;
                }

                if (source.length() != 0 && end > dend)
                    Utils.toast(MainActivity.this, R.string.incorrect_input);

                return source.subSequence(start, end > dend ? dend : end);
            }
        };
        InputFilter[] filter = new InputFilter[]{inputFilter};
        mViewFrom.setFilters(filter);
        mViewTo.setFilters(filter);


        mResultView.setText(mResultValue);
    }


    private void startConvertation() {

        if (mConverterOutputCall == null || mConverterOutputCall.isExecuted()) {
            String query = getConvertCurrency();

            if (query != null) {
                setState(State.IN_PROGRESS);
                mConverterOutputCall = RetrofitBuilder.getCurrencyRequest().getConverter(query);
                mConverterOutputCall.enqueue(new Callback<ConverterOutput>() {
                    @Override
                    public void onResponse(Call<ConverterOutput> call, Response<ConverterOutput> response) {
                        ConverterOutput converterOutput = response.body();


                        if (converterOutput != null) {
                            LinkedHashMap<String, ConverterOutputMeta> output = converterOutput.getResults();


                            if (output.keySet().size() > 0) {
                                final int INDEX = 0;    //по этой позиции будут браться данные
                                ConverterOutputMeta a = (ConverterOutputMeta) output.values().toArray()[INDEX];
                                mResultView.setText(String.valueOf(a.getValue()));
                                setState(State.SHOWING_RESULT);

                            } else {
                                // TODO: 12.09.18 указать что пришёл некорректный ответ с сервера
                                setState(State.GET_ERROR);
                                Utils.toast(MainActivity.this, R.string.not_found);
                            }
                        }
                    }


                    @Override
                    public void onFailure(Call<ConverterOutput> call, Throwable t) {
                        t.printStackTrace();
                        setState(State.GET_ERROR);
                        Utils.toast(MainActivity.this, R.string.unexpected_error);
                    }
                });


            } else {
                Utils.toast(this, R.string.invalid_data);
            }
        }
    }


    private void shouldEnableViewsForInput(boolean shouldEnable) {
        mViewFrom.setEnabled(shouldEnable);
        mViewTo.setEnabled(shouldEnable);
    }


    private void shouldShowResultViews(boolean shouldShow) {
        int flag = shouldShow ? View.VISIBLE : View.GONE;
        mResultView.setVisibility(flag);
        mDataView.setVisibility(flag);
    }


    private void shouldShowProgress(boolean shouldShow) {
        if (shouldShow) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
        }
    }


    private void fillAutoCompleteDataIntoViews(Set<String> data) {
        setupLoginAutoCompletion(mViewFrom, data);
        setupLoginAutoCompletion(mViewTo, data);
    }


    private void setupLoginAutoCompletion(AutoCompleteTextView view, Set<String> data) {
        //  минимальное количество символов которое пользователь должен ввести
        //  прежде чем появится выпадающий список с подсказкой
        final int MINIMUM_INPUT_CHARS_COUNT = 1;

        if (data != null) {
            String[] convertedData = data.toArray(new String[data.size()]);
            ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, convertedData);
            view.setAdapter(adapter);
        }
        view.setThreshold(MINIMUM_INPUT_CHARS_COUNT);
    }


    /**
     * @return - вернёт строку формата ID1_ID2 если поля в {@link #mViewFrom} и {@link #mViewTo}
     * не являются пустыми. Иначе null
     */
    private String getConvertCurrency() {
        String currencyFrom = mViewFrom.getText().toString().trim();
        String currencyTo = mViewTo.getText().toString().trim();
        if (!"".equals(currencyFrom) && !"".equals(currencyTo)) {
            return currencyFrom + "_" + currencyTo;
        }
        return null;
    }


    /**
     * Установить состояние элементов View
     */
    private void setState(State state) {
        setState(false, state);
    }


    private void setState(boolean manualRefresh, State state) {
        if (mCurrentState != state || manualRefresh) {

            mCurrentState = state;
            Log.d(DEBUG_KEY, "current state: " + state);
            switch (state) {
                case DEFAULT:
                    clearAll();
                    break;

                case IN_PROGRESS:
                    inProgress();
                    break;

                case GET_ERROR:
                    getError();
                    break;

                case SHOWING_RESULT:
                    resultShow();
                    break;

                case ON_DATA_CHANGE:
                    onDataChange();
                    break;
            }
        }
    }


    private void clearAll() {
        mConvertButton.setText(R.string.convert);
        mViewFrom.setText("");
        mViewTo.setText("");
        shouldEnableViewsForInput(true);
        shouldShowResultViews(false);
        shouldShowProgress(false);
        mResultValue = null;
    }


    private void inProgress() {
        shouldEnableViewsForInput(false);
        shouldShowProgress(true);
        mConvertButton.setText(null);
    }


    private void resultShow() {
        mConvertButton.setText(R.string.reset);
        shouldShowResultViews(true);
        shouldEnableViewsForInput(true);
        shouldShowProgress(false);
        mResultValue = mResultView.getText().toString();
    }


    private void getError() {
        mConvertButton.setText(R.string.convert);
        shouldEnableViewsForInput(true);
        shouldShowProgress(false);
    }


    private void onDataChange() {
        mConvertButton.setText(R.string.convert);
        shouldShowResultViews(false);
        shouldShowProgress(false);
        mResultValue = null;
    }

}
