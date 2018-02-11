//Gionee <jianghuan> <2013-09-29> add for CR00975553 begin
package com.aurora.puremanager.traffic;

import android.graphics.drawable.Drawable;

public class NetworkAppInfo {

    private int mAppUid = 0;
    private String mAppName = "";
    private String mAppPackageName = "";
    private Drawable mAppIcon = null;
    private int mAppMobileStatus = 0;
    private int mAppWifiStatus = 0;

    public void setAppUid(int uid) {
        this.mAppUid = uid;
    }

    public void setAppName(String name) {
        this.mAppName = name;
    }

    public void setAppPackageName(String name) {
        this.mAppPackageName = name;
    }

    public void setAppIcon(Drawable icon) {
        this.mAppIcon = icon;
    }

    public void setAppMobileStatus(int status) {
        this.mAppMobileStatus = status;
    }

    public void setAppWifiStatus(int status){
    	this.mAppWifiStatus = status;
    }
    
    public int getAppUid() {
        return this.mAppUid;
    }

    public String getAppName() {
        return this.mAppName;
    }

    public String getAppPackageName() {
        return this.mAppPackageName;
    }

    public Drawable getAppIcon() {
        return this.mAppIcon;
    }

    public int getAppMobileStatus() {
        return this.mAppMobileStatus;
    }
    
    public int getAppWifiStatus(){
    	return this.mAppWifiStatus;
    }
}
// Gionee <jianghuan> <2013-09-29> add for CR00975553 end