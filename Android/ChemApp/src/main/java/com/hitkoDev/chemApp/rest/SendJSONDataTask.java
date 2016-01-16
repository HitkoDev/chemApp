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
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import org.json.JSONObject;

/**
 *
 * @author hitno
 */
public class SendJSONDataTask extends AsyncTask<String, Void, String> {

    private final Context context;
    private final OnJSONResponseListener listener;

    public SendJSONDataTask(Context c, OnJSONResponseListener l) {
        super();
        context = c;
        listener = l;
    }

    private String buildURL(String... urls) {
        StringBuilder url = new StringBuilder(context.getString(R.string.rest_base));
        int i = 0;
        for (String s : urls) {
            if (i > 0) {
                url.append(s).append('/');
            }
            i++;
        }
        return url.toString();
    }

    @Override
    protected String doInBackground(String... urls) {
        if (checkNetwork()) {
            String data = urls[0];
            String url = buildURL(urls);
            OkHttpClient client = ChemApp.client;
            RequestBody body = RequestBody.create(IOLib.JSON, data);
            Request request = new Request.Builder().url(url).post(body).build();
            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (Exception e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        } else {
            return "No network or cached files";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        JSONObject json;
        try {
            json = new JSONObject(result);
        } catch (Exception ex) {
            listener.onFail(result);
            return;
        }

        try {
            if (json.has("success") && !json.getBoolean("success")) {
                throw new Exception(json.has("message") ? json.getString("message") : "Unknown response");
            }
            if (listener != null) {
                listener.onSuccess(json);
            }
        } catch (Exception ex) {
            listener.onFail(ex.toString());
        }
    }

    public boolean checkNetwork() {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
