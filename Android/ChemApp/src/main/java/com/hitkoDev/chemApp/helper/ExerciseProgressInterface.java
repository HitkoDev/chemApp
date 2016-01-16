/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hitkoDev.chemApp.helper;

/**
 *
 * @author hitno
 */
public interface ExerciseProgressInterface {

    public void setUnlocked(int sectionId);

    public boolean isUnlocked(int sectionId);

    public int[] getUnlocked();

}
