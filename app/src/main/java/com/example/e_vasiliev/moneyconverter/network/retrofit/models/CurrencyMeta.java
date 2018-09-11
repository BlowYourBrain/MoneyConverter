package com.example.e_vasiliev.moneyconverter.network.retrofit.models;

import com.google.gson.annotations.Expose;

/**
 * Мета-данные для класса {@link CurrencyModel}
 */
public class CurrencyMeta {

	@Expose
	private String currencyName;
	@Expose
	private String id;


	/**
	 * Название валюты
	 */
	public String getCurrencyName() {
		return currencyName;
	}


	/**
	 * ID валюты (судя по API состоит из 3-х символов)
	 */
	public String getId() {
		return id;
	}
}
