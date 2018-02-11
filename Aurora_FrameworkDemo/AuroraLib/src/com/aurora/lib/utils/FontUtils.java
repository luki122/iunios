package com.aurora.lib.utils;

import android.graphics.Typeface;

import aurora.lib.widget.AuroraUtil;

public class FontUtils {

    
    public static Typeface auroraCreateTitleFont(String url) {
        Typeface  auroraTitleFace = null;
        try {
            
             auroraTitleFace = Typeface.createFromFile(url);
        } catch (Exception e) {
            // TODO: handle exception
            e.getCause();
            e.printStackTrace();
            auroraTitleFace = null;
        }
        
        return auroraTitleFace;
    }
    
}
