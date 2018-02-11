package com.netmanage.data;


import android.os.Parcel;
import android.os.Parcelable;

import com.alibaba.fastjson.JSONObject;

/**
 * 流量数据
 * @author chengrq
 *
 */
public class FlowData implements Parcelable{
	 private String packageName;
	 private long totalBytes;
	 private long backBytes;
	 
	 public FlowData() {
//		super("FlowData");
	 }
	 
	 public void setPackageName(String packageName){
		 this.packageName = packageName;
	 }
	 
	 public String getPackageName(){
		 return this.packageName;
	 }
	 
	 public void setTotalBytes(long totalBytes){
		 this.totalBytes = totalBytes;
	 }
	 
	 public long getTotalBytes(){
		 return this.totalBytes;
	 }
	 
	 public void setBackBytes(long backBytes){
		 this.backBytes = backBytes;
	 }
	 
	 public long getBackBytes(){
		 return this.backBytes;
	 }
	 
	 public JSONObject getJson() {				
		JSONObject json = new JSONObject();	
		json.put("packageName", packageName);
		json.put("totalBytes", totalBytes);
		json.put("backBytes", backBytes);
		return json;
	 }
		
	 /**
	  * 解析json对象
	  * @param json
	  * @return true 解析成功  false 解析失败
	  */
	public boolean parseJson(JSONObject json) throws Exception{	
		boolean result = false;
		  if (json != null && !json.isEmpty()) {	
			  packageName = json.getString("packageName");   
			  totalBytes = json.getLong("totalBytes");
			  backBytes = json.getLong("backBytes");
			  result = true;			 			 
		  }		  
		  return result;
	}
	  
	public static final Parcelable.Creator<FlowData> CREATOR = new Parcelable.Creator<FlowData>(){
        public FlowData createFromParcel(Parcel in){
            return new FlowData(in);
        }

        public FlowData[] newArray(int size){
            return new FlowData[size];
        }
    };
    
    private FlowData(Parcel in){
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in){
    	packageName = in.readString();
    	totalBytes = in.readLong();
    	backBytes = in.readLong();
    }

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(packageName);
		dest.writeLong(totalBytes);
		dest.writeLong(backBytes);
	}
}
