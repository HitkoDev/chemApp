/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hitkoDev.chemApp.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.hitkoDev.chemApp.R;
import com.hitkoDev.chemApp.helper.FragmentActionsListener;

/**
 *
 * @author hitno
 */
public class HelpFragment extends Fragment {

    private FragmentActionsListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentActionsListener) {
            listener = (FragmentActionsListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle bundle) {
        if (listener != null) {
            listener.setFragmentDescription(getContext().getString(R.string.app_name));
        }
        return li.inflate(R.layout.layout_help, vg, false);
    }

}
