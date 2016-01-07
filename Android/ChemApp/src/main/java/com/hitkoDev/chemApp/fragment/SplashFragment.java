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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.hitkoDev.chemApp.ChemApp;
import com.hitkoDev.chemApp.R;
import com.hitkoDev.chemApp.helper.FragmentActionsListener;

/**
 *
 * @author hitno
 */
public class SplashFragment extends Fragment {
    
    private Button continueLearning;
    private Button takeExam;
    private SharedPreferences settings;
    private SharedPreferences.Editor prefEditor;
    private FragmentActionsListener listener;
    
    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle bundle) {
        
        settings = getContext().getSharedPreferences(ChemApp.PREF_NAME, 0);
        prefEditor = settings.edit();
        
        View v = li.inflate(R.layout.layout_splash, vg, false);
        continueLearning = (Button) v.findViewById(R.id.button_continue_learning);
        takeExam = (Button) v.findViewById(R.id.button_take_exam);
        
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof FragmentActionsListener) listener = (FragmentActionsListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        continueLearning.setEnabled(settings.getInt("section", 0) > 0);
        takeExam.setEnabled(settings.getInt("level", 0) > 0);
        if(listener != null) listener.setFragmentDescription(getContext().getString(R.string.app_name));
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle); //To change body of generated methods, choose Tools | Templates.
        setRetainInstance(true);
    }
    
}
