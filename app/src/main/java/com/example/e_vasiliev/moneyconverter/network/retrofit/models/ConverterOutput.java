package com.example.e_vasiliev.moneyconverter.network.retrofit.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Содержит ответ сервера о конвертации валют
 */
public class ConverterOutput {
	@Expose
	@SerializedName("results")
	private Map<String, ConverterOutputMeta> results;


	public Map<String, ConverterOutputMeta> getResults() {
		return results;
	}
}
