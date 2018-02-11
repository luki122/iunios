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

public class categoryListtem {
	// 应用ID
	private int id;
	// 类型：APP和GAME
	private String type;
	// 类别名称
	private String name;
	// 类别的图标地址
	private String icon;
	// 上一级类别ID
	private int parentId;
	//包含的子类
	private List<subcategoryListtem> subCategories =  new ArrayList<subcategoryListtem>();
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public int getParentId() {
		return parentId;
	}
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}
	public List<subcategoryListtem> getSubCategories() {
		return subCategories;
	}
	public void setSubCategories(List<subcategoryListtem> subCategories) {
		this.subCategories = subCategories;
	}
	
}
