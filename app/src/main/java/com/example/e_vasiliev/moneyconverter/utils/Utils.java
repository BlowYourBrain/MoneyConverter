package com.example.e_vasiliev.moneyconverter.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.e_vasiliev.moneyconverter.network.retrofit.models.ConverterOutput;
import com.example.e_vasiliev.moneyconverter.network.retrofit.models.CurrencyModel;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.regex.Pattern;

public final class Utils {
    private static final String filename = "currencies";
    private static final Pattern pattern = Pattern.compile("^([a-z]+)$", Pattern.CASE_INSENSITIVE);


    private Utils() {
    }


    /**
     * @return true - если есть интернет соедениние
     * false - если соединения нет
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    /**
     * @param layout  - view, в которой будет расположен snackbar. Предпочтительнее {@link android.support.design.widget.CoordinatorLayout}
     * @param message - строковая контстанта, которая будет отображена в сообщении
     */
    public static void showMessage(View layout, @StringRes int message) {
        Snackbar.make(layout, message, Snackbar.LENGTH_LONG).show();
    }


    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager)
                activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }


    public static boolean checkText(CharSequence text) {
        if (pattern.matcher(text).find()) {
            return true;
        }
        return false;
    }


    public static void saveConverterOutput(Context context, ConverterOutput converterOutput, String id) {
        Log.d("converter", "пытаюсь сохранить ConverterOutput в кэш");
        if (converterOutput.getDatetime() == null) {
            converterOutput.setDatetime(Calendar.getInstance().getTime());
        }

        Gson gson = new Gson();
        String json = gson.toJson(converterOutput);

        deleteFileIfExists(context, id);
        createFile(context, id);
        writeInFile(context, id, json);
    }


    public static ConverterOutput getConverterOutputFromCache(Context context, String id) {
        Log.d("converter", "пытаюсь получить ConverterOutput из кэша");
        byte[] buffer = null;
        try {
            FileInputStream inputStream = context.openFileInput(id);
            buffer = new byte[inputStream.available()];
            inputStream.read(buffer, 0, buffer.length);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (buffer != null) {
            String json = new String(buffer);
            Gson gson = new Gson();
            return gson.fromJson(json, ConverterOutput.class);
        }


        return null;
    }


    public static CurrencyModel getCurrencyFromCache(Context context) {
        byte[] buffer = null;
        try {
            FileInputStream inputStream = context.openFileInput(filename);
            buffer = new byte[inputStream.available()];
            inputStream.read(buffer, 0, buffer.length);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (buffer != null) {
            String json = new String(buffer);
            Gson gson = new Gson();
            return gson.fromJson(json, CurrencyModel.class);
        }

        return null;
    }


    public static void saveCurrencyInCache(Context context, CurrencyModel currencyModels) {
        Gson gson = new Gson();
        String json = gson.toJson(currencyModels);
        deleteFileIfExists(context, filename);
        createFile(context, filename);
        writeInFile(context, filename, json);
    }


    //region манипуляции с файлом
    private static File createFile(Context context, String filename) {
        File file = null;
        try {
            file = File.createTempFile(filename, null, context.getCacheDir());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }


    private static void writeInFile(Context context, String filename, String data) {
        try {
            OutputStream outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(data.getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void deleteFileIfExists(Context context, String filename) {
        File file = new File(context.getCacheDir(), filename);
        if (file.exists()) {
            file.delete();
        }
    }
    //endregion
}