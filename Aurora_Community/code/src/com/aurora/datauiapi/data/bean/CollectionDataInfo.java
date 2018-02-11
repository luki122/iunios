package com.aurora.datauiapi.data.bean;

import java.util.ArrayList;

public class CollectionDataInfo {

	private String ordertime;
	private String mix_uid;
	private String authorip; //作者IP
	private String replymsg;
	private String replies;//文章回复数
	private String attachment;
	private String favorites;//文章私藏数
	private String body;
	private String status;
	private String type;//文章内型 1表示文章 2表示图片
	private String notpassreason;
	private String pid;//文章id
	private ArrayList<TagInfo> tags = new ArrayList<TagInfo>();//文章标签信息
	private String subject;//文章标题
	private String gid;//
	private ArrayList<ImageInfo> images = new ArrayList<ImageInfo>();//文章图片
	private String uid;//文章id
	private String summary;//文章内容摘要
	private String created;//文章创建时间
	private String top_recommend;//推荐置顶
	private String uid_nick; //作者昵称
	private String uid_avatar;//作者头像
	private String mix_uid_nick;
	private String mix_uid_avatar;
	private String isliked;
	private String created_format;//创建时间特定的日志格式
	
	public String getOrdertime() {
		return ordertime;
	}
	public void setOrdertime(String ordertime) {
		this.ordertime = ordertime;
	}
	public String getMix_uid() {
		return mix_uid;
	}
	public void setMix_uid(String mix_uid) {
		this.mix_uid = mix_uid;
	}
	public String getAuthorip() {
		return authorip;
	}
	public void setAuthorip(String authorip) {
		this.authorip = authorip;
	}
	public String getReplymsg() {
		return replymsg;
	}
	public void setReplymsg(String replymsg) {
		this.replymsg = replymsg;
	}
	public String getReplies() {
		return replies;
	}
	public void setReplies(String replies) {
		this.replies = replies;
	}
	public String getAttachment() {
		return attachment;
	}
	public void setAttachment(String attachment) {
		this.attachment = attachment;
	}
	public String getFavorites() {
		return favorites;
	}
	public void setFavorites(String favorites) {
		this.favorites = favorites;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getNotpassreason() {
		return notpassreason;
	}
	public void setNotpassreason(String notpassreason) {
		this.notpassreason = notpassreason;
	}
	public String getPid() {
		return pid;
	}
	public void setPid(String pid) {
		this.pid = pid;
	}
	public ArrayList<TagInfo> getTags() {
		return tags;
	}
	public void setTags(ArrayList<TagInfo> tags) {
		this.tags = tags;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getGid() {
		return gid;
	}
	public void setGid(String gid) {
		this.gid = gid;
	}
	public ArrayList<ImageInfo> getImages() {
		return images;
	}
	public void setImages(ArrayList<ImageInfo> images) {
		this.images = images;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getCreated() {
		return created;
	}
	public void setCreated(String created) {
		this.created = created;
	}
	public String getTop_recommend() {
		return top_recommend;
	}
	public void setTop_recommend(String top_recommend) {
		this.top_recommend = top_recommend;
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
	public String getMix_uid_nick() {
		return mix_uid_nick;
	}
	public void setMix_uid_nick(String mix_uid_nick) {
		this.mix_uid_nick = mix_uid_nick;
	}
	public String getMix_uid_avatar() {
		return mix_uid_avatar;
	}
	public void setMix_uid_avatar(String mix_uid_avatar) {
		this.mix_uid_avatar = mix_uid_avatar;
	}
	public String getIsliked() {
		return isliked;
	}
	public void setIsliked(String isliked) {
		this.isliked = isliked;
	}
	public String getCreated_format() {
		return created_format;
	}
	public void setCreated_format(String created_format) {
		this.created_format = created_format;
	}
	
	
	
	
}
