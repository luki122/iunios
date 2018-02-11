package com.aurora.voiceassistant.model;

import java.util.ArrayList;

public class RspXmlCxMsgLc 
{
	/*
	wappagetype：wap页类型，目前使用的是"5"这一组数据
	url：跳转url
	title: 显示标题
	content1：起止时间
	content2：里程和票价
	linkurl：更多url
	
	*/
	private ArrayList<RspXmlCxMsgLc.Item> list = new ArrayList<Item>();
	
	public void listAdd(RspXmlCxMsgLc.Item node)
	{
		list.add(node);
	}
	public int getListSize()
	{
		return list.size();
	}
	public ArrayList<RspXmlCxMsgLc.Item> getList()
	{
		return list;
	}
	
	public class Item
	{
		
		private String name;
		private String number;
		private String sStation;
		private String eStation;
		private String sTime;
		private String eTime;
		private String fPrice;
		private String sPrice;
		private String spendTime;
		private String linkurl;
		private String url;
		
		public String getName() 
		{
			return name;
		}
		public void setName(String name) 
		{
			this.name = name;
		}
		public String getNumber() 
		{
			return number;
		}
		public void setNumber(String number) 
		{
			this.number = number;
		}
		public String getsStation() 
		{
			return sStation;
		}
		public void setsStation(String sStation) 
		{
			this.sStation = sStation;
		}
		public String geteStation() 
		{
			return eStation;
		}
		public void seteStation(String eStation) 
		{
			this.eStation = eStation;
		}
		public String getsTime() 
		{
			return sTime;
		}
		public void setsTime(String sTime) 
		{
			this.sTime = sTime;
		}
		public String geteTime() 
		{
			return eTime;
		}
		public void seteTime(String eTime) 
		{
			this.eTime = eTime;
		}
		public String getfPrice() 
		{
			return fPrice;
		}
		public void setfPrice(String fPrice) 
		{
			this.fPrice = fPrice;
		}
		public String getsPrice() 
		{
			return sPrice;
		}
		public void setsPrice(String sPrice) 
		{
			this.sPrice = sPrice;
		}
		public String getSpendTime() 
		{
			return spendTime;
		}
		public void setSpendTime(String spendTime) 
		{
			this.spendTime = spendTime;
		}
		public String getLinkurl()
		{
			return linkurl;
		}
		public void setLinkurl(String linkurl)
		{
			this.linkurl = linkurl;
		}
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		
	}
	
	
}
