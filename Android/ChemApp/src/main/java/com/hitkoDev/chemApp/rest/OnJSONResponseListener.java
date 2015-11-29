/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hitkoDev.chemApp.rest;

import org.json.JSONObject;

/**
 *
 * @author hitno
 */
public interface OnJSONResponseListener {
    
    public void onSuccess(JSONObject response);
    public void onFail(String response);
    
}
