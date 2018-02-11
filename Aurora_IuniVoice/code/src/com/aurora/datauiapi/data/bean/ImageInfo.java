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

import java.io.Serializable;

import android.text.TextUtils;
import android.util.Log;

public class ImageInfo implements Serializable {
	private static final long serialVersionUID = 4321L;
	// 附件ID
	private String aid;
	// 附件描述
	private String desc;
	// 附件原始文件路径
	private String fpath;
	// 图片高度
	private String height;
	// 图片宽度
	private String width;
	// 图片缩略图尺寸定义 字符串，宽x高，多组数据逗号隔开
	private String thumbs;
	public String getAid() {
		return aid;
	}
	public void setAid(String aid) {
		this.aid = aid;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getFpath() {
		return fpath;
	}
	
	public String getThumbPath(){
		if(TextUtils.isEmpty(fpath))
		{
			return null;
		}
		
		int last = fpath.lastIndexOf(".");
		String imageEnd = fpath.substring(last, fpath.length());
		return fpath.replace(imageEnd,thumbTypes[3]+imageEnd);
		
	}
	
	private String[] thumbTypes = {"_100x100","_200x200","_400x400","_800x800"};
	
	public String getThumbPath(int type){
		if(TextUtils.isEmpty(fpath))
		{
			return null;
		}
		
		int last = fpath.lastIndexOf(".");
		String imageEnd = fpath.substring(last, fpath.length());
		return fpath.replace(imageEnd,thumbTypes[type]+imageEnd);
		
	}
	
	public void setFpath(String fpath) {
		this.fpath = fpath;
	}
	public String getHeight() {
		return height;
	}
	public void setHeight(String height) {
		this.height = height;
	}
	public String getWidth() {
		return width;
	}
	public void setWidth(String width) {
		this.width = width;
	}
	public String getThumbs() {
		return thumbs;
	}
	public void setThumbs(String thumbs) {
		this.thumbs = thumbs;
	}

	
}
