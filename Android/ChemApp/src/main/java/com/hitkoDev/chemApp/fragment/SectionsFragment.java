/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hitkoDev.chemApp.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.hitkoDev.chemApp.ChemApp;
import com.hitkoDev.chemApp.MainActivity;
import com.hitkoDev.chemApp.R;
import com.hitkoDev.chemApp.data.Section;
import com.hitkoDev.chemApp.rest.LoadDataTask;
import com.hitkoDev.chemApp.rest.OnJSONResponseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author hitno
 */
public class SectionsFragment extends Fragment {

    private SharedPreferences settings;
    private SharedPreferences.Editor prefEditor;
    private LinearLayout.LayoutParams linearParams;
    
    public interface onSelectedListener {
        public void onSectionSelected(int section);
    }
    
    private onSelectedListener listener;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Section> sections = new ArrayList();

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            listener = (onSelectedListener) activity;
        } catch (ClassCastException e) {
            Logger.getLogger(SectionsFragment.class.getName()).log(java.util.logging.Level.SEVERE, null, e);
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sections_list, container, false);
        recyclerView = (RecyclerView) v.findViewById(R.id.sections_recycler_view);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SectionAdapter();
        
        settings = getContext().getSharedPreferences(ChemApp.PREF_NAME, 0);
        prefEditor = settings.edit();
        
        new LoadDataTask(getContext(), new OnJSONResponseListener() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    JSONArray l = response.getJSONObject("object").getJSONArray("sections");
                    for(int i = 0; i < l.length(); i++){
                        try {
                            sections.add(new Section(l.getJSONObject(i)));
                        } catch (JSONException ex) {
                            Logger.getLogger(MainActivity.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                        }
                    }
                } catch (JSONException ex) {
                    Logger.getLogger(MainActivity.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onFail(String response) {
                System.out.println(response);
            }
        }).executeCached("level", settings.getInt("level", 0)+"");
        return v;
    }
    
    private HashMap<Integer, Boolean> selected = new HashMap();
        
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView name;
        public TextView desc;
        public LinearLayout view;
        public ImageView topBorder;
        public ImageView bottomBorder;
        public CheckBox bookmark;
        public CheckBox dropdown;
        public LinearLayout subsections;
        public int id = 0;
        public int index = 0;
        public RecyclerView.Adapter adapter;
        public boolean binding = false;
        public ArrayList<ViewHolder> subsectionHolders = new ArrayList();
        
        public ViewHolder(View v, RecyclerView.Adapter a) {
            super(v);
            adapter = a;
            view = (LinearLayout) v;
            name = (TextView) v.findViewById(R.id.section_name);
            desc = (TextView) v.findViewById(R.id.section_desc);
            topBorder = (ImageView) v.findViewById(R.id.top_line);
            bottomBorder = (ImageView) v.findViewById(R.id.bottom_line);
            subsections = (LinearLayout) v.findViewById(R.id.subsections);
            
            bookmark = (CheckBox) v.findViewById(R.id.section_bookmark);
            bookmark.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(!binding){
                        prefEditor.putBoolean(ChemApp.PREF_SECTION_PR + id, isChecked);
                        prefEditor.commit();
                    }
                }
                
            });
            
            dropdown = (CheckBox) v.findViewById(R.id.section_expand);
            dropdown.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    selected.put(id, isChecked);
                    if(!binding) adapter.notifyItemChanged(index);
                }
                
            });
        }
    }
    
    class SectionAdapter extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public int getItemCount() {
            return sections.size();
        }

        @Override
        public void onBindViewHolder(ViewHolder vh, int i) {
            vh.binding = true;
            Section s = sections.get(i);
            vh.index = i;
            vh.name.setText(s.getName());
            vh.desc.setText(s.getDescription());
            vh.desc.setVisibility(s.getDescription().isEmpty() ? View.GONE : View.VISIBLE);
            vh.bottomBorder.setVisibility(i == getItemCount() - 1 ? View.GONE : View.VISIBLE);
            vh.id = s.getId();
            vh.bookmark.setChecked(settings.getBoolean(ChemApp.PREF_SECTION_PR + vh.id, false));
            
            if(s.hasChildren()) {
                vh.bookmark.setVisibility(View.GONE);
                vh.dropdown.setVisibility(View.VISIBLE);
                boolean b = selected.containsKey(s.getId()) && selected.get(s.getId());
                vh.dropdown.setChecked(b);
                if(b) {
                    for(int j = s.getChildren().size(); j < vh.subsectionHolders.size(); j++) vh.subsectionHolders.get(j).view.setVisibility(View.GONE);
                    int j = 0;
                    for(Section sub : s.getChildren()){
                        if(j < vh.subsectionHolders.size()){
                            onBindSubViewHolder(vh.subsectionHolders.get(j), sub);
                            vh.subsectionHolders.get(j).view.setVisibility(View.VISIBLE);
                        } else {
                            View v = LayoutInflater.from(vh.subsections.getContext()).inflate(R.layout.section_card, vh.subsections, false);
                            vh.subsections.addView(v);
                            ViewHolder newVh = new ViewHolder(v, this);
                            vh.subsectionHolders.add(newVh);
                            onBindSubViewHolder(newVh, sub);
                        }
                        j++;
                    }
                    vh.subsections.setVisibility(View.VISIBLE);
                } else {
                    vh.subsections.setVisibility(View.GONE);
                }
            } else {
                vh.bookmark.setVisibility(View.VISIBLE);
                vh.subsections.setVisibility(View.GONE);
                vh.dropdown.setVisibility(View.GONE);
            }
            vh.binding = false;
        }

        public void onBindSubViewHolder(ViewHolder vh, Section s) {
            vh.binding = true;
            vh.name.setText(s.getName());
            vh.desc.setText(s.getDescription());
            vh.desc.setVisibility(s.getDescription().isEmpty() ? View.GONE : View.VISIBLE);
            vh.id = s.getId();
            vh.topBorder.setVisibility(View.VISIBLE);
            vh.bookmark.setChecked(settings.getBoolean(ChemApp.PREF_SECTION_PR + vh.id, false));
            if(vh.subsections.getChildCount() > 0) vh.subsections.removeAllViews();
            vh.binding = false;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup vg, int i) {
            View v = LayoutInflater.from(vg.getContext()).inflate(R.layout.section_card, vg, false);
            
            ViewHolder vh = new ViewHolder(v, this);
            return vh;
        }
    }
    
}
