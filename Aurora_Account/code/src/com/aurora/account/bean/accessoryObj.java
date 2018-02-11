package com.aurora.account.bean;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

@SuppressLint("ParcelCreator")
public class accessoryObj implements Parcelable {
	// 附件数组
	private ArrayList<accessoryInfo> accessory = new ArrayList<accessoryInfo>();

	public ArrayList<accessoryInfo> getAccessory() {
		return accessory;
	}

	public void setAccessory(ArrayList<accessoryInfo> accessory) {
		this.accessory = accessory;
	}

	public accessoryObj()
	{
		
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeList(accessory);
	}

	public static final Parcelable.Creator<accessoryObj> CREATOR = new Creator<accessoryObj>() {
		@Override
		public accessoryObj createFromParcel(Parcel source) {
			return new accessoryObj(source);
		}

		@Override
		public accessoryObj[] newArray(int size) {
			return new accessoryObj[size];
		}
	};

	@SuppressWarnings("unchecked")
	public accessoryObj(Parcel in) {

		accessory = in.readArrayList(accessoryInfo.class.getClassLoader());

	}

}
