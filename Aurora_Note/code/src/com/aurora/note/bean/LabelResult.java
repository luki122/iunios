package com.aurora.note.bean;


/**
 * 
 * @author jason 新建标签数据bean
 */

public class LabelResult {
	// 对应序列ID
	private int id;

	// 标签内容
	private String content;

	//  更新时间
	private long update_time;

	// 是否加密
	private boolean isEncrypted;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public long getUpdate_time() {
		return update_time;
	}

	public void setUpdate_time(long update_time) {
		this.update_time = update_time;
	}

	public boolean isEncrypted() {
		return isEncrypted;
	}

	public void setEncrypted(boolean isEncrypted) {
		this.isEncrypted = isEncrypted;
	}

}
