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



public class PublishPageInfo {
	// 用户ID
	private String uid;
	// 当前用户和已登录用户是否一致
	private String isCurUser;
	// 互粉关系
	private String mutual;
	// 昵称
	private String nickname;
	// 头像
	private String avatar;

	// 个人主页的banner图片
	private String banner;
	// 文章数
	private String posts;
	// 收藏数
	private String favorites;
	// 等级
	private String level;
	// 积分
	private String points;
	
	
	// 小组数
	private String groups;
	// 回复数
	private String replies;
	// 关注数
	private String follows;
	// 粉丝数
	private String fans;
	// 签名
	private String desc;
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getIsCurUser() {
		return isCurUser;
	}
	public void setIsCurUser(String isCurUser) {
		this.isCurUser = isCurUser;
	}
	public String getMutual() {
		return mutual;
	}
	public void setMutual(String mutual) {
		this.mutual = mutual;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getAvatar() {
		return avatar;
	}
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	public String getBanner() {
		return banner;
	}
	public void setBanner(String banner) {
		this.banner = banner;
	}
	public String getPosts() {
		return posts;
	}
	public void setPosts(String posts) {
		this.posts = posts;
	}
	public String getFavorites() {
		return favorites;
	}
	public void setFavorites(String favorites) {
		this.favorites = favorites;
	}
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	public String getPoints() {
		return points;
	}
	public void setPoints(String points) {
		this.points = points;
	}
	public String getGroups() {
		return groups;
	}
	public void setGroups(String groups) {
		this.groups = groups;
	}
	public String getReplies() {
		return replies;
	}
	public void setReplies(String replies) {
		this.replies = replies;
	}
	public String getFollows() {
		return follows;
	}
	public void setFollows(String follows) {
		this.follows = follows;
	}
	public String getFans() {
		return fans;
	}
	public void setFans(String fans) {
		this.fans = fans;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}

}
