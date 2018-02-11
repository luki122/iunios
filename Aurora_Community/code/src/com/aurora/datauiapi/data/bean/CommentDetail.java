package com.aurora.datauiapi.data.bean;

import java.util.ArrayList;
import java.util.List;

public class CommentDetail {

	private String countRow;//评论总数
	private String page;//当前分页
	private String pageRow;//分页条目
	private String totalPage;//总页数
	private String offset;//当前页首页偏移量
	private ArrayList<CommentInfo> dataContext;
	private String mincid;//最小评论id
	private String maxcid;//最大评论id
	public String getCountRow() {
		return countRow;
	}
	public void setCountRow(String countRow) {
		this.countRow = countRow;
	}
	public String getPage() {
		return page;
	}
	public void setPage(String page) {
		this.page = page;
	}
	public String getPageRow() {
		return pageRow;
	}
	public void setPageRow(String pageRow) {
		this.pageRow = pageRow;
	}
	public String getTotalPage() {
		return totalPage;
	}
	public void setTotalPage(String totalPage) {
		this.totalPage = totalPage;
	}
	public String getOffset() {
		return offset;
	}
	public void setOffset(String offset) {
		this.offset = offset;
	}
	public ArrayList<CommentInfo> getDataContext() {
		return dataContext;
	}
	public void setDataContext(ArrayList<CommentInfo> dataContext) {
		this.dataContext = dataContext;
	}
	public String getMincid() {
		return mincid;
	}
	public void setMincid(String mincid) {
		this.mincid = mincid;
	}
	public String getMaxcid() {
		return maxcid;
	}
	public void setMaxcid(String maxcid) {
		this.maxcid = maxcid;
	}
	
}
