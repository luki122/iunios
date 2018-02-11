package com.aurora.datauiapi.data.bean;

import java.util.ArrayList;


public class ArticleDataInfo{
	public static final int NEWS_WITHOUT_PIC_TYPE = 1;
	public static final int NEWS_CONTAINS_PIC_TYPE = 2;

	
	
	// 文章ID
	private String pid;
	// 作者ID
	private String uid;
	// 作者昵称
	private String uid_nick;
	// 作者头像
	private String uid_avatar;
	// 文章图片附件image数组，可为null
	private ArrayList<ImageInfo> images = new ArrayList<ImageInfo>();
	// 文章创建时间，毫秒
	private String created;
	// 文章被私藏数
	private String favorites;
	
	// 文章回复数
	private String replies;
	// 文章标题
	private String subject;
	// 文章内容摘要
	private String summary;
	// 文章类型 1：文章，2：图片
	private int type;
	// 文章状态
	private String status;
	// 置顶推荐  1是 0否
	private String top_recommend;
	// 作者IP
	private String authorip;
	// 是否被当前访问用户私藏
	private String isFavor;
	// 文章标签数据tag数组
	
	private String body;
	
	private ArrayList<TagInfo> tag = new ArrayList<TagInfo>();
	public String getPid() {
		return pid;
	}
	public void setPid(String pid) {
		this.pid = pid;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getUid_nick() {
		return uid_nick;
	}
	public void setUid_nick(String uid_nick) {
		this.uid_nick = uid_nick;
	}
	public String getUid_avatar() {
		return uid_avatar;
	}
	public void setUid_avatar(String uid_avatar) {
		this.uid_avatar = uid_avatar;
	}
	public ArrayList<ImageInfo> getImages() {
		return images;
	}
	public void setImages(ArrayList<ImageInfo> images) {
		this.images = images;
	}


	public String getCreated() {
		return created;
	}
	public void setCreated(String created) {
		this.created = created;
	}
	public String getFavorites() {
		return favorites;
	}
	public void setFavorites(String favorites) {
		this.favorites = favorites;
	}
	public String getReplies() {
		return replies;
	}
	public void setReplies(String replies) {
		this.replies = replies;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getTop_recommend() {
		return top_recommend;
	}
	public void setTop_recommend(String top_recommend) {
		this.top_recommend = top_recommend;
	}
	public String getAuthorip() {
		return authorip;
	}
	public void setAuthorip(String authorip) {
		this.authorip = authorip;
	}
	public String getIsFavor() {
		return isFavor;
	}
	public void setIsFavor(String isFavor) {
		this.isFavor = isFavor;
	}
	public ArrayList<TagInfo> getTag() {
		return tag;
	}
	public void setTag(ArrayList<TagInfo> tag) {
		this.tag = tag;
	}
	
	public void setBody(String body){
		this.body = body;
	}
	
	public String getBody(){
		return this.body;
	}
	
}
