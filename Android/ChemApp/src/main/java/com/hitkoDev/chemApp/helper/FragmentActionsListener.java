/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hitkoDev.chemApp.helper;

import android.app.ActivityManager;

/**
 *
 * @author hitno
 */
public interface FragmentActionsListener {

    public void setFragmentDescription(ActivityManager.TaskDescription td, String title);

    public void setFragmentDescription(String title);

    public void onSectionSelected(int section);

}
