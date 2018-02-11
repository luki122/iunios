package com.aurora.datauiapi.data.bean;

import android.os.Parcel;
import android.os.Parcelable;


public class UserInfoExtcredits implements Parcelable {

	/**
	 * 
	 */
	private String img;
	private String title;
	private String unit;
	private String ratio;
	private String showinthread;
	private String allowexchangein;
	private String allowexchangeout;
	private String value;
	public String getImg() {
		return img;
	}
	public void setImg(String img) {
		this.img = img;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public String getRatio() {
		return ratio;
	}
	public void setRatio(String ratio) {
		this.ratio = ratio;
	}
	public String getShowinthread() {
		return showinthread;
	}
	public void setShowinthread(String showinthread) {
		this.showinthread = showinthread;
	}
	public String getAllowexchangein() {
		return allowexchangein;
	}
	public void setAllowexchangein(String allowexchangein) {
		this.allowexchangein = allowexchangein;
	}
	public String getAllowexchangeout() {
		return allowexchangeout;
	}
	public void setAllowexchangeout(String allowexchangeout) {
		this.allowexchangeout = allowexchangeout;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub
		arg0.writeString(img);
		arg0.writeString(title);
		arg0.writeString(unit);
		arg0.writeString(ratio);
		arg0.writeString(showinthread);
		arg0.writeString(allowexchangein);
		arg0.writeString(allowexchangeout);
		arg0.writeString(value);
	}
	
	public UserInfoExtcredits(Parcel in){
		img = in.readString();
		title = in.readString();
		unit = in.readString();
		ratio = in.readString();
		showinthread = in.readString();
		allowexchangein = in.readString();
		allowexchangeout = in.readString();
		value = in.readString();
		
	}
	
	public UserInfoExtcredits(){}
	
	public static final Parcelable.Creator<UserInfoExtcredits> CREATOR = new Parcelable.Creator<UserInfoExtcredits>() {

		@Override
		public UserInfoExtcredits createFromParcel(Parcel arg0) {
			// TODO Auto-generated method stub
			return new UserInfoExtcredits(arg0);
		}

		@Override
		public UserInfoExtcredits[] newArray(int arg0) {
			// TODO Auto-generated method stub
			return new UserInfoExtcredits[arg0];
		}
	};
	
	
}
