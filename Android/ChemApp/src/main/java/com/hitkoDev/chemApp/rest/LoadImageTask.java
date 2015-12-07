/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hitkoDev.chemApp.rest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import com.hitkoDev.chemApp.ChemApp;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hitno
 */
public class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
    
    private final Context context;
    private final OnImageLoadedListener listener;
    private final File cache;
    private File file;
    
    public LoadImageTask(Context c, OnImageLoadedListener l) {
        super();
        context = c;
        listener = l;
        cache = context.getExternalCacheDir();
    }
    
    public LoadImageTask executeCached(String... urls){
        file = new File(cache, IOLib.md5(urls[0]) + ".png");
        System.out.println(file);
        if(file != null && file.exists()){
            new LoadFileTask().execute(urls);
            return this;
        } else if(checkNetwork()){
            return (LoadImageTask) execute(urls);
        } else {
            if(listener != null) listener.onFail("No network or cached files");
            return this;
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

    @Override
    protected Bitmap doInBackground(String... urls) {
        OkHttpClient client = ChemApp.client;
        Request request = new Request.Builder().url(urls[0]).build();
        try {
            Response response = client.newCall(request).execute();
            return BitmapFactory.decodeStream(response.body().byteStream());
        } catch(IOException e){
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        if(result == null){
            if(listener != null) listener.onFail("Can't load the image");
        } else {
            new StoreFileTask().execute(result);
            if(listener != null) listener.onSuccess(result);
        }
    }
    
    private class LoadFileTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {
            String file = IOLib.md5(urls[0]) + ".png";
            if(checkFile(file)){
                try(InputStream is = new FileInputStream(new File(cache, file))) {
                    return BitmapFactory.decodeStream(is);
                } catch (IOException ex) {
                    Logger.getLogger(LoadImageTask.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            LoadImageTask.this.onPostExecute(result);
        }

    }
    
    private class StoreFileTask extends AsyncTask<Bitmap, Void, Void> {

        @Override
        protected Void doInBackground(Bitmap... data) {
            Bitmap image = data[0];
            
            try (OutputStream os = new FileOutputStream(file)) {
                image.compress(Bitmap.CompressFormat.PNG, 100, os);
            } catch (IOException ex) {
                Logger.getLogger(LoadDataTask.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            return null;
        }

    }
    
}
