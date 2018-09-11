package com.example.e_vasiliev.moneyconverter.network.retrofit;

import android.view.ViewDebug;

import com.google.gson.annotations.Expose;

public class CurrencyMeta {
	@Expose
	private String currencyName;
	@Expose
	private String id;


	public String getCurrencyName() {
		return currencyName;
	}


	public void setCurrencyName(String currencyName) {
		this.currencyName = currencyName;
	}
}
