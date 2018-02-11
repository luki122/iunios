package com.aurora.datauiapi.data.bean;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class NewsCategoryDataHolder {

	public  ArrayList<NewsCategoryData> allTags;

	//public  List<String> rssTagID;
	
	public ArrayList<NewsCategoryData> getInfo() {
		return this.allTags;
	}

	public void setInfo(ArrayList<NewsCategoryData> info1) {
		this.allTags = info1;
	}
	
//	public void setTagId(List<String> ids){
//		this.rssTagID = ids;
//	}
//	
//	public List<String> getRssTagIds(){
//		return this.rssTagID ;
//	}
}
