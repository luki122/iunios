package com.aurora.datauiapi.data.bean;

import java.util.ArrayList;

public class SystemMsgData {

	private ArrayList<SystemMsg> list;
	private String count;
	private String perpage;
	private String page;
	public ArrayList<SystemMsg> getList() {
		return list;
	}
	public void setList(ArrayList<SystemMsg> list) {
		this.list = list;
	}
	public String getCount() {
		return count;
	}
	public void setCount(String count) {
		this.count = count;
	}
	public String getPerpage() {
		return perpage;
	}
	public void setPerpage(String perpage) {
		this.perpage = perpage;
	}
	public String getPage() {
		return page;
	}
	public void setPage(String page) {
		this.page = page;
	}
	
	
	
	
}
