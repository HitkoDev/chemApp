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
import android.widget.TextView;
import com.hitkoDev.chemApp.ChemApp;
import com.hitkoDev.chemApp.MainActivity;
import com.hitkoDev.chemApp.R;
import com.hitkoDev.chemApp.data.Section;
import com.hitkoDev.chemApp.rest.LoadDataTask;
import com.hitkoDev.chemApp.rest.OnJSONResponseListener;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author hitno
 */
public class SectionsFragment extends Fragment {
    
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
        
        SharedPreferences settings = getContext().getSharedPreferences(ChemApp.PREF_NAME, 0);
        
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
        
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public ViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }
    }
    
    class SectionAdapter extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public int getItemCount() {
            return sections.size();
        }

        @Override
        public void onBindViewHolder(ViewHolder vh, int i) {
            vh.mTextView.setText(sections.get(i).getName());
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup vg, int i) {
            TextView v = (TextView) LayoutInflater.from(vg.getContext()).inflate(R.layout.section_card, vg, false);
            
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }
    }
    
}
