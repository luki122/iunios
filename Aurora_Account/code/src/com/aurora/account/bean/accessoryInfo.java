package com.aurora.account.bean;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

@SuppressLint("ParcelCreator")
public class accessoryInfo implements Parcelable {
	// 客户端附件路径
	private String path;
	// 客户端附件新路径
	private String new_path;
	// 模块附件类型
	private String type;
	// 附件id
	private String accessoryid;
	// 服务器同步id
	private String syncid;

	private String date;
	
	private String accessoryurl;

	public accessoryInfo()
	{
		
	}
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAccessoryid() {
		return accessoryid;
	}

	public void setAccessoryid(String accessoryid) {
		this.accessoryid = accessoryid;
	}

	public String getSyncid() {
		return syncid;
	}

	public void setSyncid(String syncid) {
		this.syncid = syncid;
	}

	public String getNew_path() {
		return new_path;
	}

	public void setNew_path(String new_path) {
		this.new_path = new_path;
	}

	public String getAccessoryurl() {
		return accessoryurl;
	}
	
	public void setAccessoryurl(String accessoryurl) {
		this.accessoryurl = accessoryurl;
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(path);
		dest.writeString(new_path);
		dest.writeString(type);
		dest.writeString(accessoryid);
		dest.writeString(syncid);
		dest.writeString(date);
		dest.writeString(accessoryurl);
	}

	public static final Parcelable.Creator<accessoryInfo> CREATOR = new Creator<accessoryInfo>() {
		@Override
		public accessoryInfo createFromParcel(Parcel source) {
			return new accessoryInfo(source);
		}

		@Override
		public accessoryInfo[] newArray(int size) {
			return new accessoryInfo[size];
		}
	};

	@SuppressWarnings("unchecked")
	public accessoryInfo(Parcel in) {
		path = in.readString();
		new_path = in.readString();
		type = in.readString();
		accessoryid = in.readString();
		syncid = in.readString();
		date = in.readString();
		accessoryurl = in.readString();
	}
}
