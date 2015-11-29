/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hitkoDev.chemApp.rest;

import android.content.Context;
import android.os.AsyncTask;
import com.hitkoDev.chemApp.ChemApp;
import com.hitkoDev.chemApp.R;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author hitno
 */
public class LoadDataTask extends AsyncTask<String, Void, String> implements OnJSONResponseListener {
    
    private Context context;
    
    public LoadDataTask(Context c) {
        super();
        context = c;
    }
    
    @Override
    protected String doInBackground(String... urls) {
        OkHttpClient client = ChemApp.client;
        StringBuilder url = new StringBuilder(context.getString(R.string.rest_base));
        for(String s : urls) url.append(s).append('/');
        Request request = new Request.Builder().url(url.toString()).build();
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
            onSuccess(json);
        } catch (JSONException ex) {
            Logger.getLogger(LoadDataTask.class.getName()).log(Level.SEVERE, null, ex);
            onFail(ex.getMessage());
        }
    }

    @Override
    public void onSuccess(JSONObject response) {
        try {
            System.out.println(response.getJSONObject("object").getString("name"));
        } catch (JSONException ex) {
            Logger.getLogger(LoadDataTask.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void onFail(String response) {
        System.out.println(response);
    }
    
}
