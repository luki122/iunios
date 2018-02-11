package com.aurora.voiceassistant.model;

import java.util.ArrayList;
import java.util.List;


import android.os.Parcel;
import android.os.Parcelable;

public class RspXmlPic {
	private  String description;
	//private  RspWebview webview;
	private  String morelink;
	
	
	private  ArrayList<RspXmlPicItem> list ;
	
	RspXmlPic() {
		list = new ArrayList<RspXmlPicItem>();
	}
	
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getMorelink() {
		return morelink;
	}
	
	public void setMorelink(String morelink) {
		this.morelink = morelink;
	}
	
	
	/*
	public RspWebview getWebview()
	{
		return webview;
	}

	public void setWebview(RspWebview webview) 
	{
		this.webview = webview;
	}
	*/
	
	public void listAdd(RspXmlPicItem node) {
		list.add(node);
	}
	
	public ArrayList<RspXmlPicItem> getList() {
		return list;
	}
	
	public int getListSize() {
		return list.size();
	}
	
}
