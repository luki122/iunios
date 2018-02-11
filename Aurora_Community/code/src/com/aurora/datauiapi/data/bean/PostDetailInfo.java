package com.aurora.datauiapi.data.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PostDetailInfo implements Serializable {
	private static final long serialVersionUID = 99121L;
	private String mix_uid;
	private String status;
	private String authorip;//作者ip
	private String replies;//文章评论数
	private String replymsg;
	private String type;//文章类型
	private ArrayList<TagInfo> tags;//文章标签
	private String attachment;//附件数量
	private String subject;//文章标题
	private ArrayList<ImageInfo> images;//文章图片
	private String uid;//作者id
	private String summary;//文章摘要
	private String gid;//小组id
	private String notpassreason;
	private String ordertime;
	private String pid;//文章id
	private String favorites;//被私藏数
	private String body;//文章内容
	private String created;//创建时间
	private String top_recommend;
	private String uid_nick;//作者昵称
	private String uid_avatar;//作者头像
	private String mix_uid_nick;
	private String mix_uid_avatar;
	private String isFavor;//是否被收藏
	private String is_recommend;//是否被推荐
	
	public void revertIsFavor(){
		int favour = Integer.parseInt(isFavor);
		if(favour == 1)
		{
			favour = 0;
		}else{
			favour = 1;
		}
		isFavor = favour+"";
	}
	
	public void replyAdd(){
		int reply = Integer.parseInt(replies);
		reply++;
		replies = reply+"";
	}
	
	public void favourAdd(){
		int favor = Integer.parseInt(favorites);
		favor++;
		favorites = favor+"";
	}
	
	public void favourReduce(){
		int favor = Integer.parseInt(favorites);
		favor--;
		favorites = favor+"";
	}
	
	public String getMix_uid() {
		return mix_uid;
	}
	public void setMix_uid(String mix_uid) {
		this.mix_uid = mix_uid;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getAuthorip() {
		return authorip;
	}
	public void setAuthorip(String authorip) {
		this.authorip = authorip;
	}
	public String getReplies() {
		return replies;
	}
	public void setReplies(String replies) {
		this.replies = replies;
	}
	public String getReplymsg() {
		return replymsg;
	}
	public void setReplymsg(String replymsg) {
		this.replymsg = replymsg;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public ArrayList<TagInfo> getTags() {
		return tags;
	}
	public void setTags(ArrayList<TagInfo> tags) {
		this.tags = tags;
	}
	public String getAttachment() {
		return attachment;
	}
	public void setAttachment(String attachment) {
		this.attachment = attachment;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
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
	public String getGid() {
		return gid;
	}
	public void setGid(String gid) {
		this.gid = gid;
	}
	public String getNotpassreason() {
		return notpassreason;
	}
	public void setNotpassreason(String notpassreason) {
		this.notpassreason = notpassreason;
	}
	public String getOrdertime() {
		return ordertime;
	}
	public void setOrdertime(String ordertime) {
		this.ordertime = ordertime;
	}
	public String getPid() {
		return pid;
	}
	public void setPid(String pid) {
		this.pid = pid;
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
	public String getIsFavor() {
		return isFavor;
	}
	public void setIsFavor(String isFavor) {
		this.isFavor = isFavor;
	}
	public String getIs_recommend() {
		return is_recommend;
	}
	public void setIs_recommend(String is_recommend) {
		this.is_recommend = is_recommend;
	}
	
	
}
