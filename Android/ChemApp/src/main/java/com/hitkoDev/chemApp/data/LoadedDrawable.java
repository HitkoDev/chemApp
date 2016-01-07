/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hitkoDev.chemApp.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LevelListDrawable;
import android.util.TypedValue;
import com.hitkoDev.chemApp.rest.LoadImageTask;
import com.hitkoDev.chemApp.rest.OnImageLoadedListener;
import java.net.URLDecoder;

/**
 *
 * @author hitno
 */
public class LoadedDrawable extends LevelListDrawable {
    
    private BitmapDrawable bmp;
    private OnDrawableUpdatedListener listener;
    private Bitmap image;
    private int bottomOffset = 0;
    
    private float factor = 1;
    
    private boolean formula = false;

    public LoadedDrawable(final Context c, String url, OnDrawableUpdatedListener l) {
        listener = l;
        try {
            url = URLDecoder.decode(url, "UTF-8").trim();
        } catch (Exception ex) {
            url = url.trim();
        }
        if(!url.isEmpty()){
            formula = url.contains("/Enačbe/");
            new LoadImageTask(c, new OnImageLoadedListener() {
                @Override
                public void onSuccess(Bitmap img) {
                    if(!formula){
                        float w = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, img.getWidth(), c.getResources().getDisplayMetrics());
                        float h = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, img.getHeight(), c.getResources().getDisplayMetrics());
                        image = Bitmap.createScaledBitmap(img, Math.round(w), Math.round(h), true);
                    } else {
                        image = img;
                    }
                    bmp = new BitmapDrawable(c.getResources(), image);
                    LoadedDrawable.this.addLevel(0, 0, bmp);
                    setMetrics(true);
                }

                @Override
                public void onFail(String response) {
                    System.out.println(response);
                }
            }).execute(url);
        }
    }

    public boolean isFormula() {
        return formula;
    }

    public void setFontFactor(float font) {
        factor = font/83f;
        if(image != null) setMetrics(false);
    }
    
    public void setFactor(float f){
        factor = f;
        if(image != null) setMetrics(true);
    }
    
    private void setMetrics(boolean notify){ 
        bottomOffset = Math.round(image.getHeight()*factor*0.93f);
        setBounds(0, 0, Math.round(image.getWidth()*factor), Math.round(image.getHeight()*factor), notify);
    }
    
    public void setBounds(int left, int top, int right, int bottom, boolean notify) {
        Rect b = getBounds();
        boolean changed = b.left != left || b.top != top || b.right != right || b.bottom != bottom;
        setBounds(left, top, right, bottom);
        if(notify && image != null && changed) listener.onDrawableUpdated();
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
    
    public int getRealWidth() {
        return image.getWidth();
    }
    
    public interface OnDrawableUpdatedListener {
        
        public void onDrawableUpdated();
        
    }
    
}
