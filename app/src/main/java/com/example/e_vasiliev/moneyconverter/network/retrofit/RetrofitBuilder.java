package com.example.e_vasiliev.moneyconverter.network.retrofit;

import com.example.e_vasiliev.moneyconverter.network.retrofit.models.ConverterOutput;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Позволяет получить полностью подготовленный запрос в сеть
 */
public final class RetrofitBuilder {
	private RetrofitBuilder() {

	}


	private static final String BASE_URL = "https://free.currencyconverterapi.com/api/v6/";


	private static Retrofit retrofit = new Retrofit.Builder()
			.baseUrl(BASE_URL)
			.addConverterFactory(GsonConverterFactory.create())
			.build();


	/**
	 * Запрос на получение существующих валют
	 */
	public static MoneyConverterRequests getCurrencyRequest() {
		return retrofit.create(MoneyConverterRequests.class);
	}
}
