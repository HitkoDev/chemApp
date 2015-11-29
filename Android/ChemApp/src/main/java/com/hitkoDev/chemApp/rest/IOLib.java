/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hitkoDev.chemApp.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hitno
 */
public class IOLib {
    
    public static String readStream(InputStream in){
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuilder out = new StringBuilder("");
        String output;
        try {
            output = br.readLine();
        } catch (IOException ex) {
            Logger.getLogger(IOLib.class.getName()).log(Level.SEVERE, null, ex);
            output = null;
        }
        while(output != null) {
            out.append(output).append("\n");
            try {
                output = br.readLine();
            } catch (IOException ex) {
                Logger.getLogger(IOLib.class.getName()).log(Level.SEVERE, null, ex);
                output = null;
            }
        }
        return out.toString();
    }
    
}
