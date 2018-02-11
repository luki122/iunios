package com.aurora.datauiapi.data.bean;

import java.util.ArrayList;
import java.util.List;

public class SpecialListObject {

	//返回码  1：成功  其它失败 
	private int code;
	//成功和失败的描述 
	private String desc;
	private List<specials> specials = new ArrayList<specials>();
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
	public List<specials> getSpecials() {
		return specials;
	}
	public void setSpecials(List<specials> specials) {
		this.specials = specials;
	}



}
