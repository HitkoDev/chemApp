/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hitkoDev.chemApp.helper;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.style.ImageSpan;
import com.hitkoDev.chemApp.data.LoadedDrawable;
import java.lang.ref.WeakReference;

/**
 *
 * @author hitno
 */
public class WidthImageSpan extends ImageSpan {

    private WeakReference<Drawable> mDrawableRef;

    public WidthImageSpan(Drawable res) {
        super(res);
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text,
            int start, int end, float x,
            int top, int y, int bottom, @NonNull Paint paint) {
        Drawable b = getCachedDrawable();
        if(b.getClass().equals(LoadedDrawable.class)){
            LoadedDrawable d = (LoadedDrawable)b;
            d.setFactor(d.getRealWidth() > canvas.getWidth() ? ((float)canvas.getWidth())/((float)d.getRealWidth()) : 1f);
        }
        super.draw(canvas, text, start, end, x, top, y, bottom, paint);
    }

    // Redefined locally because it is a private member from DynamicDrawableSpan
    private Drawable getCachedDrawable() {
        WeakReference<Drawable> wr = mDrawableRef;
        Drawable d = null;

        if (wr != null) {
            d = wr.get();
        }

        if (d == null) {
            d = getDrawable();
            mDrawableRef = new WeakReference<>(d);
        }

        return d;
    }
}
