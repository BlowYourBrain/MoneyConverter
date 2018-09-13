package com.example.e_vasiliev.moneyconverter.network.retrofit.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Содержит информацию о конвертации валют
 */
public class ConverterOutput {
    @Expose
    @SerializedName("results")
    private LinkedHashMap<String, ConverterOutputMeta> results;

    @Expose
    private Date datetime;


    public LinkedHashMap<String, ConverterOutputMeta> getResults() {
        return results;
    }


    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }


    public Date getDatetime() {
        return datetime;
    }
}
