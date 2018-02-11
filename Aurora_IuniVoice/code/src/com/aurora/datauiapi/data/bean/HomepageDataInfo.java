package com.aurora.datauiapi.data.bean;

public class HomepageDataInfo {

	private String tid; // 帖子ID
	private String subject; // 帖子标题
	private String author; // 作者
	private String lastpost; // 最后回帖时间
	private String portal_img; // 精华帖图片
	private String portal_summary; // 精华帖描述
	private String avatar;	// 用户头像

	public String getTid() {
		return tid;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getLastpost() {
		return lastpost;
	}

	public void setLastpost(String lastpost) {
		this.lastpost = lastpost;
	}

	public String getPortal_img() {
		return portal_img;
	}

	public void setPortal_img(String portal_img) {
		this.portal_img = portal_img;
	}

	public String getPortal_summary() {
		return portal_summary;
	}

	public void setPortal_summary(String portal_summary) {
		this.portal_summary = portal_summary;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

}
