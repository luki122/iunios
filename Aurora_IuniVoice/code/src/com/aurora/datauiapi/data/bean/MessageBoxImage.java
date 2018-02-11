package com.aurora.datauiapi.data.bean;

import android.text.TextUtils;

public class MessageBoxImage {

	private String aid;
	private String desc;
	private String fpath;
	private String height;
	private String width;
	private String serial_no;
	public String getThumbPath(){
		if(TextUtils.isEmpty(fpath))
		{
			return null;
		}
		
		int last = fpath.lastIndexOf(".");
		String imageEnd = fpath.substring(last, fpath.length());
		return fpath.replace(imageEnd,thumbTypes[3]+imageEnd);
		
	}
	private String[] thumbTypes = {"_100x100","_200x200","_400x400","_800x800"};
	public String getThumbPath(int type){
		if(TextUtils.isEmpty(fpath))
		{
			return null;
		}
		
		int last = fpath.lastIndexOf(".");
		String imageEnd = fpath.substring(last, fpath.length());
		return fpath.replace(imageEnd,thumbTypes[type]+imageEnd);
		
	}

	public String getAid() {
		return aid;
	}

	public void setAid(String aid) {
		this.aid = aid;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getFpath() {
		return fpath;
	}

	public void setFpath(String fpath) {
		this.fpath = fpath;
	}

	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public String getSerial_no() {
		return serial_no;
	}

	public void setSerial_no(String serial_no) {
		this.serial_no = serial_no;
	}

	public String[] getThumbTypes() {
		return thumbTypes;
	}

	public void setThumbTypes(String[] thumbTypes) {
		this.thumbTypes = thumbTypes;
	}
}
