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

public class IconItem {

	//应用图标256尺寸
	private String px256;
	//应用图标100尺寸
	private String px100;

	public String getPx256() {
		return px256;
	}

	public void setPx256(String px256) {
		this.px256 = px256;
	}

	public String getPx100() {
		return px100;
	}

	public void setPx100(String px100) {
		this.px100 = px100;
	}

}