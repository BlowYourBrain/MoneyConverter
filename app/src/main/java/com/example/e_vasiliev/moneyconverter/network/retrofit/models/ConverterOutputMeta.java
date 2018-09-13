package com.example.e_vasiliev.moneyconverter.network.retrofit.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ConverterOutputMeta {
    @Expose
    @SerializedName("id")
    private String id;

    @Expose
    @SerializedName("val")
    private float value;


    public float getValue() {
        return value;
    }


    public String getId() {
        return id;
    }
}
