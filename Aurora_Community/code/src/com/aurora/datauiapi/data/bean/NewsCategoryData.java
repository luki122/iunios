package com.aurora.datauiapi.data.bean;

public class NewsCategoryData {
	//话题圈封面
	public String cover;
	//话题圈名称
	public String tname;
	//是否允许取消
	public String cancancel;
	//是否已关注
	public String liked;
	//文章数
	public String post_count;
	//更新时间/最后发表时间
	public String updated;
	//状态 200:正常，4:禁止
	public String status;
	//权重/排序
	public String weight;
	//话题圈图标
	public String icon;
	//CSS样式的名称，用于控制话题圈的显示样式
	public String style;
	//是否允许订阅 0：不允许，1：允许
	public String rss_feed;
	//话题圈描述
	public String desc;
	//话题圈ID
	public String tid;
	
	public String tsname;
	
	public String getTsname() {
		return tsname;
	}
	public void setTsname(String tsname) {
		this.tsname = tsname;
	}
	public String getCover() {
		return cover;
	}
	public void setCover(String cover) {
		this.cover = cover;
	}
	public String getTname() {
		return tname;
	}
	public void setTname(String tname) {
		this.tname = tname;
	}
	public String getCancancel() {
		return cancancel;
	}
	public void setCancancel(String cancancel) {
		this.cancancel = cancancel;
	}
	public String getLiked() {
		return liked;
	}
	public void setLiked(String liked) {
		this.liked = liked;
	}
	public String getPost_count() {
		return post_count;
	}
	public void setPost_count(String post_count) {
		this.post_count = post_count;
	}
	public String getUpdated() {
		return updated;
	}
	public void setUpdated(String updated) {
		this.updated = updated;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getWeight() {
		return weight;
	}
	public void setWeight(String weight) {
		this.weight = weight;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getStyle() {
		return style;
	}
	public void setStyle(String style) {
		this.style = style;
	}
	public String getRss_feed() {
		return rss_feed;
	}
	public void setRss_feed(String rss_feed) {
		this.rss_feed = rss_feed;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getTid() {
		return tid;
	}
	public void setTid(String tid) {
		this.tid = tid;
	}
	
	
}
