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

public class recommendInfo {
	// 应用ID
	private int id;
	// 应用名称
	private String title;
	private String packageName;
	private iconItem icons = new iconItem();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public iconItem getIcons() {
		return icons;
	}

	public void setIcons(iconItem icons) {
		this.icons = icons;
	}
	
	

}
