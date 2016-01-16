/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hitkoDev.chemApp.helper;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.style.ImageSpan;
import com.hitkoDev.chemApp.data.LoadedDrawable;
import java.lang.ref.WeakReference;

/**
 *
 * @author hitno
 */
public class CenteredImageSpan extends ImageSpan {

    private WeakReference<Drawable> mDrawableRef;

    public CenteredImageSpan(Drawable res) {
        super(res);
    }

    @Override
    public int getSize(Paint paint, CharSequence text,
            int start, int end,
            Paint.FontMetricsInt fm) {
        Drawable b = getCachedDrawable();
        if (b.getClass().equals(LoadedDrawable.class)) {
            LoadedDrawable d = (LoadedDrawable) b;
            d.setFontFactor(paint.getTextSize());
        }
        Rect rect = b.getBounds();

        if (fm != null) {
            Paint.FontMetricsInt pfm = paint.getFontMetricsInt();
            // keep it the same as paint's fm
            fm.ascent = pfm.ascent;
            fm.descent = pfm.descent;
            fm.top = pfm.top;
            fm.bottom = pfm.bottom;
        }

        return rect.right;
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text,
            int start, int end, float x,
            int top, int y, int bottom, @NonNull Paint paint) {
        Drawable b = getCachedDrawable();
        int btO = b.getBounds().bottom;
        if (b.getClass().equals(LoadedDrawable.class)) {
            btO = ((LoadedDrawable) b).getBottomOffset();
        }

        b.setColorFilter(new PorterDuffColorFilter(paint.getColor(), PorterDuff.Mode.SRC_IN));

        canvas.save();

        int drawableHeight = b.getIntrinsicHeight();
        int fontAscent = paint.getFontMetricsInt().ascent;
        int fontDescent = paint.getFontMetricsInt().descent;
        int transY = (bottom - btO)
                + (drawableHeight - fontDescent + fontAscent) / 2;  // align center to center

        canvas.translate(x, transY);

        float[] hsv = new float[3];
        Color.colorToHSV(paint.getColor(), hsv);
        float f = 0.75f;
        hsv[2] = hsv[2] > 0.5f ? (1f - (1f - hsv[2]) * f) : hsv[2] * f;

        b.setColorFilter(new PorterDuffColorFilter(Color.HSVToColor(Color.alpha(paint.getColor()), hsv), PorterDuff.Mode.SRC_IN));
        b.draw(canvas);

        canvas.restore();
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
