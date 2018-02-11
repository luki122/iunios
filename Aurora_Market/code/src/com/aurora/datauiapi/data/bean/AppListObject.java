package com.aurora.datauiapi.data.bean;

import java.util.ArrayList;

public class AppListObject {

	private String code;
	private String desc;
	private ArrayList<appListtem> appList = new ArrayList<appListtem>();

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public ArrayList<appListtem> getAppList() {
		return appList;
	}

	public void setAppList(ArrayList<appListtem> appList) {
		this.appList = appList;
	}

}
