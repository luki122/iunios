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

public class WeatherItem {
	private String city;
	private String city_en;
	private String date_y;
	
	private String week;
	private String fchh;
	
	private String temp1;

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCity_en() {
		return city_en;
	}

	public void setCity_en(String city_en) {
		this.city_en = city_en;
	}

	public String getDate_y() {
		return date_y;
	}

	public void setDate_y(String date_y) {
		this.date_y = date_y;
	}

	public String getWeek() {
		return week;
	}

	public void setWeek(String week) {
		this.week = week;
	}

	public String getFchh() {
		return fchh;
	}

	public void setFchh(String fchh) {
		this.fchh = fchh;
	}

	public String getTemp1() {
		return temp1;
	}

	public void setTemp1(String temp1) {
		this.temp1 = temp1;
	}
	
}
