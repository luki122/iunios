package com.aurora.datauiapi.data.bean;

import java.util.ArrayList;

public class PublishOfUserCenter {

	private PublishPageInfo pageuser = new PublishPageInfo();
	private int countRow;
	private int page;
	private int pageRow;
	private int totalPage;
	private ArrayList<PublishDataInfo> dataContext = new ArrayList<PublishDataInfo>();
	public PublishPageInfo getPageuser() {
		return pageuser;
	}
	public void setPageuser(PublishPageInfo pageuser) {
		this.pageuser = pageuser;
	}
	public int getCountRow() {
		return countRow;
	}
	public void setCountRow(int countRow) {
		this.countRow = countRow;
	}
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public int getPageRow() {
		return pageRow;
	}
	public void setPageRow(int pageRow) {
		this.pageRow = pageRow;
	}
	public int getTotalPage() {
		return totalPage;
	}
	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}
	public ArrayList<PublishDataInfo> getDataContext() {
		return dataContext;
	}
	public void setDataContext(ArrayList<PublishDataInfo> dataContext) {
		this.dataContext = dataContext;
	}

}
