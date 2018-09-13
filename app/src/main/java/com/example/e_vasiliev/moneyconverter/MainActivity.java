package com.example.e_vasiliev.moneyconverter;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.StandaloneActionMode;
import android.text.Editable;
import android.text.InputFilter;
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
import com.example.e_vasiliev.moneyconverter.utils.CurrencyInputFilter;
import com.example.e_vasiliev.moneyconverter.utils.Utils;

import java.util.LinkedHashMap;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

    private TextInputLayout mTextInputViewFrom;
    private TextInputLayout mTextInputViewTo;
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
    private CoordinatorLayout mCoordinatorLayout;
    private Handler mHandler;

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
        mHandler = new Handler();
        mCoordinatorLayout = findViewById(R.id.coordinator);
        mTextInputViewFrom = findViewById(R.id.textInputConvertFrom);
        mTextInputViewTo = findViewById(R.id.textInputConvertTo);
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


    private void setupButton() {
        mConvertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonAction();
            }
        });
    }


    private void buttonAction() {
        Utils.hideKeyboard(this);
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
        mCurrencyModel = Utils.getCurrencyFromCache(this);
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
                        Utils.saveCurrencyInCache(MainActivity.this, mCurrencyModel);
                    }


                    @Override
                    public void onFailure(Call<CurrencyModel> call, Throwable t) {
                        t.fillInStackTrace();
                    }
                });
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

        String errorMessage = getString(R.string.incorrect_input);
        CurrencyInputFilter filterFrom = new CurrencyInputFilter(mTextInputViewFrom, errorMessage, mHandler);
        CurrencyInputFilter filterTo = new CurrencyInputFilter(mTextInputViewTo, errorMessage, mHandler);

        InputFilter[] filter = new InputFilter[]{filterFrom};
        mViewFrom.setFilters(filter);
        filter = new InputFilter[]{filterTo};
        mViewTo.setFilters(filter);
        mResultView.setText(mResultValue);
    }


    private void startConvertation() {
        final String query = getConvertCurrency();


        if (query != null) {
            setState(State.IN_PROGRESS);
        } else {
            Utils.showMessage(mCoordinatorLayout, R.string.invalid_data);
            return;
        }

        if (Utils.isOnline(this)) {
            if (mConverterOutputCall == null || mConverterOutputCall.isExecuted()) {
                mConverterOutputCall = RetrofitBuilder.getCurrencyRequest().getConverter(query);
                mConverterOutputCall.enqueue(new Callback<ConverterOutput>() {
                    @Override
                    public void onResponse(Call<ConverterOutput> call, Response<ConverterOutput> response) {
                        ConverterOutput converterOutput = response.body();
                        if (converterOutput != null && fillResults(converterOutput, query)) {
                            Utils.saveConverterOutput(MainActivity.this, converterOutput, query);
                            setState(State.SHOWING_RESULT);
                        } else {
                            setState(State.GET_ERROR);
                            Utils.showMessage(mCoordinatorLayout, R.string.not_found);
                        }
                    }


                    @Override
                    public void onFailure(Call<ConverterOutput> call, Throwable t) {
                        t.printStackTrace();
                        setState(State.GET_ERROR);
                        Utils.showMessage(mCoordinatorLayout, R.string.unexpected_error);
                    }
                });
            }
        } else {
            ConverterOutput converterOutput = Utils.getConverterOutputFromCache(this, query);

            if (converterOutput == null) {
                setState(State.ON_DATA_CHANGE);
                Utils.showMessage(mCoordinatorLayout, R.string.no_internet);
            } else {
                fillResults(converterOutput, query);
                setState(State.SHOWING_RESULT);
                Utils.showMessage(mCoordinatorLayout, R.string.cached_data);
            }

        }


    }


    /**
     * @param converterOutput - данные, которые получены с сервера / взяты из кэша
     * @param query           - строка формата ID1_ID2, где ID1 - это id валюты из которой происходит конвертация,
     *                        а ID2 - id валюты в которую следует конвертировать данные
     * @return true - в том случае, если все прошло успешно и данные установлены во view элементы
     * false - если произошла ошибка
     */
    private boolean fillResults(ConverterOutput converterOutput, String query) {
        if (converterOutput.getDatetime() != null)
            Log.d("converter", "показать время: " + converterOutput.getDatetime().toString());

        try {
            float value = converterOutput.getResults().get(query).getValue();
            mResultView.setText(String.valueOf(value));
            Log.d("converter", "query " + query + " is: " + value);
            return true;

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * @return - вернёт строку формата ID1_ID2 если поля {@link #mViewFrom} и {@link #mViewTo}
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
        shouldEnableViewsForInput(true);
        shouldShowProgress(false);
        mResultValue = null;
    }

}
