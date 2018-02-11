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

import java.util.ArrayList;
import java.util.List;

public class MainListItem {

	//MainListItem类型
	//类型0：单行4个布局 1：双行4个布局 2：单行2个布局
	private int type;
	//专题id
	private int id;
	//专题名称
	private String typeName;
	//专题下的apps
	private List<appListtem> apps = new ArrayList<appListtem>();
	//专题下的apps
	private List<topVideoItem> banners = new ArrayList<topVideoItem>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public List<appListtem> getApps() {
		return apps;
	}

	public void setApps(List<appListtem> apps) {
		this.apps = apps;
	}

	public List<topVideoItem> getBanners() {
		return banners;
	}

	public void setBanners(List<topVideoItem> banners) {
		this.banners = banners;
	}

}
