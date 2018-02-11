package com.aurora.datauiapi.data.bean;

import java.util.List;


import android.util.Log;

/** information about news on main page will be shown */

public class NewsInfo {

	private static final String TAG = "NewsInfo";

/*	public static final int NEWS_CONTAINS_PIC_TYPE = 1;
	public static final int NEWS_WITHOUT_PIC_TYPE = 2;*/
	
	private  List<String> url;

	private String content;

	private String date;

	private int type;
	
	public void setDate(String d) {
		this.date = d;
	}

	public String getDate() {
		return this.date;
	}

	public void setUrl(List<String> url) {
		this.url = url;
	}

	public List<String> getUrl() {
		return this.url;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getContent() {
		return this.content;
	}

	public void setType(int type){
		this.type = type;
	}
	
	public int  getType(){
		return this.type;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "info:" + "date=" + date + ";" + "content=" + content + ";"
				+ "url=" + url + "url.size=" + url.size();
	}

}
