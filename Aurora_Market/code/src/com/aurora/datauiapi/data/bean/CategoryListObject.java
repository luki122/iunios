package com.aurora.datauiapi.data.bean;

import java.util.ArrayList;
import java.util.List;

public class CategoryListObject {
	
	//返回码  1：成功  其它失败 
	private int code;
	//成功和失败的描述 
	private String desc;
	//主界面list数据
	private List<categoryListtem> categories =  new ArrayList<categoryListtem>();
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
	public List<categoryListtem> getCategories() {
		return categories;
	}
	public void setCategories(List<categoryListtem> categories) {
		this.categories = categories;
	}

}
