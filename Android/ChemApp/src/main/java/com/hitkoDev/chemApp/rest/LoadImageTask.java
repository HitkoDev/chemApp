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
import com.hitkoDev.chemApp.MainActivity;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    
    public boolean checkNetwork(){
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
        file = new File(cache, IOLib.md5(urls[0]) + ".png");
        System.out.println(file);
        if(file.exists()){
            return BitmapFactory.decodeFile(file.getPath());
        } else if(checkNetwork()){
            OkHttpClient client = ChemApp.client;
            Request request = new Request.Builder().url(urls[0]).build();
            try {
                Response response = client.newCall(request).execute();
                return BitmapFactory.decodeStream(response.body().byteStream());
            } catch(IOException ex){
                Logger.getLogger(MainActivity.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        if(result == null){
            listener.onFail("Can't load the image");
        } else {
            listener.onSuccess(result);
            new StoreFileTask().execute(result);
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
