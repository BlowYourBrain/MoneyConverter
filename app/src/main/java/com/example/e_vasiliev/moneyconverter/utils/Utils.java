package com.example.e_vasiliev.moneyconverter.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.StringRes;
import android.util.Log;
import android.widget.Toast;

import com.example.e_vasiliev.moneyconverter.network.retrofit.models.CurrencyModel;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
     * @param context - {@link Context}
     * @param message - строковая контстанта, которая будет отображена в сообщении
     */
    public static void toast(Context context, @StringRes int message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static boolean checkText(CharSequence text){
        if (pattern.matcher(text).find()){
            Log.d("glibglob", "найдено совпадение");
            return true;
        }else {
            Log.d("glibglob", "совпадение не найдено");
        }
        return false;
    }


    public static CurrencyModel getFromCache(Context context) {
        try {
            FileInputStream inputStream = context.openFileInput(filename);

            inputStream.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void saveInCache(Context context, CurrencyModel currencyModels) {

        Gson gson = new Gson();
        String json = gson.toJson(currencyModels);
        Log.d("fuck", json);
        File file = new File(context.getCacheDir(), filename);
        if (file.exists()) {
            Log.d("fuck", "файл существует");
            deleteFile(file);
        } else {
            Log.d("fuck", "файл не существует");
        }

        createFile(context, filename);
        writeInFile(context, filename, json);
    }


    private static File createFile(Context context, String filename) {
        Log.d("fuck", "создаём файл");
        File file = null;
        try {
            file = File.createTempFile(filename, null, context.getCacheDir());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }


    private static void writeInFile(Context context, String filename, String data) {
        Log.d("fuck", "пишем в файл");
        try {
            OutputStream outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(data.getBytes());
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void deleteFile(File file) {
        file.delete();
        Log.d("fuck", "успешно удалил файл");
    }
}
