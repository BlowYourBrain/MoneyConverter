package com.example.e_vasiliev.moneyconverter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private final String DEBUG_KEY = "debugkey";

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

    private CurrencyModel mAllCurrency;

    private ConverterOutput mConverterOutput;

    /**
     * Запрос, позволяющий получить ответ {@link CurrencyModel}
     */
    private Call<CurrencyModel> mCurrencyModelCall;

    /**
     * Запрос, позволяющий получить ответ {@link ConverterOutput}
     */
    private Call<ConverterOutput> mConverterOutputCall;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewFrom = findViewById(R.id.convert_from);
        mViewTo = findViewById(R.id.convert_to);
        mResultView = findViewById(R.id.result);
        mDataView = findViewById(R.id.output_field);
        mConvertButton = findViewById(R.id.convert);
        mCurrencyModelCall = RetrofitBuilder.getCurrencyRequest().getCurrency();

        setupData();
        setupButton();
    }


    private void setupButton() {
        mConvertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startConvertation();
            }
        });
    }


    private void startConvertation() {

        if (mConverterOutputCall == null || !mConverterOutputCall.isExecuted()) {
            String query = getConvertCurrency();

            if (query != null) {
                mConverterOutputCall = RetrofitBuilder.getCurrencyRequest().getConverter(query);
                mConverterOutputCall.enqueue(new Callback<ConverterOutput>() {
                    @Override
                    public void onResponse(Call<ConverterOutput> call, Response<ConverterOutput> response) {
                        Log.d(DEBUG_KEY, "response code: " + response.code());


                        ConverterOutput converterOutput = response.body();
                        if (converterOutput != null) {
                            LinkedHashMap<String, ConverterOutputMeta> output = converterOutput.getResults();

                            if (output.keySet().size() > 0) {
                                ConverterOutputMeta a = (ConverterOutputMeta) output.values().toArray()[0];
                                mResultView.setText(String.valueOf(a.getValue()));
                                mResultView.setVisibility(View.VISIBLE);
                                mDataView.setVisibility(View.VISIBLE);

                            } else {
                                // TODO: 12.09.18 указать что пришёл некорректный ответ с сервера
                            }
                        }
                    }


                    @Override
                    public void onFailure(Call<ConverterOutput> call, Throwable t) {

                    }
                });
            } else {
                // TODO: 12.09.18 указать пользователю на некрректность данных
                Toast.makeText(this, "Некорректные данные", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void shouldEnableViewsForInput(boolean shouldEnable) {
        mViewFrom.setEnabled(shouldEnable);
        mViewTo.setEnabled(shouldEnable);

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


    private void setupData() {
        if (mConverterOutput == null) {
            //проверить доступ в интернет
            if (Utils.isOnline(this)) {


                mCurrencyModelCall.enqueue(new Callback<CurrencyModel>() {
                    @Override
                    public void onResponse(Call<CurrencyModel> call, Response<CurrencyModel> response) {
                        Log.d(DEBUG_KEY, "response code: " + response.code());
                        mAllCurrency = response.body();
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
        }
    }


    @Override
    protected void onDestroy() {
        if (mCurrencyModelCall != null) mCurrencyModelCall.cancel();
        if (mConverterOutputCall != null) mConverterOutputCall.cancel();
        super.onDestroy();
    }
}
