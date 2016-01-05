/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hitkoDev.chemApp.rest;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html.ImageGetter;
import com.hitkoDev.chemApp.data.LoadedDrawable;
import com.hitkoDev.chemApp.data.LoadedDrawable.OnDrawableLoadedListener;

/**
 *
 * @author hitno
 */
public class TextImageGetter implements ImageGetter {
    
    private final Context context;
    private final OnDrawableLoadedListener listener;
    
    public TextImageGetter(Context c, OnDrawableLoadedListener l) {
        context = c;
        listener = l;
    }

    @Override
    public Drawable getDrawable(String source) {
        return new LoadedDrawable(context, source, listener);
    }
    
}
