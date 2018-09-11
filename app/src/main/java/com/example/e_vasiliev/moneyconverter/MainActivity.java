package com.example.e_vasiliev.moneyconverter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.e_vasiliev.moneyconverter.network.retrofit.RetrofitBuilder;
import com.example.e_vasiliev.moneyconverter.network.retrofit.models.ConverterOutput;
import com.example.e_vasiliev.moneyconverter.network.retrofit.models.CurrencyModel;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
	private final String DEBUG_KEY = "debugkey";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		RetrofitBuilder.getCurrencyRequest().getCurrency().enqueue(new Callback<CurrencyModel>() {
			@Override
			public void onResponse(Call<CurrencyModel> call, Response<CurrencyModel> response) {
				Log.d(DEBUG_KEY, "response code: " + response.code());
				CurrencyModel currencyModel = response.body();
				if (currencyModel != null) {
					for (String key : currencyModel.getResults().keySet()) {
						Log.d(DEBUG_KEY, key);
					}
				}
			}


			@Override
			public void onFailure(Call<CurrencyModel> call, Throwable t) {
				t.fillInStackTrace();
			}
		});

		RetrofitBuilder.getCurrencyRequest().getConverter("USD_RUB").enqueue(new Callback<ConverterOutput>() {
			@Override
			public void onResponse(Call<ConverterOutput> call, Response<ConverterOutput> response) {
				Log.d(DEBUG_KEY, "response code: " + response.code());
				ConverterOutput output = response.body();
				for (String key : output.getResults().keySet()) {
					Log.d(DEBUG_KEY, String.valueOf(output.getResults().get(key).getValue()));
				}
			}


			@Override
			public void onFailure(Call<ConverterOutput> call, Throwable t) {
				t.fillInStackTrace();
			}
		});
	}
}
