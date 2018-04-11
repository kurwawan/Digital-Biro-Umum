package com.example.asus.d_biumsemarang1.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by ASUS on 12/5/2017.
 */
//Mencari lokasi
public class GoogleMapLoader extends AsyncTaskLoader<String> {
    private String mData;
    private boolean isFirstTime = true;
    private String mUrl;

    public GoogleMapLoader(@NonNull Context context, String url) {
        super(context);
        mUrl = url;
    }


    @Override
    protected void onStartLoading() {
        if(isFirstTime){
            isFirstTime = false;
            forceLoad();
        }
        else{
            deliverResult(mData);
        }
    }
    @Override
    public String loadInBackground() {
        URL url;
        String result;
        try {
            url = createURL(mUrl);
            result = openReadConnection(url);
        } catch (MalformedURLException ex) {
            return null;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return result;
    }
    private URL createURL(String url) throws MalformedURLException {
        URL urlFinal = new URL(url);
        return urlFinal;
    }

    private String openReadConnection(URL url) throws IOException {
        InputStream inputStream = null;
        HttpURLConnection connection = null;
        String result = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.connect();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = connection.getInputStream();
                result = readByteToString(inputStream);
            } else {
                return null;
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            connection.disconnect();
        }
        return result;
    }

    @Override
    public void deliverResult(@Nullable String data) {
        mData = data;
        super.deliverResult(data);
    }
    private String readByteToString(InputStream inputStream) throws IOException {
        if (inputStream != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder builder = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                builder.append(line);
                line = reader.readLine();
            }
            return builder.toString();
        }
        return null;
    }
}
