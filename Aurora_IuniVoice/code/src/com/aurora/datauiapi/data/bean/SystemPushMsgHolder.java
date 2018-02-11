package com.aurora.datauiapi.data.bean;

public class SystemPushMsgHolder extends BaseResponseObject {

	private SystemPushMsgList data;

	private String formhash;
	
	public String getFormhash() {
		return formhash;
	}

	public void setFormhash(String formhash) {
		this.formhash = formhash;
	}

	public SystemPushMsgList getData() {
		return data;
	}

	public void setData(SystemPushMsgList data) {
		this.data = data;
	}
	
	
}
