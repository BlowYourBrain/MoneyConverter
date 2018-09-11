package com.example.e_vasiliev.moneyconverter.network.retrofit.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;
/**Содержит список всех доступных валют для конвертации */
public class CurrencyModel {
	@SerializedName("results")
	@Expose
	public Map<String, CurrencyMeta> results;


	public Map<String, CurrencyMeta> getResults() {
		return results;
	}
}
