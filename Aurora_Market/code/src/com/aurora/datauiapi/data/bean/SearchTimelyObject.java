package com.aurora.datauiapi.data.bean;

import java.util.ArrayList;
import java.util.List;

public class SearchTimelyObject {
	
	//返回码  1：成功  其它失败 
	private int code;
	//成功和失败的描述 
	private String desc;
	//搜索及时接口显示字段
	private List<TimelyInfo> suggestions = new ArrayList<TimelyInfo>();
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
	public List<TimelyInfo> getSuggestions() {
		return suggestions;
	}
	public void setSuggestions(List<TimelyInfo> suggestions) {
		this.suggestions = suggestions;
	}
		
}
