package com.aurora.datauiapi.data.bean;



/** 
* @ClassName: NewsCategoryObject
* @Description: 首页-话题圈列表
* @author jason
* @date 2015年3月17日 下午5:22:59
* 
*/ 
public class UpArticleObject extends BaseResponseObject{


	//业务数据
	public UpArticleInfo data;

	public UpArticleInfo getData() {
		return data;
	}

	public void setData(UpArticleInfo data) {
		this.data = data;
	}



	
}
