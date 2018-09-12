package com.example.e_vasiliev.moneyconverter.network.retrofit.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Содержит список всех доступных валют для конвертации
 */
public class CurrencyModel {
    @SerializedName("results")
    @Expose
    private Map<String, CurrencyMeta> results;


    public Map<String, CurrencyMeta> getResults() {
        return results;
    }


    public void setResults(Map<String, CurrencyMeta> results) {
        this.results = results;
    }
}
