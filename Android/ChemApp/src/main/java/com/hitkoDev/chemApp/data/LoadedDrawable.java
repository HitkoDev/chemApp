/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hitkoDev.chemApp.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LevelListDrawable;
import com.hitkoDev.chemApp.rest.LoadImageTask;
import com.hitkoDev.chemApp.rest.OnImageLoadedListener;

/**
 *
 * @author hitno
 */
public class LoadedDrawable extends LevelListDrawable {
    
    private BitmapDrawable bmp;
    private OnDrawableLoadedListener listener;
    private Bitmap image;
    private int bottomOffset = 0;
    
    private float factor = 1;

    public LoadedDrawable(final Context c, String url, OnDrawableLoadedListener l) {
        listener = l;
        url = url.trim();
        if(!url.isEmpty()){
            new LoadImageTask(c, new OnImageLoadedListener() {
                @Override
                public void onSuccess(Bitmap img) {
                    image = img;
                    System.out.println(image);
                    bmp = new BitmapDrawable(c.getResources(), image);
                    LoadedDrawable.this.addLevel(0, 0, bmp);
                    setMetrics();
                    listener.onDrawableLoaded();
                }

                @Override
                public void onFail(String response) {
                    System.out.println(response);
                }
            }).execute(url);
        }
    }

    public void setFactor(float font) {
        this.factor = font/83f;
        if(image != null) setMetrics();
    }
    
    private void setMetrics(){
        setBounds(0, 0, Math.round(image.getWidth()*factor), Math.round(image.getHeight()*factor)); 
        bottomOffset = Math.round(image.getHeight()*factor*0.93f);
    }

    @Override
    public int getIntrinsicHeight() {
        return this.getBounds().bottom;
    }

    @Override
    public int getIntrinsicWidth() {
        return this.getBounds().right;
    }

    public int getBottomOffset() {
        return bottomOffset;
    }
    
    public interface OnDrawableLoadedListener {
        
        public void onDrawableLoaded();
        
    }
    
}
