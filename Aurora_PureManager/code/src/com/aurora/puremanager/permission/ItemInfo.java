package com.aurora.puremanager.permission;

import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;

/**
 * 
 File Description:
 * 
 * @author: Gionee-lihq
 * @see: 2013-4-22 Change List:
 */

public class ItemInfo {
    protected String mName;
    protected String mSummary;
    //Gionee <jianghuan> <2014-04-17> modify for CR01200613 begin
    protected String mPackageName;
    //Gionee <jianghuan> <2014-04-17> modify for CR01200613 end
    protected Drawable mIcon = null;
    // Gionee <jingjc> <2013-9-17> modify for CR00904574 begin
    protected int status;
    // Gionee <jingjc> <2013-9-17> modify for CR00904574 end
    protected int mImportantStatus ;
    protected int mUseCounts ;
    
    protected String mSize ;
    protected long mFrequency ;
    protected boolean mCheckStatus;
    public ApplicationInfo mApplicationInfo;

    public ItemInfo(){}
    
    public ItemInfo(ApplicationInfo info) {
        this.mApplicationInfo = info;
    }
    //Gionee <jianghuan> <2014-04-17> modify for CR01200613 begin
    public void setPackageName(String packagename){
    	mPackageName = packagename;
    }
    
    public String getPackageName(){
    	return mPackageName;
    }
    //Gionee <jianghuan> <2014-04-17> modify for CR01200613 begin
    public void setName(String name) {
//        this.mName = new String(name);
        this.mName = name;
    }

    public void setSummary(String summary) {
//        this.mSummary = new String(summary);
        this.mSummary = summary;
    }
    
    //Gionee <jianghuan> <2014-04-17> delete for CR01200613 begin
    public void setIcon(Drawable drawable) {
        this.mIcon = drawable;
    }
    //Gionee <jianghuan> <2014-04-17> delete for CR01200613 end
    
    // Gionee <jingjc> <2013-9-17> modify for CR00904574 begin
    public void setStauts(int status) {
    	this.status = status;
    }
    // Gionee <jingjc> <2013-9-17> modify for CR00904574 end
    
    public void setImportantStatus(int status) {
        this.mImportantStatus = status ;
    }
    
    public void setCheckStatus(boolean status){
        this.mCheckStatus = status;
    }
    
    public void setUseCounts(int count){
        this.mUseCounts = count ;
    }
    
    public void setSize(String size){
//        this.mSize = new String(size) ;
        this.mSize = size;
    }
    
    public void setAppFrequency(long frequency){
       this.mFrequency = frequency ; 
    }

    public String getName() {
        return this.mName;
    }

    public String getSummary() {
        return this.mSummary;
    }
    
    
  //Gionee <jianghuan> <2014-04-17> delete for CR01200613 begin
    public Drawable getIcon() {
        return this.mIcon;
    }
  //Gionee <jianghuan> <2014-04-17> delete for CR01200613 end
    
    // Gionee <jingjc> <2013-9-17> modify for CR00904574 begin
    public int getStatus() {
    	return this.status;
    }
    // Gionee <jingjc> <2013-9-17> modify for CR00904574 end
    
    public int getImportantStatus(){
        return this.mImportantStatus ;
    }
    
    public boolean getCheckStaus(){
        return this.mCheckStatus;
    }
    
    public int getUseCount() {
        return this.mUseCounts ;
    }
    
    public String getSize() {
        return this.mSize; 
     }
    
    public long getAppFrequency(){
        return this.mFrequency ;
    }
}
