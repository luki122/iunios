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


public class cacheitem {
	
	//0-首页 1 -新品 2-排行  3-分类 4-专题 5-必备 6-设计奖;
	private int type;
	//应用类型还是游戏类型
	private String app_type;
	//分类id
	private int cat_id;
	//专题id
	private int spe_id;
	//应用名称
	private String context;
	
	//
	private String update_time;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getUpdate_time() {
		return update_time;
	}

	public void setUpdate_time(String update_time) {
		this.update_time = update_time;
	}

	public String getApp_type() {
		return app_type;
	}

	public void setApp_type(String app_type) {
		this.app_type = app_type;
	}

	public int getCat_id() {
		return cat_id;
	}

	public void setCat_id(int cat_id) {
		this.cat_id = cat_id;
	}

	public int getSpe_id() {
		return spe_id;
	}

	public void setSpe_id(int spe_id) {
		this.spe_id = spe_id;
	}
	
	
}
