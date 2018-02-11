package com.aurora.voiceassistant.model;

import java.util.ArrayList;

public class RspXmlTq 
{
	private String address;
	private ArrayList<RspXmlTq.Item> list = new ArrayList<RspXmlTq.Item>();
	
	public String getAddress() 
	{
		return address;
	}

	public void setAddress(String address) 
	{
		this.address = address;
	}
	
	public void listAdd(Item node)
	{
		list.add(node);
	}
	public int getListSize()
	{
		return list.size();
	}
	
	public ArrayList<RspXmlTq.Item> getList()
	{
		return list;
	}
	
	
	public class Item
	{
		private String low;
		private String high;
		
		private String date;
		private String days;
		private String week;
		private String daydescription;
		private String nightdescription;
		private String description;
		private String wind;
		//private String dayicon;
		//private String nighticon;
		
		public String getLow() 
		{
			return low;
		}
		public void setLow(String low) 
		{
			this.low = low;
		}
		public String getHigh() 
		{
			return high;
		}
		public void setHigh(String high) 
		{
			this.high = high;
		}
		public String getDate() 
		{
			return date;
		}
		public void setData(String date) 
		{
			this.date = date;
		}
		public String getDays() 
		{
			return days;
		}
		public void setDays(String days) 
		{
			this.days = days;
		}
		public String getWeek() 
		{
			return week;
		}
		public void setWeek(String week) 
		{
			this.week = week;
		}
		public String getDaydescription() 
		{
			return daydescription;
		}
		public void setDaydescription(String daydescription) 
		{
			this.daydescription = daydescription;
		}
		public String getNightdescription() 
		{
			return nightdescription;
		}
		public void setNightdescription(String nightdescription) 
		{
			this.nightdescription = nightdescription;
		}
		public String getDescription() 
		{
			return description;
		}
		public void setDescription(String description) 
		{
			this.description = description;
		}
		public String getWind() 
		{
			return wind;
		}
		public void setWind(String wind) 
		{
			this.wind = wind;
		}
		/*
		public String getDayicon() 
		{
			return dayicon;
		}
		public void setDayicon(String dayicon) 
		{
			this.dayicon = dayicon;
		}
		public String getNighticon() 
		{
			return nighticon;
		}
		public void setNighticon(String nighticon) 
		{
			this.nighticon = nighticon;
		}
		*/
	}
}
