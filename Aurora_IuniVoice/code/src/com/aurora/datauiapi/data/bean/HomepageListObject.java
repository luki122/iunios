package com.aurora.datauiapi.data.bean;

public class HomepageListObject extends BaseResponseObject {

	private String formhash;
	private HomepageData data;

	public String getFormhash() {
		return formhash;
	}

	public void setFormhash(String formhash) {
		this.formhash = formhash;
	}

	public HomepageData getData() {
		return data;
	}

	public void setData(HomepageData data) {
		this.data = data;
	}

}
