package com.aurora.datauiapi.data.bean;

public class MessageBoxHolder extends BaseResponseObject {

	private MessageBoxList data;

	private int countRow;
	private int page;
	private int pageRow;
	private int totalPage;
	
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

	public MessageBoxList getData() {
		return data;
	}

	public void setData(MessageBoxList data) {
		this.data = data;
	}
	
	
}
