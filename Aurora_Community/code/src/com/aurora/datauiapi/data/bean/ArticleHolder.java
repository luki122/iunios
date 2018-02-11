package com.aurora.datauiapi.data.bean;

public class ArticleHolder extends BaseResponseObject{


	private ArticleObjInfo data = new ArticleObjInfo();

	public ArticleObjInfo getData() {
		return data;
	}

	public void setData(ArticleObjInfo data) {
		this.data = data;
	}

	
}
