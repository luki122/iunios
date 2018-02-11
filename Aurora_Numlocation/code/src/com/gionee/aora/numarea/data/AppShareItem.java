package com.gionee.aora.numarea.data;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;

/**
 * Copyright (c) 2001, 深圳市奥软网络科技公司研发部
 * All rights reserved.
 *
 * @file AppShareItem.java
 * 摘要: 分享对象
 *
 * @author yinzw
 * @data 2011-7-7
 * @version 
 *
 */
public class AppShareItem {
	
	/**小图标*/
	protected Bitmap icon;
	
	/**包名*/
	protected String packageName;
	/**软件id*/
	protected String id;
	
	/**ActivityInfo*/
	ActivityInfo info;

	/**ActivityInfo
	 * @return
	 */
	public ActivityInfo getInfo() {
		return info;
	}

	/**ActivityInfo
	 * @param info
	 */
	public void setInfo(ActivityInfo info) {
		this.info = info;
	}
	
	/**
	 * 获取小图标
	 * @return
	 */
	public Bitmap getIcon() {
		return icon;
	}

	/**
	 * 设置小图标
	 * @param icon
	 */
	public void setIcon(Bitmap icon) {
		this.icon = icon;
	}
	
	/**
	 * get id
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * set id
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * 获取包名
	 * @return
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * 设置包名
	 * @param packageName
	 */
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof AppShareItem)
			return packageName.equals(((AppShareItem) o).getPackageName());
		return false;
	}
}
