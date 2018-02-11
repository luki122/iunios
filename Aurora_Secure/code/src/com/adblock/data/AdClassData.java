package com.adblock.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 广告类数据
 * @author chengrq
 *
 */
public class AdClassData{
     private String name;
     private String desc;
//     private String level;
     
//     public AdClassData(){
//    	 
//     }
     
     /**
      * 广告类名
      * @param name
      */
     public void setName(String name){
    	 this.name = name;
     }
     
     /**
      * 广告描述
      * @param desc
      */
     public void setDesc(String desc){
    	 this.desc = desc;
     }
     
//     /**
//      * 广告危险级别
//      * @param level
//      */
//     public void setLevel(String level){
//    	 this.level = level;
//     }
     
     /**
      * 广告类名
      * @return
      */
     public String getName(){
    	 return this.name;
     }
     
     /**
      * 广告描述
      * @return
      */
     public String getDesc(){
    	 return this.desc;
     }

    @Override
    public String toString() {
        return "AdClassData [name=" + name + ", desc=" + desc + "]";
    }
     
//     /**
//      * 广告危险级别
//      * @return
//      */
//     public String getLevel(){
//    	 return this.level;
//     }

//	@Override
//	public int describeContents() {
//		// TODO Auto-generated method stub
//		return 0;
//	}
	
//	public static final Parcelable.Creator<AdClassData> CREATOR = new Parcelable.Creator<AdClassData>(){
//        public AdClassData createFromParcel(Parcel in){
//            return new AdClassData(in);
//        }
//
//        public AdClassData[] newArray(int size){
//            return new AdClassData[size];
//        }
//    };
//    
//    public AdClassData(Parcel in){
//   	 readFromParcel(in);
//    }
//    
//    public void readFromParcel(Parcel in){
//		name = in.readString();
//		desc = in.readString();
//		level = in.readString();
//    }
//
//	@Override
//	public void writeToParcel(Parcel dest, int flags) {
//		dest.writeString(name);
//		dest.writeString(desc);
//		dest.writeString(level);			
//	}
}
