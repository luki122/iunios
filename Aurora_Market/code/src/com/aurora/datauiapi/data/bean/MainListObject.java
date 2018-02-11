package com.aurora.datauiapi.data.bean;

import java.util.ArrayList;
import java.util.List;

public class MainListObject {
	
	//返回码  1：成功  其它失败 
	private int code;
	//成功和失败的描述 
	private String desc;
	//主界面banner字段
	private List<topVideoItem> banners = new ArrayList<topVideoItem>();
	//主界面list数据
	private List<MainListItem> apps =  new ArrayList<MainListItem>();
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
	public List<MainListItem> getApps() {
		return apps;
	}
	public void setApps(List<MainListItem> apps) {
		this.apps = apps;
	}

}
