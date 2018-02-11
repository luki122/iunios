package com.adblock.data;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 用于广告数据的aidl传输
 * @author chengrq
 */
public class AidlAdData implements Parcelable{
    private String pkgName;
    private ArrayList<String> adClassLis;
    
    public AidlAdData(){
    	
    }
    
    public void setPkgName(String pkgName){
    	this.pkgName = pkgName;
    }
    
    public void setAdClassLis(ArrayList<String> adClassLis){
    	this.adClassLis = adClassLis;
    }
    
    public String getPkgName(){
    	return this.pkgName;
    }
    
    public ArrayList<String> getAdClassLis(){
    	return this.adClassLis;
    }
    
    
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public static final Parcelable.Creator<AidlAdData> CREATOR = 
			new Parcelable.Creator<AidlAdData>(){
        public AidlAdData createFromParcel(Parcel in){
            return new AidlAdData(in);
        }

        public AidlAdData[] newArray(int size){
            return new AidlAdData[size];
        }
    };
    
    public AidlAdData(Parcel in){
      	 readFromParcel(in);
       }

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(pkgName);
		dest.writeList(adClassLis);	
	}
	
	public void readFromParcel(Parcel in){
		pkgName = in.readString();
		adClassLis = in.readArrayList(ArrayList.class.getClassLoader());
     }
}
