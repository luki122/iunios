package com.aurora.datauiapi.data.bean;

import java.util.ArrayList;
import java.util.List;

public class SearchRecListObject {
	
	//返回码  1：成功  其它失败 
	private int code;
	//成功和失败的描述 
	private String desc;
	//搜索推荐显示字段
	private List<recommendInfo> recommendations = new ArrayList<recommendInfo>();
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
	public List<recommendInfo> getRecommendations() {
		return recommendations;
	}
	public void setRecommendations(List<recommendInfo> recommendations) {
		this.recommendations = recommendations;
	}
}
