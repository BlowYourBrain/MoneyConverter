package com.example.e_vasiliev.moneyconverter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.e_vasiliev.moneyconverter.network.retrofit.RetrofitBuilder;
import com.example.e_vasiliev.moneyconverter.network.retrofit.models.ConverterOutput;
import com.example.e_vasiliev.moneyconverter.network.retrofit.models.ConverterOutputMeta;
import com.example.e_vasiliev.moneyconverter.network.retrofit.models.CurrencyModel;
import com.example.e_vasiliev.moneyconverter.utils.Utils;

import java.util.LinkedHashMap;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private final String DEBUG_KEY = "debugkey";

    private final String CURRENT_STATE = "currentstate";

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

    private CurrencyModel mCurrencyModel;

    private ConverterOutput mConverterOutput;

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
        GET_ERROR,
        SHOWING_RESULT,
    }

    private State mCurrentState = State.DEFAULT;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(CURRENT_STATE, mCurrentState);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mCurrentState = (State) savedInstanceState.getSerializable(CURRENT_STATE);
        }

        mViewFrom = findViewById(R.id.convert_from);
        mViewTo = findViewById(R.id.convert_to);
        mResultView = findViewById(R.id.result);
        mDataView = findViewById(R.id.output_field);
        mConvertButton = findViewById(R.id.convert);
        mCurrencyModelCall = RetrofitBuilder.getCurrencyRequest().getCurrency();

        setupData();
        setupButton();
    }


    @Override
    protected void onDestroy() {
        if (mCurrencyModelCall != null) mCurrencyModelCall.cancel();
        if (mConverterOutputCall != null) mConverterOutputCall.cancel();
        super.onDestroy();
    }


    private void setupButton() {
        mConvertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startConvertation();
            }
        });
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
                    }


                    @Override
                    public void onFailure(Call<CurrencyModel> call, Throwable t) {
                        t.fillInStackTrace();
                    }
                });


            } else {
                //получить даныне из кэша
                Toast.makeText(this, "нет интернета, нужно взять данные из кэша", Toast.LENGTH_SHORT).show();
            }
        } else if (mCurrencyModel.getResults() != null) {
            fillAutoCompleteDataIntoViews(mCurrencyModel.getResults().keySet());
        }
    }


    private void startConvertation() {

        if (mConverterOutputCall == null || mConverterOutputCall.isExecuted()) {
            String query = getConvertCurrency();

            if (query != null) {
                mConverterOutputCall = RetrofitBuilder.getCurrencyRequest().getConverter(query);
                setState(State.IN_PROGRESS);
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
                            }
                        }
                    }


                    @Override
                    public void onFailure(Call<ConverterOutput> call, Throwable t) {
                        t.printStackTrace();
                    }
                });


            } else {
                // TODO: 12.09.18 указать пользователю на некорректность данных
                Toast.makeText(this, "Некорректные данные", Toast.LENGTH_SHORT).show();
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
            ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, convertedData);
            view.setAdapter(adapter);
        }
        view.setThreshold(MINIMUM_INPUT_CHARS_COUNT);
    }


    /**
     * Установить состояние элементов View
     */
    private void setState(State state) {
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
        }
    }


    private void clearAll() {
        mConvertButton.setText(R.string.convert);
        mViewFrom.setText("");
        mViewTo.setText("");
        shouldEnableViewsForInput(true);
        shouldShowResultViews(false);
        shouldShowProgress(false);
    }


    private void inProgress() {
        shouldEnableViewsForInput(false);
        shouldShowProgress(true);
    }


    private void resultShow() {
        shouldShowResultViews(true);
        shouldEnableViewsForInput(true);
        shouldShowProgress(false);
    }


    private void getError() {
        shouldEnableViewsForInput(true);
        shouldShowProgress(false);
    }

}
