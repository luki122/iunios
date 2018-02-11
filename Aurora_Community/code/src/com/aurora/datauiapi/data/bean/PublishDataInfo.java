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

import java.io.Serializable;
import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;



public class PublishDataInfo{
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
	
	private String body;//文章内容
	
	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	// 文章类型 1：文章，2：图片
	private String type;
	// 文章状态
	private String status;
	// 置顶推荐  1是 0否
	private String top_recommend;
	// 作者IP
	private String authorip;
	// 是否被当前访问用户私藏
	private String isFavor;
	
	// 文章标签数据tag数组
	private ArrayList<TagInfo> tags = new ArrayList<TagInfo>();

    private String created_format;
    
	
	public String getCreated_format() {
		return created_format;
	}

	public void setCreated_format(String created_format) {
		this.created_format = created_format;
	}

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

	public String getType() {
		return type;
	}

	public void setType(String type) {
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

	public ArrayList<ImageInfo> getImages() {
		return images;
	}

	public void setImages(ArrayList<ImageInfo> images) {
		this.images = images;
	}

	public ArrayList<TagInfo> getTags() {
		return tags;
	}

	public void setTags(ArrayList<TagInfo> tags) {
		this.tags = tags;
	}
}
