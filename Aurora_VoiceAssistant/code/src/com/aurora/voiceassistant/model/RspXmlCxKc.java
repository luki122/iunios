package com.aurora.voiceassistant.model;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import android.util.Log;

public class RspXmlCxKc 
{
	private String trip_from;
	private String trip_tod;
	
	private String linkurl;
	private String linkcontent;
	
	private ArrayList<RspXmlCxKc.Item> list = new ArrayList<RspXmlCxKc.Item>();
	public static final String TAG = "RspXmlCxKc";
	
	public ArrayList<RspXmlCxKc.Item> getList()
	{
		return list;
	}
	
	public int getListSize()
	{
		return list.size();
	}
	
	public void listAdd(RspXmlCxKc.Item node)
	{
		list.add(node);
	}
	
	
	public String getTrip_from() 
	{
		return trip_from;
	}

	public void setTrip_from(String trip_from) 
	{
		this.trip_from = trip_from;
	}

	public String getTrip_tod() 
	{
		return trip_tod;
	}

	public void setTrip_tod(String trip_tod) 
	{
		this.trip_tod = trip_tod;
	}

	public String getLinkurl() 
	{
		if(!is3GLink(linkurl)){
			linkurl = append3GUrl(trip_from, trip_tod);
		}
		return linkurl;
	}

	public void setLinkurl(String linkurl) 
	{	
		this.linkurl = linkurl;
	}

	public String getLinkcontent() 
	{
		return linkcontent;
	}

	public void setLinkcontent(String linkcontent) 
	{
		this.linkcontent = linkcontent;
	}
	
	private boolean is3GLink(String url){
		if(url!=null&&url.contains("http://3g")){
			return true;
		}
		return false;
	}
	
	private String append3GUrl(String from,String to){
		String urlHead =null;
		try {
			urlHead ="http://3g.trip8080.com/timeTable.htm?stCity="
					+ URLEncoder.encode(from, "utf-8") + "&endCity="
					+ URLEncoder.encode(to, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			Log.i(TAG, "append 3G Error");
			e.printStackTrace();
		}
		return urlHead;
	}

	
	
	public class Item
	{
		private String col0;
		private String col1;
		private String col2;
		
		public String getCol0() 
		{
			return col0;
		}
		public void setCol0(String col0) 
		{
			this.col0 = col0;
		}
		public String getCol1() 
		{
			return col1;
		}
		public void setCol1(String col1) 
		{
			this.col1 = col1;
		}
		public String getCol2() 
		{
			return col2;
		}
		public void setCol2(String col2) 
		{
			this.col2 = col2;
		}
		
	}
}
