package com.example.e_vasiliev.moneyconverter.network.retrofit.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Содержит информацию о конвертации валют
 */
public class ConverterOutput {
    @Expose
    @SerializedName("results")
    private LinkedHashMap<String, ConverterOutputMeta> results;


    public LinkedHashMap<String, ConverterOutputMeta> getResults() {
        return results;
    }
}
