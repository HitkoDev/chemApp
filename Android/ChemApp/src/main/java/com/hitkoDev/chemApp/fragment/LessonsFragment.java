/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hitkoDev.chemApp.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.hitkoDev.chemApp.ChemApp;
import com.hitkoDev.chemApp.R;

/**
 *
 * @author hitno
 */
public class LessonsFragment extends Fragment {
    
    private SharedPreferences settings;
    private SharedPreferences.Editor prefEditor;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.lessons_content, container, false);
        
        TextView t = (TextView) v.findViewById(R.id.section_name);
        
        settings = getContext().getSharedPreferences(ChemApp.PREF_NAME, 0);
        prefEditor = settings.edit();
        
        t.setText(settings.getInt("section", 0) + "");
        
        return v;
    }
    
}
