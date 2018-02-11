package com.aurora.datauiapi.data.bean;

import java.util.ArrayList;


public class ArticleObjInfo{

	// 当前访问用户是否收听该标签，true/false
	private String rss;
	// 文章总数
	private String countRow;
	// 当前分页
	private String page;
	// 分页条目
	private String pageRow;
	// 总页数
	private String totalPage;
	// 当前页首条偏移量
	private String offse;
	// 文章列表数据，数组
	private ArrayList<ArticleDataInfo> dataContext = new ArrayList<ArticleDataInfo>();
	// 标签数据
	private ArticleTagInfo tag = new ArticleTagInfo();

	public String getRss() {
		return rss;
	}

	public void setRss(String rss) {
		this.rss = rss;
	}

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

	public String getOffse() {
		return offse;
	}

	public void setOffse(String offse) {
		this.offse = offse;
	}

	public ArticleTagInfo getTag() {
		return tag;
	}

	public void setTag(ArticleTagInfo tag) {
		this.tag = tag;
	}

	public ArrayList<ArticleDataInfo> getDataContext() {
		return dataContext;
	}

	public void setDataContext(ArrayList<ArticleDataInfo> dataContext) {
		this.dataContext = dataContext;
	}
	
}
