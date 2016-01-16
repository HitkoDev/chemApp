/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hitkoDev.chemApp.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import com.hitkoDev.chemApp.rest.LoadImageTask;
import com.hitkoDev.chemApp.rest.OnImageLoadedListener;
import java.net.URLDecoder;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author hitno
 */
public class Section {

    public interface OnIconLoaded {

        public void notifyLoaded(Section s);
    }

    private int id;
    private String name;
    private String desc;
    private Section parent;
    private ArrayList<Section> children;
    private Drawable tile;
    private Bitmap icon;
    private OnIconLoaded listener;
    private String iconURL;

    public Section(JSONObject o) throws JSONException {
        if (o.has("id")) {
            id = o.getInt("id");
        }
        if (o.has("name")) {
            name = o.getString("name");
        }
        if (o.has("description")) {
            desc = o.getString("description");
        }
        if (o.has("icon")) {
            try {
                iconURL = URLDecoder.decode(o.getString("icon"), "UTF-8").trim();
            } catch (Exception ex) {

            }
        }
        if (o.has("sections")) {
            JSONArray sec = o.getJSONArray("sections");
            children = new ArrayList();
            for (int i = 0; i < sec.length(); i++) {
                children.add(new Section(sec.getJSONObject(i), this));
            }
        }
    }

    public Section(JSONObject o, Context c, OnIconLoaded l) throws JSONException {
        this(o);
        listener = l;
        if (o.has("sections")) {
            JSONArray sec = o.getJSONArray("sections");
            children = new ArrayList();
            for (int i = 0; i < sec.length(); i++) {
                children.add(new Section(sec.getJSONObject(i), this, c, l));
            }
        }
        if (hasIcon()) {
            new LoadImageTask(c, new OnImageLoadedListener() {
                @Override
                public void onSuccess(Bitmap image) {
                    icon = image;
                    if (listener != null) {
                        listener.notifyLoaded(Section.this);
                    }
                }

                @Override
                public void onFail(String response) {
                    System.out.println(response);
                }
            }).execute(iconURL);
        }
    }

    public final boolean hasIcon() {
        return iconURL != null && !iconURL.isEmpty();
    }

    public Section(JSONObject o, Section p) throws JSONException {
        this(o);
        parent = p;
    }

    public Section(JSONObject o, Section p, Context c, OnIconLoaded l) throws JSONException {
        this(o, c, l);
        parent = p;
    }

    public boolean loadedIcon() {
        return icon != null;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public int getChildrenCount() {
        return children == null ? 0 : children.size();
    }

    public boolean isChild() {
        return this.parent != null;
    }

    public void setTile(Drawable tile) {
        this.tile = tile;
    }

    public Drawable getTile() {
        return tile;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return desc;
    }

    public Section getParent() {
        return parent;
    }

    public ArrayList<Section> getChildren() {
        return children;
    }

    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

}
