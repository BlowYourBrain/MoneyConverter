package com.example.e_vasiliev.moneyconverter.network.retrofit.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ConverterOutputMeta {
	@Expose
	@SerializedName("val")
	float value;


	public float getValue() {
		return value;
	}
}
