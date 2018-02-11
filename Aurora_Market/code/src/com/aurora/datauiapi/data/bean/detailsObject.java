package com.aurora.datauiapi.data.bean;

import java.util.ArrayList;
import java.util.List;

public class detailsObject {
	
	//返回码  1：成功  其它失败 
	private int code;
	//成功和失败的描述 
	private String desc;
	
	//应用信息结构体
	private appiteminfo appInfo = new appiteminfo();

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public appiteminfo getAppInfo() {
		return appInfo;
	}

	public void setAppInfo(appiteminfo appInfo) {
		this.appInfo = appInfo;
	}
		
}
