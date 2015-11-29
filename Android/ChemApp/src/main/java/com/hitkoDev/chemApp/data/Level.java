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
public class Level {
    
    private int id;
    private String name;
    private String desc;
    private int order;
    private ArrayList<Section> sections;
    
    public Level(JSONObject o) throws JSONException{
        if(o.has("id")) id = o.getInt("id");
        if(o.has("name")) name = o.getString("name");
        if(o.has("description")) desc = o.getString("description");
        if(o.has("order")) id = o.getInt("order");
        if(o.has("sections")){
            JSONArray sec = o.getJSONArray("sections");
            sections = new ArrayList();
            for(int i = 0; i < sec.length(); i++){
                sections.add(new Section(sec.getJSONObject(i)));
            }
        }
        System.out.println(name);
    }
    
}
