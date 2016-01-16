/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hitkoDev.chemApp.rest;

import android.graphics.Bitmap;

/**
 *
 * @author hitno
 */
public interface OnImageLoadedListener {

    public void onSuccess(Bitmap image);

    public void onFail(String response);

}
