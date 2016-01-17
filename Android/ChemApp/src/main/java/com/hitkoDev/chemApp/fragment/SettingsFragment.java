/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hitkoDev.chemApp.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceFragmentCompat;
import com.hitkoDev.chemApp.ChemApp;
import com.hitkoDev.chemApp.R;
import com.hitkoDev.chemApp.data.Level;
import com.hitkoDev.chemApp.helper.FragmentActionsListener;
import java.util.ArrayList;

/**
 *
 * @author hitno
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    private PreferenceAttachedListener preferenceListener;
    protected FragmentActionsListener listener;

    @Override
    public void onCreatePreferences(Bundle bundle, String string) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentActionsListener) {
            listener = (FragmentActionsListener) context;
        }
        if (context instanceof PreferenceAttachedListener) {
            preferenceListener = (PreferenceAttachedListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
        preferenceListener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //To change body of generated methods, choose Tools | Templates.
        getPreferenceManager().setSharedPreferencesName(ChemApp.PREF_NAME);
        getPreferenceManager().setSharedPreferencesMode(0);
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onStart() {
        super.onStart();

        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            final Preference p = getPreferenceScreen().getPreference(i);
            if (preferenceListener != null) {
                if (p.getKey().equals("level")) {
                    ListPreference lp = (ListPreference) p;
                    ArrayList<Level> lvl = preferenceListener.getLevels();
                    String[] names = new String[lvl.size()];
                    String[] values = new String[lvl.size()];
                    for (int j = 0; j < lvl.size(); j++) {
                        names[j] = lvl.get(j).getName();
                        values[j] = lvl.get(j).getId() + "";
                    }
                    lp.setEntries(names);
                    lp.setEntryValues(values);
                } else if (p.getKey().equals("reset")) {
                    p.setSummary("Opravljenio: " + preferenceListener.getProgress() + " poglavij");
                    p.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference prfrnc) {
                            if(preferenceListener == null) return false;
                            
                            new AlertDialog.Builder(getContext())
                                    .setMessage("Ali res želiš ponastaviti napredek?")
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            p.setSummary("Opravljenio: 0 poglavij");
                                            if(preferenceListener != null) preferenceListener.resetProgress();
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .show();
                            
                            return true;
                        }
                    });
                }
            }
        }
        if (listener != null) {
            listener.setFragmentDescription(getContext().getString(R.string.settings));
        }
    }

    public interface PreferenceAttachedListener {

        public ArrayList<Level> getLevels();
        
        public int getProgress();

        public void resetProgress();

    }

}
