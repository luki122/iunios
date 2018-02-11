package com.aurora.datauiapi.data.bean;

import java.util.ArrayList;
import java.util.List;

public class MarketListObject {
	
	//返回码  1：成功  其它失败 
	private int code;
	//成功和失败的描述 
	private String desc;
	//主界面banner字段
	private List<topVideoItem> banners = new ArrayList<topVideoItem>();
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
	public List<topVideoItem> getBanners() {
		return banners;
	}
	public void setBanners(List<topVideoItem> banners) {
		this.banners = banners;
	}
	public List<appListtem> getApps() {
		return apps;
	}
	public void setApps(List<appListtem> apps) {
		this.apps = apps;
	}

}
