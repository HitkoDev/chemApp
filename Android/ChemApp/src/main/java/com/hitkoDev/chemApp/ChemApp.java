/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hitkoDev.chemApp;

import android.app.Application;
import com.squareup.okhttp.OkHttpClient;

/**
 *
 * @author hitno
 */
public class ChemApp extends Application {
    
    public static final OkHttpClient client = new OkHttpClient();
    public static final String PREF_NAME = "ChemAppPreferences";
    
}
