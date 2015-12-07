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
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hitno
 */
public class IOLib {
    
    public static String md5(String s){
        try {
            MessageDigest digester = MessageDigest.getInstance("MD5");
            byte[] bytes = s.getBytes();
            digester.update(bytes, 0, bytes.length);
            return new BigInteger(1, digester.digest()).toString(16);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(LoadDataTask.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
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
