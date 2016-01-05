/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hitkoDev.chemApp.data;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import com.hitkoDev.chemApp.helper.CenteredImageSpan;
import com.hitkoDev.chemApp.rest.TextImageGetter;
import org.json.JSONException;
import org.json.JSONObject;
import com.hitkoDev.chemApp.data.LoadedDrawable.OnDrawableUpdatedListener;

/**
 *
 * @author hitno
 */
public class Lesson {
    
    private int id;
    private String name;
    private String content;
    private String classKey;
    private Spanned nameParsed;
    private Spanned contentParsed;
    private Drawable imageContent;
    
    public Lesson(JSONObject o) throws JSONException {
        if(o.has("id")) id = o.getInt("id");
        if(o.has("name")) name = o.getString("name");
        if(o.has("content")) content = o.getString("content");
        if(o.has("class_key")) classKey = o.getString("class_key");
        if(name != null) nameParsed = centerImages(Html.fromHtml(name));
        if(content != null) contentParsed = centerImages(Html.fromHtml(content));
    }
    
    public Lesson(JSONObject o, Context c, OnDrawableUpdatedListener l) throws JSONException {
        this(o);
        TextImageGetter g = new TextImageGetter(c, l);
        if(name != null) nameParsed = centerImages(Html.fromHtml(name, g, null));
        if(content != null) contentParsed = centerImages(Html.fromHtml(content, g, null));
        if(classKey != null && classKey.equals("caImage")) imageContent = g.getDrawable(content);
    }
    
    private Spanned centerImages(Spanned sp){
        SpannableStringBuilder b = new SpannableStringBuilder(sp);
        ImageSpan[] img = b.getSpans(0, sp.length(), ImageSpan.class);
        int firstImg = b.length() - 1;
        int lastImg = 0;
        for(ImageSpan i : img) {
            int s = b.getSpanStart(i);
            int e = b.getSpanEnd(i);
            if(s < firstImg) firstImg = s;
            if(e > lastImg) lastImg = e;
            if(i.getDrawable().getClass() == LoadedDrawable.class && ((LoadedDrawable)i.getDrawable()).isFormula()){
                int f = b.getSpanFlags(i);
                b.removeSpan(i);
                b.setSpan(new CenteredImageSpan(i.getDrawable()), s, e, f);
            }
        }
        if(b.length() > 0){
            int first = 0;
            int last = b.length() - 1;
            while(Character.isWhitespace(b.charAt(first)) && first < last && first < firstImg) first++;
            while(Character.isWhitespace(b.charAt(last)) && last > first && last > lastImg) last--;
            return new SpannableStringBuilder(b.subSequence(first, last + 1));
        } else {
            return b;
        }
    }

    public Drawable getImageContent() {
        return imageContent;
    }
    
    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public String getClassKey() {
        return classKey;
    }

    public Spanned getContentParsed() {
        return contentParsed;
    }

    public Spanned getNameParsed() {
        return nameParsed;
    }
    
}
