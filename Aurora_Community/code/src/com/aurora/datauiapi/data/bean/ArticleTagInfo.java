/*
 * Copyright (C) 2010-2012 TENCENT Inc.All Rights Reserved.
 *
 * FileName: BannerItem
 *
 * Description:  海报图中每项数据
 *
 * History:
 *  1.0   kodywu (kodytx@gmail.com) 2010-11-30   Create
 */
package com.aurora.datauiapi.data.bean;

public class ArticleTagInfo {
	// 话题圈ID
	private String tid;
	// 话题圈名称
	private String tname;
	// 话题圈描述
	private String desc;
	// 文章数
	private String post_count;
	// 话题圈封面
	private String cover;
	// 话题圈图标
	private String icon;
	// 权重/排序
	private String weight;
	// 状态 200:正常，4:禁止
	private String status;
	// 是否允许订阅 0：不允许，1：允许
	private String rss_feed;
	// CSS样式的名称，用于控制话题圈的显示样式
	private String style;
	// 更新时间/最后发表时间
	private String updated;
	// 是否已关注
	private String liked;
	// 是否允许取消
	private String cancancel;

	public String getTid() {
		return tid;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}

	public String getTname() {
		return tname;
	}

	public void setTname(String tname) {
		this.tname = tname;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getPost_count() {
		return post_count;
	}

	public void setPost_count(String post_count) {
		this.post_count = post_count;
	}

	public String getCover() {
		return cover;
	}

	public void setCover(String cover) {
		this.cover = cover;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getWeight() {
		return weight;
	}

	public void setWeight(String weight) {
		this.weight = weight;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRss_feed() {
		return rss_feed;
	}

	public void setRss_feed(String rss_feed) {
		this.rss_feed = rss_feed;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getUpdated() {
		return updated;
	}

	public void setUpdated(String updated) {
		this.updated = updated;
	}

	public String getLiked() {
		return liked;
	}

	public void setLiked(String liked) {
		this.liked = liked;
	}

	public String getCancancel() {
		return cancancel;
	}

	public void setCancancel(String cancancel) {
		this.cancancel = cancancel;
	}

	
}
