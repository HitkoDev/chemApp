/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hitkoDev.chemApp.data;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author hitno
 */
public class Section {
    
    private int id;
    private String name;
    private String desc;
    private Section parent;
    private ArrayList<Section> children;
    
    public Section(JSONObject o) throws JSONException {
        if(o.has("id")) id = o.getInt("id");
        if(o.has("name")) name = o.getString("name");
        if(o.has("description")) desc = o.getString("description");
        if(o.has("sections")){
            JSONArray sec = o.getJSONArray("sections");
            children = new ArrayList();
            for(int i = 0; i < sec.length(); i++){
                children.add(new Section(sec.getJSONObject(i), this));
            }
        }
    }
    
    public Section(JSONObject o, Section p) throws JSONException {
        this(o);
        parent = p;
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
