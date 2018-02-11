package com.aurora.datauiapi.data.bean;



/** 
* @ClassName: NewsCategoryObject
* @Description: 首页-话题圈列表
* @author jason
* @date 2015年3月17日 下午5:22:59
* 
*/ 
public class NewsCategoryObject extends BaseResponseObject{


	//业务数据
	public NewsCategoryDataHolder Data;

	
	public void setData(NewsCategoryDataHolder h){
		this.Data = h;
	}
	
	public NewsCategoryDataHolder getData(){
		return this.Data;
	}
	
}
