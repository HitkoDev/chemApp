/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hitkoDev.chemApp.rest;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import com.hitkoDev.chemApp.ChemApp;
import com.hitkoDev.chemApp.R;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author hitno
 */
public class LoadDataTask extends AsyncTask<String, Void, String> {
    
    private final Context context;
    private final OnJSONResponseListener listener;
    private final File cache;
    private File file;
    
    public LoadDataTask(Context c, OnJSONResponseListener l) {
        super();
        context = c;
        listener = l;
        cache = context.getExternalCacheDir();
    }
    
    public LoadDataTask executeCached(String... urls){
        file = new File(cache, IOLib.md5(urls[0]) + ".json");
        System.out.println(file);
        if(checkNetwork()){
            return (LoadDataTask) execute(urls);
        } else {
            new LoadFileTask().execute(urls);
            return this;
        }
    }
    
    private String buildURL(String... urls){
        StringBuilder url = new StringBuilder(context.getString(R.string.rest_base));
        for(String s : urls) url.append(s).append('/');
        return url.toString();
    }
    
    @Override
    protected String doInBackground(String... urls) {
        OkHttpClient client = ChemApp.client;
        Request request = new Request.Builder().url(buildURL(urls)).build();
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch(IOException e){
            return "Unable to retrieve web page. URL may be invalid.";
        }
    }
    
    @Override
    protected void onPostExecute(String result) {
        try {
            JSONObject json = new JSONObject(result);
            if(json.has("success") && !json.getBoolean("success")) throw new JSONException(json.has("message") ? json.getString("message") : "Unknown response");
            new StoreFileTask().execute(result);
            if(listener != null) listener.onSuccess(json);
        } catch (JSONException ex) {
            Logger.getLogger(LoadDataTask.class.getName()).log(Level.SEVERE, null, ex);
            if(listener != null) listener.onFail(ex.getMessage());
        }
    }
    
    public boolean checkNetwork(){
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
    
    private boolean checkFile(String name){
        File f = new File(cache, name);
        return f != null && f.exists();
    }
    
    private class LoadFileTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String url = buildURL(urls);
            String file = IOLib.md5(url) + ".json";
            if(checkFile(file)){
                try(InputStream is = new FileInputStream(new File(cache, file))) {
                    return IOLib.readStream(is);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(LoadDataTask.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(LoadDataTask.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return context.getString(R.string.no_network);
        }
    
        @Override
        protected void onPostExecute(String result) {
            LoadDataTask.this.onPostExecute(result);
        }

    }
    
    private class StoreFileTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... data) {
            String result = data[0];
            
            try (OutputStream os = new FileOutputStream(file)) {
                os.write(result.getBytes());
            } catch (IOException ex) {
                Logger.getLogger(LoadDataTask.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            return null;
        }

    }
    
}
