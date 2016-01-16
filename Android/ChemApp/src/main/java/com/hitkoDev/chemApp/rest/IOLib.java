/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hitkoDev.chemApp.rest;

import com.squareup.okhttp.MediaType;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 *
 * @author hitno
 */
public class IOLib {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static String md5(String s) {
        try {
            MessageDigest digester = MessageDigest.getInstance("MD5");
            byte[] bytes = s.getBytes();
            digester.update(bytes, 0, bytes.length);
            return new BigInteger(1, digester.digest()).toString(16);
        } catch (Exception ex) {
        }
        return "";
    }

    public static String readStream(InputStream in) {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuilder out = new StringBuilder("");
        String output;
        try {
            output = br.readLine();
        } catch (Exception ex) {
            output = null;
        }
        while (output != null) {
            out.append(output).append("\n");
            try {
                output = br.readLine();
            } catch (Exception ex) {
                output = null;
            }
        }
        return out.toString();
    }

}
