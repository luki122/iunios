package com.aurora.datauiapi.data.bean;

import java.util.ArrayList;
import java.util.List;

public class SpecialAllObject {
	
	//返回码  1：成功  其它失败 
	private int code;
	//成功和失败的描述 
	private String desc;
	private String specialName;
	//主界面list数据
	private List<appListtem> apps =  new ArrayList<appListtem>();
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

	public String getSpecialName() {
		return specialName;
	}
	public void setSpecialName(String specialName) {
		this.specialName = specialName;
	}
	public List<appListtem> getApps() {
		return apps;
	}
	public void setApps(List<appListtem> apps) {
		this.apps = apps;
	}

}
