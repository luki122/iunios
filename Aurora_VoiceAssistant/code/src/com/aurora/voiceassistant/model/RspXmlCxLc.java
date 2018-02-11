package com.aurora.voiceassistant.model;

import java.util.ArrayList;

public class RspXmlCxLc 
{
	private String trip_from;
	private String trip_to;
	private String triType;
	private String allurl;//link linkurl
	private ArrayList<RspXmlCxLc.Item> list = new ArrayList<RspXmlCxLc.Item>();
	
	
	public class Item
	{
		private String name;
		private String sTime;
		private String eTime;
		
		private String sStation;
		private String eStation;
		private String spendTime;
		
		private String fPrice;
		private String sPrice;
		
		private String linkurl;
		private String booking;
		
		public String getName() 
		{
			return name;
		}
		public void setName(String name) 
		{
			this.name = name;
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
		public String getBooking() 
		{
			return booking;
		}
		public void setBooking(String booking) 
		{
			this.booking = booking;
		}
	}
	public String getTrip_from() 
	{
		return trip_from;
	}

	public void setTrip_from(String trip_from) 
	{
		this.trip_from = trip_from;
	}

	public String getTrip_to() 
	{
		return trip_to;
	}

	public void setTrip_to(String trip_to) 
	{
		this.trip_to = trip_to;
	}

	public String getTriType() 
	{
		return triType;
	}

	public void setTriType(String triType) 
	{
		this.triType = triType;
	}

	public String getAllurl() 
	{
		return allurl;
	}

	public void setAllurl(String allurl) 
	{
		this.allurl = allurl;
	}
	
	public ArrayList<RspXmlCxLc.Item> getList()
	{
		return list;
	}
	
	public int getListSize()
	{
		return list.size();
	}
	
	public void listAdd(RspXmlCxLc.Item node)
	{
		list.add(node);
	}
	
}
