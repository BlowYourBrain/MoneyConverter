package com.example.e_vasiliev.moneyconverter.network.retrofit;

import com.example.e_vasiliev.moneyconverter.network.retrofit.models.ConverterOutput;
import com.example.e_vasiliev.moneyconverter.network.retrofit.models.CurrencyModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MoneyConverterRequests {
	/**
	 * Получить запрос доступных валют для конвертации
	 */
	@GET("currencies/")
	Call<CurrencyModel> getCurrency();

	/**
	 * Получить запрос для конвертации валют
	 *
	 * @param convertId - строка, формата idFrom_idTo, где
	 *                  idFrom - id валюты из которой мы собираемся конветировать
	 *                  idTo - id валюты в которую мы собираемся конвертировать
	 */
	@GET("convert/")
	Call<ConverterOutput> getConverter(@Query("q") String convertId);
}
