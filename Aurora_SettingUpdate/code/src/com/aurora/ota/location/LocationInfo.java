package com.aurora.ota.location;

import android.text.TextUtils;

public class LocationInfo {
    
    private String mTime;
    private String mErrorCode;
    private String mLatitude;
    private String mLongtitude;
    private String mRadius;
    
    private String mAddr;

    private String mCoutry;
    
    private String mProvince;
    
    private String mCity;
    
    private String ip;
    
    public String getIp() {
        return ip;
    }
    public void setIp(String ip) {
        this.ip = ip;
    }
    public void setCountry(String coutry){
        this.mCoutry = coutry;
    }
    public String getCoutry(){
        return mCoutry;
    }
    
    public void setProvince(String Province){
        this.mProvince = Province;
    }
    public String getProvince(){
        return mProvince;
    }
    
    public void setCity(String City){
        this.mCity = City;
    }
    public String getCity(){
        return mCity;
    }
    
    public String getTime() {
        return mTime;
    }

    public void setTime(String mTime) {
        this.mTime = mTime;
    }

    public String getErrorCode() {
        return mErrorCode;
    }

    public void setErrorCode(String mNerrorCode) {
        this.mErrorCode = mNerrorCode;
    }

    public String getLatitude() {
        return mLatitude;
    }

    public void setLatitude(String mNlatitude) {
        this.mLatitude = mNlatitude;
    }

    public String getLongtitude() {
        return mLongtitude;
    }

    public void setLongtitude(String mNLongtitude) {
        this.mLongtitude = mNLongtitude;
    }

    public String getRadius() {
        return mRadius;
    }

    public void setRadius(String mNradius) {
        this.mRadius = mNradius;
    }

    public String getAddr() {
        return mAddr;
    }

    public void setAddr(String mNaddr) {
        this.mAddr = mNaddr;
    }
    
    @Override
    public String toString() {
       if(mProvince != null && mCity != null&&mCoutry!=null){
           return "country:"+mCoutry.toString()+"provice:"+mProvince.toString()+" City:"+mCity.toString()+"";
       }else{
           return "";
       }
        
        
    }


    public boolean equal(LocationInfo obj) {
        // TODO Auto-generated method stub
        if(obj == null){
            return false;
        }
        
        return (obj.getProvince().equals(mProvince) && obj.getCity().equals(mCity));
    }
    
    
}
