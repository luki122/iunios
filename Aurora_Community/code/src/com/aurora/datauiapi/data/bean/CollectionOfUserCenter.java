package com.aurora.datauiapi.data.bean;

import java.util.ArrayList;

public class CollectionOfUserCenter {

	private PublishPageInfo pageuser = new PublishPageInfo();
	private int countRow;//总记录数
	private int page;//当前页
	private int pageRow;//没也记录数
	private int totalPage;//总页数
	private ArrayList<CollectionDataInfo> dataContext = new ArrayList<CollectionDataInfo>();
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
	public ArrayList<CollectionDataInfo> getDataContext() {
		return dataContext;
	}
	public void setDataContext(ArrayList<CollectionDataInfo> dataContext) {
		this.dataContext = dataContext;
	}
	
	
	

}
