/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hitkoDev.chemApp.tiles;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.TextUtils;
import com.hitkoDev.chemApp.R;
import com.hitkoDev.chemApp.tiles.ImageCanvas.Dimensions;
/**
 * LetterTileProvider is an implementation of the DefaultImageProvider. When no
 * matching contact photo is found, and there is a supplied displayName or email
 * address whose first letter corresponds to an English alphabet letter (or
 * number), this method creates a bitmap with the letter in the center of a
 * tile. If there is no English alphabet character (or digit), it creates a
 * bitmap with the default contact avatar.
 */
public class LetterTileProvider {
    private final Drawable mDefaultDrawable;
    private final Bitmap[] mBitmapBackgroundCache;
    private final Typeface mSansSerifLight;
    private final Rect mBounds;
    private final int mTileLetterFontSize;
    private final int mTileLetterFontSizeSmall;
    private final int mTileFontColor;
    private final TextPaint mPaint = new TextPaint();
    private final Canvas mCanvas = new Canvas();
    private final char[] mFirstChar = new char[1];
    private static final int POSSIBLE_BITMAP_SIZES = 3;
    private final ColorPicker mTileColorPicker;
    public boolean noCache = false;
    public LetterTileProvider(Resources res) {
        this(res, new ColorPicker.PaletteColorPicker(res));
    }
    public LetterTileProvider(Resources res, ColorPicker colorPicker) {
        mTileLetterFontSize = res.getDimensionPixelSize(R.dimen.tile_letter_font_size_medium);
        mTileLetterFontSizeSmall = res.getDimensionPixelSize(R.dimen.tile_letter_font_size_small);
        mTileFontColor = res.getColor(R.color.letter_tile_font_color);
        mSansSerifLight = Typeface.create("sans-serif-light", Typeface.NORMAL);
        mBounds = new Rect();
        mPaint.setTypeface(mSansSerifLight);
        mPaint.setColor(mTileFontColor);
        mPaint.setTextAlign(Align.CENTER);
        mPaint.setAntiAlias(true);
        mBitmapBackgroundCache = new Bitmap[POSSIBLE_BITMAP_SIZES];
        mDefaultDrawable = res.getDrawable(R.drawable.ic_tile_generic);
        mTileColorPicker = colorPicker;
    }
    public Bitmap makeCircle(final Dimensions dimensions, Bitmap input) {
        final Bitmap bitmap = getBitmap(dimensions);
        if (bitmap == null) {
            return null;
        }
        final Canvas c = mCanvas;
        c.setBitmap(bitmap);

        final Paint p = new Paint();
        final Rect dst = new Rect(0, 0, dimensions.width, dimensions.height);
        
        double kw = ((double)input.getWidth()) / ((double)dimensions.width);
        double kh = ((double)input.getHeight()) / ((double)dimensions.height);
        
        double nw = dimensions.width * Math.min(kw, kh);
        double nh = dimensions.height * Math.min(kw, kh);
        int l = (int) Math.round((input.getWidth() - nw)/2.0);
        int t = (int) Math.round((input.getHeight() - nh)/2.0);
        int r = (int) Math.round(nw + (input.getWidth() - nw)/2.0);
        int b = (int) Math.round(nh + (input.getHeight() - nh)/2.0);
        final Rect src = new Rect(l, t, r, b);

        p.setAntiAlias(true);
        c.drawARGB(0, 0, 0, 0);
        c.drawOval(0, 0, dimensions.width, dimensions.height, p);
        p.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        c.drawBitmap(input, src, dst, p);
        return bitmap;
    }
    public Bitmap getLetterTile(final Dimensions dimensions, final String displayName,
            final String address) {
        final String display = !TextUtils.isEmpty(displayName) ? displayName : address;
        final char firstChar = display.charAt(0);
        // get an empty bitmap
        final Bitmap bitmap = getBitmap(dimensions);
        if (bitmap == null) {
            return null;
        }
        final Canvas c = mCanvas;
        c.setBitmap(bitmap);
        Paint p = new Paint();
        p.setColor(mTileColorPicker.pickColor(address));
        p.setAntiAlias(true);
        c.drawOval(0, 0, dimensions.width, dimensions.height, p);
        // If its a valid English alphabet letter,
        // draw the letter on top of the color
        if (isEnglishLetterOrDigit(firstChar)) {
            mFirstChar[0] = Character.toUpperCase(firstChar);
            mPaint.setTextSize(
                    dimensions.fontSize > 0 ? dimensions.fontSize : getFontSize(dimensions.scale));
            mPaint.getTextBounds(mFirstChar, 0, 1, mBounds);
            c.drawText(mFirstChar, 0, 1, 0 + dimensions.width / 2,
                    0 + dimensions.height / 2 + (mBounds.bottom - mBounds.top) / 2, mPaint);
        } else { // draw the generic icon on top
            mDefaultDrawable.setBounds(0, 0, dimensions.width, dimensions.height);
            mDefaultDrawable.draw(c);
        }
        return bitmap;
    }
    private static boolean isEnglishLetterOrDigit(char c) {
        return ('A' <= c && c <= 'Z')
                || ('a' <= c && c <= 'z')
                || ('0' <= c && c <= '9');
    }
    private Bitmap getBitmap(final Dimensions d) {
        if (d.width <= 0 || d.height <= 0) {
            return null;
        }
        if(noCache){
            return Bitmap.createBitmap(d.width, d.height, Bitmap.Config.ARGB_8888);
        }
        final int pos;
        float scale = d.scale;
        if (scale == Dimensions.SCALE_ONE) {
            pos = 0;
        } else if (scale == Dimensions.SCALE_HALF) {
            pos = 1;
        } else {
            pos = 2;
        }
        Bitmap bitmap = mBitmapBackgroundCache[pos];
        // ensure bitmap is suitable for the desired w/h
        // (two-pane uses two different sets of dimensions depending on pane width)
        if (bitmap == null || bitmap.getWidth() != d.width || bitmap.getHeight() != d.height) {
            // create and place the bitmap
            bitmap = Bitmap.createBitmap(d.width, d.height, Bitmap.Config.ARGB_8888);
            mBitmapBackgroundCache[pos] = bitmap;
        }
        return bitmap;
    }
    private int getFontSize(float scale)  {
        if (scale == Dimensions.SCALE_ONE) {
            return mTileLetterFontSize;
        } else {
            return mTileLetterFontSizeSmall;
        }
    }
}