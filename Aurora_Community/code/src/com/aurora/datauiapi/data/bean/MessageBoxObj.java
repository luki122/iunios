package com.aurora.datauiapi.data.bean;

public class MessageBoxObj {

	private String nid;
	private String type;
	private String ruid;
	private String title;
	private String body;
	private String itemid;
	private String itemname;
	private String author_avatar;
	private String authorid;
	private String author_nickname;
	private String uname;
	private String news;
	private String status;
	private String updated;
	private String updated_format;
	private MessageBoxArticleInfo article_info;
	private CommentInfo comment_info;
	
	public CommentInfo getComment_info() {
		return comment_info;
	}
	public void setComment_info(CommentInfo comment_info) {
		this.comment_info = comment_info;
	}
	public MessageBoxArticleInfo getArticle_info() {
		return article_info;
	}
	public void setArticle_info(MessageBoxArticleInfo article_info) {
		this.article_info = article_info;
	}
	public String getNid() {
		return nid;
	}
	public void setNid(String nid) {
		this.nid = nid;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getItemid() {
		return itemid;
	}
	public void setItemid(String itemid) {
		this.itemid = itemid;
	}
	public String getItemname() {
		return itemname;
	}
	public void setItemname(String itemname) {
		this.itemname = itemname;
	}
	public String getRuid() {
		return ruid;
	}
	public void setRuid(String ruid) {
		this.ruid = ruid;
	}
	public String getAuthor_avatar() {
		return author_avatar;
	}
	public void setAuthor_avatar(String author_avatar) {
		this.author_avatar = author_avatar;
	}
	public String getAuthor_nickname() {
		return author_nickname;
	}
	public void setAuthor_nickname(String author_nickname) {
		this.author_nickname = author_nickname;
	}
	public String getAuthorid() {
		return authorid;
	}
	public void setAuthorid(String authorid) {
		this.authorid = authorid;
	}
	public String getUname() {
		return uname;
	}
	public void setUname(String uname) {
		this.uname = uname;
	}
	public String getNews() {
		return news;
	}
	public void setNews(String news) {
		this.news = news;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getUpdated() {
		return updated;
	}
	public void setUpdated(String updated) {
		this.updated = updated;
	}
	public String getUpdated_format() {
		return updated_format;
	}
	public void setUpdated_format(String updated_format) {
		this.updated_format = updated_format;
	}
	
}
