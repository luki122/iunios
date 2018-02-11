package com.aurora.datauiapi.data.bean;

public class CommentInfo {
	private String cid;//评论id
	private String pid;//文章id
	private String gid;
	private String uid;
	private String replycid;//被回复的评论id
	private String body;//评论内容
	private String created;//评论时间
	private String authorip;//评论发布地点ip
	private String status;//
	private String uid_nick;//评论人昵称
	private String uid_avatar;//评论人头像
	private String created_format;//评论时间的特定格式
	private String replyuid_nick;//被回复人的昵称
	private String replyuid;//被回复人的uid
	private String replyuid_avatar;//被回复人的头像
	
	
	public String getReplyuid_nick() {
		return replyuid_nick;
	}
	public void setReplyuid_nick(String replyuid_nick) {
		this.replyuid_nick = replyuid_nick;
	}
	public String getReplyuid() {
		return replyuid;
	}
	public void setReplyuid(String replyuid) {
		this.replyuid = replyuid;
	}
	public String getReplyuid_avatar() {
		return replyuid_avatar;
	}
	public void setReplyuid_avatar(String replyuid_avatar) {
		this.replyuid_avatar = replyuid_avatar;
	}
	public String getCid() {
		return cid;
	}
	public void setCid(String cid) {
		this.cid = cid;
	}
	public String getPid() {
		return pid;
	}
	public void setPid(String pid) {
		this.pid = pid;
	}
	public String getGid() {
		return gid;
	}
	public void setGid(String gid) {
		this.gid = gid;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getReplycid() {
		return replycid;
	}
	public void setReplycid(String replycid) {
		this.replycid = replycid;
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
	public String getAuthorip() {
		return authorip;
	}
	public void setAuthorip(String authorip) {
		this.authorip = authorip;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
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
	public String getCreated_format() {
		return created_format;
	}
	public void setCreated_format(String created_format) {
		this.created_format = created_format;
	}
	
}
