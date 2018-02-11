package com.aurora.change.model;

import android.R.integer;

public class PictureGroupInfo {

    private int id;
    private String display_name;
    private int count;
    private int system_flag;
    
    //shigq add start
    private String themeColor;
    private int isDefaultTheme = 0;
    private int isTimeBlack = 0;
    private int isStatusBarBlack = 0;
    public int downloadId = -1;
    public String downloadPkgPath="";
	public int fromThemePkg;
    
    //shigq add end

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getSystem_flag() {
        return system_flag;
    }

    public void setSystem_flag(int system_flag) {
        this.system_flag = system_flag;
    }
    
    //shigq add start
    public void setThemeColor(String colorValue) {
    	themeColor = colorValue;
    }
    
    public String getThemeColor() {
    	return themeColor;
    }
    
    public void setIsDefaultTheme(Integer value) {
    	isDefaultTheme = value;
    }
    
    public int getIsDefaultTheme() {
    	return isDefaultTheme;
    }
    
    public void setIsTimeBlack(Integer value) {
    	isTimeBlack = value;
    }
    
    public int getIsTimeBlack() {
    	return isTimeBlack;
    }
    public void setIsStatusBarBlack(Integer value) {
    	isStatusBarBlack = value;
    }
    
    public int getIsStatusBarBlack() {
    	return isStatusBarBlack;
    }
    //shigq add end

}
