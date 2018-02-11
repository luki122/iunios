package com.aurora.voiceassistant.model;

import java.io.StringReader;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.content.Context;
import android.util.Xml;

public class Parse 
{
	private JSONObject data; 
	private Response rspData;
	
	public Parse(JSONObject data) 
	{
		this.data = data;
	}

	private RspXmlPic parseXmlpic(String data)
	{
		String name = null;
		String type = null;
		String height = null;
		String width = null;
		String size = null;
		String imagelink = null;
		String locimagelink = null;
		
		RspXmlPic rspxmlpic=null;
		int count = 0;
		
		XmlPullParser parser = Xml.newPullParser(); 
		try 
		{
			 parser.setInput(new StringReader(data));
			 
			 int eventType = parser.getEventType();  
			 
	        while (XmlPullParser.END_DOCUMENT != eventType) 
	        {  
	              switch (eventType) 
	              {  
		              case XmlPullParser.START_DOCUMENT:  
		            	  rspxmlpic = new RspXmlPic();  
		                  break;  
		              case XmlPullParser.START_TAG:  
		            	  String tagName =  parser.getName();
		            	  
		                  if ("item".equals(tagName)) 
		                  {  
		                	  name = parser.getAttributeValue(null, "name");  
		                	  type = parser.getAttributeValue(null, "type");  
		                	  height = parser.getAttributeValue(null, "height");  
		                	  width = parser.getAttributeValue(null, "width");  
		                	  size = parser.getAttributeValue(null, "size");  
		                  } 
		                  else if ("imagelink".equals(tagName)) 
		                  {  
		                      imagelink = parser.nextText();
		                  } 
		                  else if ("locimagelink".equals(tagName)) 
		                  {  
		                	  locimagelink = parser.nextText();
		                  } 
		                  break;  
		              case XmlPullParser.END_TAG:  
		            	  String endName = parser.getName();
		                  if("item".equals(endName)) 
		                  {  
		                	  RspXmlPicItem item = new RspXmlPicItem();
		                	  
		                	  item.setHeight(Integer.parseInt(height));
		                	  item.setWidth(Integer.parseInt(width));
		                	  //item.setSize(Integer.parseInt(size));
		                	  item.setName(name);
		                	  item.setImagelink(imagelink);
		                	  item.setLocimagelink(locimagelink);
		                	  
		                	  rspxmlpic.listAdd(item);
		                	  count++;
		                  }  
		                  break;  
	              }  
	              eventType = parser.next(); 
	              if(count>=9)
	              {
	            	  eventType = XmlPullParser.END_DOCUMENT;
	              }
	          }  
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return null;
		}
		 return rspxmlpic;
		 
	}
	private RspXmlTq parseXmltq(String data)
	{
		String low = null;
		String high = null;
		String date = null;
		String days = null;
		String week = null;
		String daydescription = null;
		String nightdescription = null;
		String description = null;
		String wind = null;
		//String dayicon = null;
		//String nighticon = null;
		String address = null;
		
		RspXmlTq rspxmltq=null;
		
		XmlPullParser parser = Xml.newPullParser(); 
		try 
		{
			 parser.setInput(new StringReader(data));
			 
			 int eventType = parser.getEventType();  
			 
	        while (XmlPullParser.END_DOCUMENT != eventType) 
	        {  
	              switch (eventType) 
	              {  
		              case XmlPullParser.START_DOCUMENT:  
		            	  rspxmltq = new RspXmlTq();  
		                  break;  
		              case XmlPullParser.START_TAG:  
		            	  String tagName = parser.getName();
		                  if ("key".equals(tagName)) 
		                  {  
		                	 address = parser.nextText();
		                	 rspxmltq.setAddress(address);
		                  } 
		                  else if ("low".equals(tagName)) 
		                  {  
		                	  low = parser.nextText();
		                  } 
		                  else if ("high".equals(tagName)) 
		                  {  
		                	  high = parser.nextText();
		                  } 
		                  else if ("date".equals(tagName)) 
		                  {  
		                	  date = parser.nextText();
		                  }
		                  else if ("days".equals(tagName)) 
		                  {  
		                	  days = parser.nextText();
		                  } 
		                  else if ("week".equals(tagName)) 
		                  {  
		                	  week = parser.nextText();
		                  } 
		                  else if ("daydescription".equals(tagName)) 
		                  {  
		                	  daydescription = parser.nextText();
		                  } 
		                  else if ("nightdescription".equals(tagName)) 
		                  {  
		                	  nightdescription = parser.nextText();
		                  } 
		                  else if ("description".equals(tagName)) 
		                  {  
		                	  description = parser.nextText();
		                  } 
		                  else if ("wind".equals(tagName)) 
		                  {  
		                	  wind = parser.nextText();
		                  } 
		                  /*
		                  else if (parser.getName().equals("dayicon")) 
		                  {  
		                	  dayicon = parser.nextText();
		                  } 
		                  else if (parser.getName().equals("nighticon")) 
		                  {  
		                	  nighticon = parser.nextText();
		                  }
		                  */
		                  break;  
		              case XmlPullParser.END_TAG:  
		            	  String endName = parser.getName();
		                  if("day".equals(endName)) 
		                  {  
		                	  RspXmlTq.Item item = rspxmltq.new Item();
		                	  
		                	  item.setLow(low);
		                	  item.setHigh(high);
		                	  
		                	  item.setData(date);
		                	  item.setDaydescription(daydescription);
		                	  //item.setDayicon(dayicon);
		                	  item.setDays(days);
		                	  item.setDescription(description);
		                	  item.setNightdescription(nightdescription);
		                	  //item.setNighticon(nighticon);
		                	  item.setWeek(week);
		                	  item.setWind(wind);
		                	  
		                	  rspxmltq.listAdd(item);
		                  }  
		                  break;  
	              }  
	              eventType = parser.next();  
	          }  
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return null;
		}
		 return rspxmltq;
		 
	}
	private RspXmlNb parseXmlnb(String data)
	{
		String retState = null;
		String sourceType = null;
		String rankType = null;
		
		String shopName = null;//店名
		String star = null;//星级
		String avgPrice = null;//人均
		String distance = null;//距离
		String category = null;//类别
		
		String address = null;//店铺地址
		String coordUrl = null;//在搜狗地图上的位置
		String leftUrl = null;//到这里去 
		String gotoUrl = null;//从这里出发
		String phoneNo = null;//电话
		
		String dishTags = null;//推荐菜
		String shopUrl = null;//更多
		String score=null;//推荐值
		
		RspXmlNb rspxmlnb=null;
		
		XmlPullParser parser = Xml.newPullParser(); 
		try 
		{
			 parser.setInput(new StringReader(data));
//			 Log.d("DEBUG", "get the parser date = "+data);
			 
			 int eventType = parser.getEventType();  
			 
	        while (XmlPullParser.END_DOCUMENT != eventType) 
	        {  
	              switch (eventType) 
	              {  
		              case XmlPullParser.START_DOCUMENT:  
		            	  rspxmlnb = new RspXmlNb();  
		                  break;  
		              case XmlPullParser.START_TAG:  
		            	  String tagName = parser.getName();
//		            	  Log.d("DEBUG", "get the tagName = "+tagName);
		            	  
		            	  if ("retState".equals(tagName)) 
		                  {  
		            		  retState = parser.nextText();
		            		  rspxmlnb.setRetState(retState);
		                  }
		                  else if ("sourceType".equals(tagName)) 
		                  {  
		                	  sourceType = parser.nextText();
		                	  rspxmlnb.setSourceType(sourceType);
		                  }
		                  else if ("rankType".equals(tagName)) 
		                  {  
		                	  rankType = parser.nextText();
		                	  rspxmlnb.setRankType(rankType);
		                  }
		                  else if ("ShopName".equals(tagName)) 
		                  {  
		                	  shopName = parser.nextText();
		                  }
		                  else if ("Star".equals(tagName)) 
		                  {  
		                	  star = parser.nextText();
//		                	  Log.d("DEBUG", "get the parser xml the star = "+star);
		                	  String temstar = star.toUpperCase();
		                	  if ("NULL".equals(temstar)) star = "暂无";
//		                	  Log.d("DEBUG", "get the parser xml the star after process = "+star);
		                  }
		                  else if ("AvgPrice".equals(tagName)) 
		                  {  
		                	  avgPrice = parser.nextText();
//		                	  Log.d("DEBUG", "get the parser xml the avgPrice = "+avgPrice);
		                	  String temavgPrice = avgPrice.toUpperCase();
		                	  if ("NULL".equals(temavgPrice)) avgPrice = "暂无";
//		                	  Log.d("DEBUG", "get the parser xml the avgPrice after process = "+avgPrice);
		                  }
		                  else if ("Category".equals(tagName)) 
		                  {  
		                	  category = parser.nextText();
		                  }
		                  else if ("coord_url".equals(tagName)) 
		                  {  
		                	  coordUrl = parser.nextText();
		                  }
		                  else if ("left_url".equals(tagName)) 
		                  {  
		                	  leftUrl = parser.nextText();
		                  }
		                  else if ("goto_url".equals(tagName)) 
		                  {  
		                	  gotoUrl = parser.nextText();
		                  }
		                  else if ("PhoneNo".equals(tagName)) 
		                  {  
		                	  phoneNo = parser.nextText();
		                  }
		                  else if ("DishTags".equals(tagName) ) 
		                  {  
		                	  dishTags = parser.nextText();
		                  }
		                  else if ("shop_url".equals(tagName)) 
		                  {  
		                	  shopUrl = parser.nextText();
		                  }
		                  else if ("distance".equals(tagName)) 
		                  {  
		                	  distance = parser.nextText();
//		                	  Log.d("DEBUG", "get the parser xml the distance = "+distance);
		                	  String temdistance = distance.toUpperCase();
		                	  if ("NULL".equals(temdistance)) distance = "暂无";
//		                	  Log.d("DEBUG", "get the parser xml the distance after process = "+distance);
		                  }
		                  else if("score".equals(tagName))
		                  {
		                	  score = parser.nextText();
//		                	  Log.d("DEBUG", "get the parser xml the score = "+score);
		                	  String temscore = score.toUpperCase();
		                	  if ("NULL".equals(temscore)) avgPrice = "暂无";
//		                	  Log.d("DEBUG", "get the parser xml the score after process = "+score);
		                  }
		                  else if("Address".equals(tagName))
		                  {
		                	  address = parser.nextText();
//		                	  Log.d("DEBUG", "get the parser xml the Address = "+address);
		                  }
		                  break;  
		              case XmlPullParser.END_TAG:  
		            	  String endName = parser.getName();
		            	  
		                  if("item".equals(endName)) 
		                  {  
		                	  RspXmlNb.Item item = rspxmlnb.new Item();
		                	  
		                	  item.setShopName(shopName);
		                	  item.setStar(star);
		                	  item.setAvgPrice(avgPrice);
		                	  item.setDistance(distance);
		                	  item.setCategory(category);
		                	  
		                	  item.setAddress(address);
		                	  item.setCoordUrl(coordUrl);
		                	  item.setLeftUrl(leftUrl);
		                	  item.setGotoUrl(gotoUrl);
		                	  item.setPhoneNo(phoneNo);
		                	  
		                	  item.setDishTags(dishTags);
		                	  
		                	  item.setShopUrl(shopUrl);
		                	  item.setScore(score);
		                	  
		                	  rspxmlnb.listAdd(item);
		                  }  
//		                  Log.d("DEBUG", "get the parser xml the Address = "+address);
		                  break;  
	              }  
	              eventType = parser.next();  
	          }  
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return null;
		}
		 return rspxmlnb;
	}
	private RspXmlCxMsgLc parseXmlCxMsgLc(String data)
	{
		String wappagetype = "0";
		String url = null;
		String title = null;
		String content1 = null;
		String content2 = null;
		String linkurl = null;
		
		String name = null;
		String number = null;
		String sStation = null;
		String eStation = null;
		String sTime = null;
		String eTime = null;
		String fPrice = null;
		String sPrice = null;
		String spendTime = null;
		
		RspXmlCxMsgLc rspXmlGoMsgLc=null;
		
		XmlPullParser parser = Xml.newPullParser(); 
		try 
		{
			 parser.setInput(new StringReader(data));
			 
			 int eventType = parser.getEventType();  
			 
	        while (XmlPullParser.END_DOCUMENT != eventType) 
	        {  
	              switch (eventType) 
	              {  
		              case XmlPullParser.START_DOCUMENT:  
		            	  rspXmlGoMsgLc = new RspXmlCxMsgLc();  
		                  break;  
		              case XmlPullParser.START_TAG:  
		            	  
		            	  String tagName = parser.getName();
		            	  
		            	  if("wappagetype".equals(tagName)) 
		                  {  
		            		  wappagetype = parser.nextText();
		                  }
		            	  
		            	  if("5".equals(wappagetype))
		            	  {
		            		  
		            		  if ("url".equals(tagName)) 
			                  {  
		            			  url = parser.nextText();
			                  }
		            		  else if ("title".equals(tagName)) 
			                  {  
		            			  title = parser.nextText();
			                  }
		            		  else if ("content1".equals(tagName)) 
			                  {  
		            			  content1 = parser.nextText();
			                  }
		            		  else if ("content2".equals(tagName)) 
			                  {  
		            			  content2 = parser.nextText();
			                  }
		            		  else if ("link".equals(tagName)) 
			                  {  
		            			   linkurl = parser.getAttributeValue(null, "linkurl");
			                  }
		            	  }
		                  break;  
		              case XmlPullParser.END_TAG:  
		            	  
		            	  String endName = parser.getName();
		                  if("display".equals(endName) && "5".equals(wappagetype)) 
		                  {  
		                	  RspXmlCxMsgLc.Item item = rspXmlGoMsgLc.new Item();
		                	
		                	  int sIndex = 0,eIndex = 0;
		                	  // 运行时间:4小时44分 票价:二等座206.0 一等座247.0
		                	  String temp = title.trim();  //title =D11_北京-沈阳北_动车组_铁友网
		                	  //number
		                	  eIndex = temp.indexOf("_");
		                	  if(eIndex>0)
		                	  {
		                		  number  = temp.substring(sIndex, eIndex);
		                		  sIndex = eIndex+1;
		                		  temp = temp.substring(sIndex);
		                		  item.setNumber(number);
		                	  }
		                	  
		                	  //sStation
		                	  eIndex = temp.indexOf("-");
		                	  if(eIndex>0)
		                	  {
		                		  sStation  = temp.substring(0,eIndex);
		                		  sIndex = eIndex+1;
		                		  temp = temp.substring(sIndex);
		                		  item.setsStation(sStation);
		                	  }
		                	  
		                	//eStation
		                	  eIndex = temp.indexOf("_");
		                	  if(eIndex>0)
		                	  {
		                		  eStation  = temp.substring(0, eIndex);
		                		  sIndex = eIndex+1;
		                		  temp = temp.substring(sIndex);
		                		  item.seteStation(eStation);
		                	  }
		                	  
		                	//name
		                	  eIndex = temp.indexOf("_");
		                	  if(eIndex>0)
		                	  {
		                		  name  = temp.substring(0, eIndex);
		                		  //sIndex = eIndex+1;
		                		 // temp = temp.substring(sIndex);
		                		  item.setName(name);
		                	  }
		                	  
		                	  temp = content1.trim();// content1 =发车:12:40 到达:当天 17:24 
		                	//sTime
		                	  sIndex = temp.indexOf("发车:");
		                	  eIndex = temp.indexOf(" ");
		                	  if(sIndex>=0 && eIndex>=0)
		                	  {
		                		  sIndex += "发车:".length();
		                		  
		                		  sTime  = temp.substring(sIndex, eIndex);
		                		  item.setsTime(sTime);
		                	  }
		                	  
		                	//eTime
		                	  sIndex = temp.indexOf("到达:");
		                	  if(sIndex>=0)
		                	  {
		                		  sIndex += "到达:".length();
		                		  eTime  = temp.substring(sIndex);
		                		  item.seteTime(eTime);
		                	  }
		                	  
	                	  	  temp = content2.trim(); // content2 =运行时间:4小时44分 票价:二等座206.0 一等座247.0 
	                	  		//sTime
		                	  sIndex = temp.indexOf("运行时间:");
		                	  eIndex = temp.indexOf(" ");
		                	  if(sIndex>=0 && eIndex>=0)
		                	  {
		                		  sIndex += "运行时间:".length();
		                		  spendTime  = temp.substring(sIndex, eIndex);
		                		  item.setSpendTime(spendTime);
		                		  temp = temp.substring(eIndex+1);
		                	  }
		                	  // fPrice
		                	  sIndex = temp.indexOf("票价:");
		                	  eIndex = temp.indexOf(" ");
		                	  if(sIndex>=0 && eIndex>=0)
		                	  {
		                		  sIndex += "票价:".length();
		                		  fPrice  = temp.substring(sIndex, eIndex);
		                		// sPrice
		                		  sPrice =temp.substring(eIndex+1);
		                		  item.setsPrice(sPrice);
		                		  item.setfPrice(fPrice);
		                	  }
		                	  
		                			  
		                	  item.setLinkurl(linkurl);
		                	  item.setUrl(url);
		                	  rspXmlGoMsgLc.listAdd(item);
		                	  wappagetype="0";
		                  }  
		                  
		                  break;  
	              }  
	              eventType = parser.next();  
	          }  
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return null;
		}
		 return rspXmlGoMsgLc;
	}
	private RspXmlCxLc parseXmlCxLc(String data)
	{
		String wappagetype = "0";
		
		String col0 = null;//name
		String col0link = null;//link
		String col1 = null;//station
		String col2 = null;//time
		String col3 = null;//price
		String col4 = null;//alltime
		String col5 = null;//dingpiao
		String col5link = null;//dingpiao url
		String linkurl = null; //link linkurl
		

		String sStation = null;
		String eStation = null;
		String sTime = null;
		String eTime = null;
		String fPrice = null;
		String sPrice = null;
		String spendTime = null;
		String booking = null;
		
		RspXmlCxLc rspXmlCxLc=null;
		
		XmlPullParser parser = Xml.newPullParser(); 
		try 
		{
			 parser.setInput(new StringReader(data));
			 
			 int eventType = parser.getEventType();  
			 
	        while (XmlPullParser.END_DOCUMENT != eventType) 
	        {  
	              switch (eventType) 
	              {  
		              case XmlPullParser.START_DOCUMENT:  
		            	  rspXmlCxLc = new RspXmlCxLc();  
		                  break;  
		              case XmlPullParser.START_TAG:  
		            	  
		            	  String tagName = parser.getName();
		            	  
		            	  if("wappagetype".equals(tagName)) 
		                  {  
		            		  wappagetype = parser.nextText();
		                  }
		            	  
		            	  if("5".equals(wappagetype))
		            	  {
		            		  
		            		  if ("form".equals(tagName)) 
			                  {  
		            			  int sIndex = -1;
		            			  RspXmlCxLc.Item item = rspXmlCxLc.new Item();
			                	  
		            			  col0 = parser.getAttributeValue(null, "col0");
		            			  col0link = parser.getAttributeValue(null, "col0link");
		            			  col1 = parser.getAttributeValue(null, "col1");
		            			  col2 = parser.getAttributeValue(null, "col2");
		            			  col3 = parser.getAttributeValue(null, "col3");
		            			  col4 = parser.getAttributeValue(null, "col4");
		            			  col5 = parser.getAttributeValue(null, "col5");
		            			  col5link = parser.getAttributeValue(null, "col5link");
		            			  
		            			  //name
			                	  String temp = col0.trim(); 
			                	  item.setName(col0);
			                	 
			                	  //linkurl
			                	  item.setLinkurl(col0link);
			                	  
			                	  //sStation,eStation
			                	  temp = col1.trim();
			                	  sIndex = temp.indexOf("-");
			                	  if(sIndex>0)
			                	  {
			                		  sStation = temp.substring(0,sIndex);
			                		  eStation = temp.substring(sIndex+1);
			                		  
			                		  item.seteStation(eStation);
			                		  item.setsStation(sStation);
			                	  }
			                	  
			                	  //sTime,eTime
			                	  temp = col2.trim();
			                	  sIndex = temp.indexOf("-");
			                	  if(sIndex>0)
			                	  {
			                		  sTime = temp.substring(0,sIndex);
			                		  eTime = temp.substring(sIndex+1);
			                		  item.seteTime(eTime);
					                  item.setsTime(sTime);
			                	  }
			                	  
			                	  //fPrice sPrice
			                	  temp = col3.trim();
			                	  sIndex = temp.indexOf("  ");
			                	  if(sIndex>0)
			                	  {
			                		  fPrice = temp.substring(0,sIndex);
			                		  sPrice = temp.substring(sIndex+1);
			                		  item.setfPrice(fPrice);
					                  item.setsPrice(sPrice);
			                	  }
			                	  
			                	  //fPrice sPrice
			                	  temp = col4.trim();
			                	  spendTime = temp;
			                	  item.setSpendTime(spendTime);
			                	  
			                	  //booking
			                	  booking = col5link;
			                	  item.setBooking(booking);
			                	  
			                	  rspXmlCxLc.listAdd(item);
			                  }
		            		  else if("link".equals(tagName))
		            		  {
		            			  linkurl = parser.getAttributeValue(null, "linkurl");
		            			  rspXmlCxLc.setAllurl(linkurl);
		            		  }
		            	  }
		                  break;  
		              case XmlPullParser.END_TAG:  
		            	  
		            	  String endName = parser.getName();
		            	  
		                  if("display".equals(endName) && "5".equals(wappagetype)) 
		                  {  
		                	  /*
            	  			col1="济南-北京南" 
            				col1link="" 
            				col2="07:12-当天09:30" 
            				col2link="" 
            				col3="二等座 129.5  一等座 209.5" 
            				col3link="" 
            				col4="2小时18分" 
            				col5="订" 
		                	*/
		                	  
		                  }
		                  break;  
	              }  
	              eventType = parser.next();  
	          }  
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return null;
		}
		 return rspXmlCxLc;
	}
	private RspXmlCxKc parseXmlCxKc(String data)
	{
		String linkurl = null;
		String linkcontent = null;
		String col0 = null;
		String col1 = null;
		String col2 = null;
		boolean isSet = false;
		
		RspXmlCxKc rspXmlcxkc=null;
		
		XmlPullParser parser = Xml.newPullParser(); 
		try 
		{
			 parser.setInput(new StringReader(data));
			 
			 int eventType = parser.getEventType();  
			 
	        while (XmlPullParser.END_DOCUMENT != eventType) 
	        {  
	              switch (eventType) 
	              {  
		              case XmlPullParser.START_DOCUMENT:  
		            	  rspXmlcxkc = new RspXmlCxKc();  
		                  break;  
		              case XmlPullParser.START_TAG:  
		            	  
		            	  String tagName = parser.getName();
		            	  
		            	  if("form".equals(tagName)) 
		                  {  
		            		  col0 = parser.getAttributeValue(null, "col0");
		            		  col1 = parser.getAttributeValue(null, "col1");
		            		  col2 = parser.getAttributeValue(null, "col2");
		            		  
		            		  RspXmlCxKc.Item item = rspXmlcxkc.new Item();
		            		  item.setCol0(col0);
		            		  item.setCol1(col1);
		            		  item.setCol2(col2);
		            		  rspXmlcxkc.listAdd(item);
		                  }
		            	  
		            	  if(!isSet && "link".equals(tagName)) 
		                  {  
		            		  linkurl = parser.getAttributeValue(null, "linkurl");
		            		  linkcontent = parser.getAttributeValue(null, "linkcontent");
		            		  isSet = true;
		                  }
		                  break;  
		              case XmlPullParser.END_TAG:  
		            	  
		            	  String endName = parser.getName();
		            	  if("display".equals(endName)) 
		                  {  
		            		  rspXmlcxkc.setLinkcontent(linkcontent);
		            		  rspXmlcxkc.setLinkurl(linkurl);
		                  }
		                  
		                  break;  
	              }  
	              eventType = parser.next();  
	          }  
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return null;
		}
		 return rspXmlcxkc;
	}
	private RspXmlCxHb parseXmlCxHb(String data,String date)
	{
		String linkurl = null;
		
        
		RspXmlCxHb rspXmlcxhb=null;
		
		XmlPullParser parser = Xml.newPullParser(); 
		try 
		{
			 parser.setInput(new StringReader(data));
			 
			 int eventType = parser.getEventType();  
			 
	        while (XmlPullParser.END_DOCUMENT != eventType) 
	        {  
	              switch (eventType) 
	              {  
		              case XmlPullParser.START_DOCUMENT:  
		            	  rspXmlcxhb = new RspXmlCxHb();  
		                  break;  
		              case XmlPullParser.START_TAG:  
		            	  String tagName = parser.getName();
		          		
		            	  if ("url".equals(tagName)) 
		                  {  
		            		  linkurl = parser.nextText();
		                  } 
		                  
		                  break;  
		              case XmlPullParser.END_TAG:  
		            	  
		            	  String endName = parser.getName();
		            	  
		            	  if("display".equals(endName)) 
		                  {  
		            		  String y = date.substring(0,4);
		            		  String m = date.substring(4,6);
		            		  String d = date.substring(6,8);
		            		  
		            		  linkurl += (y+"-"+m+"-"+d);
		            		  rspXmlcxhb.setLinkUrl(linkurl);
		                  }
		                  
		                  break;  
	              }  
	              eventType = parser.next();  
	          }  
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return null;
		}
		 return rspXmlcxhb;
	}
	private RspXmlBk parseXmlbk(String data)
	{
		String content=null;
		String moreUrl=null;
		boolean isSet =false;
		
		RspXmlBk rspxmlbk=null;
		
		XmlPullParser parser = Xml.newPullParser(); 
		try 
		{
			 parser.setInput(new StringReader(data));
			 
			 int eventType = parser.getEventType();  
			 
	        while (XmlPullParser.END_DOCUMENT != eventType) 
	        {  
	              switch (eventType) 
	              {  
		              case XmlPullParser.START_DOCUMENT:  
		            	  rspxmlbk = new RspXmlBk();  
		                  break;  
		              case XmlPullParser.START_TAG:  
		            	  String tagName = parser.getName();
		            	  
		            	  if ("content".equals(tagName)) 
		                  {  
		            		  content = parser.nextText();
		                  } 
		                  else if (!isSet && "link1".equals(tagName)) 
		                  {  
		                	   moreUrl = parser.getAttributeValue(null, "linkurl");
		                	   isSet = true;
		                  } 
		            	  
		                  break;  
		              case XmlPullParser.END_TAG:  
		            	  String endName = parser.getName();
		            	  
		                  if("display".equals(endName)) 
		                  {  
		                	  rspxmlbk.setContent(content);
		                	  rspxmlbk.setMoreUrl(moreUrl);
		                	  return rspxmlbk;
		                  }  
		                  break;  
	              }  
	              eventType = parser.next();  
	          }  
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return null;
		}
		 return rspxmlbk;
	}
	
	public Response execu() {
		String tmp;
		//boolean isXml =false;
		String resultType=null;
		JSONArray jsonDataArray = null;
		
		if(null == data) return null;
		
		Iterator<String> iter = data.keys();
		
		rspData = new Response();
		//解析公共部分
		while(iter.hasNext()) {
			String key = iter.next();
			
			try {
				if(CFG.REAL_POINT.equals(key)) {
					String realPoint = data.optString(key);
					realPoint = realPoint.replace("\n", "");
					rspData.setRealPoint(realPoint);
				} else if(CFG.SYS_TIME.equals(key)) {
					String sysTime = data.optString(key);
					sysTime = sysTime.replace("\n", "");
					rspData.setSysTime(sysTime);
				} else if(CFG.CITY.equals(key)) {
					String city = data.optString(key);
					city = city.replace("\n", "");
					rspData.setCity(city);
				}
				Log.d("DEBUG", "the Response execu() rspData = "+rspData);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
		if(data.has(CFG.FINAL_RESULT)) {
			try {
				jsonDataArray = data.getJSONArray(CFG.FINAL_RESULT);
				Log.d("DEBUG", "the Response execu() CFG.FINAL_RESULT = "+jsonDataArray);
			} catch (Exception e) {
				e.printStackTrace();
				jsonDataArray = null;
				return null;
			}
		}
		
		//解析result
		if (null == jsonDataArray) {
			return null;
		}
		Log.d("DEBUG", "the Response execu() jsonDataArray.length = "+jsonDataArray.length());
		boolean skipFlag = false;
		for (int i = 0; i < jsonDataArray.length(); i++) {
			try {
				JSONObject obj = (JSONObject)(jsonDataArray.get(i));
				resultType = obj.optString(CFG.RESULT_TYPE);
//				if(null == resultType || 0 == resultType.length()) return null;
				Log.d("DEBUG", "the Response execu() JSONObject  = "+obj);
				
				if (resultType == null || resultType.length() == 0) {
					resultType = CFG.RESULTTYPE_JSON;
				}
				
				resultType = resultType.trim();
				Log.d("DEBUG", "the Response execu() jsonDataArray get resultType = "+resultType);
				String description = obj.optString(CFG.DESCRIPTION);
				if(0 == i) {
					rspData.setFirstNodeType(rspData.getResultType(resultType,description));
					Log.d("DEBUG", "the Response execu() setFirstNodeType = "+rspData.getResultType(resultType,description));
				}
				
				//check目前是否不支持的数据类型
				if(//CFG.RESULTTYPE_JSON.equals(resultType) ||
						(CFG.RESULTTYPE_XML.equals(resultType) && !description.equals(CFG.TAB_TYPE_XML_TUPIAN)
															   && !description.equals(CFG.TAB_TYPE_XML_TIANQI)
															   && !description.equals(CFG.TAB_TYPE_XML_NB_SH)
															   && !description.equals(CFG.TAB_TYPE_XML_NB_CS)
															   && !description.equals(CFG.TAB_TYPE_XML_CX_MSG_LC)
															   && !description.equals(CFG.TAB_TYPE_XML_CX_KC)
															   && !description.equals(CFG.TAB_TYPE_XML_CX_LC)
															  // && !description.equals(CFG.TAB_TYPE_XML_CX_MSG_HB)
															   && !description.equals(CFG.TAB_TYPE_XML_CX_HB)
															   && !description.equals(CFG.TAB_TYPE_XML_BAIKE)) ) {
					skipFlag =true;
					continue;
				}
				if(1 == i && true == skipFlag) {
					rspData.setFirstNodeType(rspData.getResultType(resultType,description));
				}
				
				Response.Item item = new Response().new Item();

				if(CFG.RESULTTYPE_TEXT.equals(resultType)) {	//type=text
					RspText resText= new RspText();
					
					tmp = obj.optString(CFG.ANSWER);
					if(null == tmp) return null;
					tmp = tmp.replace("\n", "");
					
					resText.setAnswer(tmp);
					item.setRestext(resText);
					item.setType(CFG.VIEW_TYPE_TEXT);
					
				} else if(CFG.RESULTTYPE_WEBVIEW.equals(resultType)) {	//type=webview
					String searchContent = obj.optString(CFG.SEARCH_CONTENT);
					if(null != searchContent && 0 != searchContent.length()) {
						rspData.setSearchContent(searchContent);
					}
					
					RspWebview webview = new RspWebview();
					
					//description
					tmp = obj.optString(CFG.DESCRIPTION);
					if(null == tmp) return null;
					tmp = tmp.replace("\n", "");
					webview.setDescription(tmp);
					
					//result_name
					tmp = obj.optString(CFG.RESULT_NAME);
					if(null == tmp) return null;
					tmp = tmp.replace("\n", "");
					webview.setResultName(tmp);
					
					//url
					if(CFG.WEBVIEW_URL.equals(tmp)) {
						tmp = obj.optString(CFG.WEBVIEW_URL);
						if(null == tmp) return null;
						tmp = tmp.replace("\n", "");
						
					} else if(CFG.SEARCH_URL.equals(tmp)) {
						tmp = obj.optString(CFG.SEARCH_URL);
						if(null == tmp) return null;
						tmp = tmp.replace("\n", "");
					}
					webview.setUrl(tmp);
					
					item.setRspwebview(webview);
					item.setType(CFG.VIEW_TYPE_WEBVIEW);
					
				} else if(CFG.RESULTTYPE_JSON.equals(resultType)) {		//type=josn
					
					RspJson rspJson = new RspJson(obj);
					item.setRspjson(rspJson);
					
					item.setType(CFG.VIEW_TYPE_JOSN);
					
				} else if(CFG.RESULTTYPE_XML.equals(resultType)) {		//type=xml
					String descri = obj.optString(CFG.DESCRIPTION);
					String keyword = obj.optString(CFG.SEARCH_CONTENT);
					Log.d("DEBUG", "the Response execu() CFG.RESULTTYPE_XML and the descri = "+descri);
					Log.d("DEBUG", "the Response execu() CFG.RESULTTYPE_XML and the SEARCH_CONTENT = "+obj.optString(CFG.SEARCH_CONTENT));
					
					String searchContent = obj.optString(CFG.SEARCH_CONTENT);
					if(null != searchContent && 0 != searchContent.length()) {
						rspData.setSearchContent(searchContent);
					}
					
					//xml_pic
					if(descri.equals(CFG.TAB_TYPE_XML_TUPIAN)) {
						RspXmlPic rspxmlpic = null;
						
						String xml = obj.optString(CFG.RESULT);
						if(null == xml || 0 == xml.length()) {
							continue;
						}
						
						rspxmlpic = parseXmlpic(xml);
						if(null == rspxmlpic) continue;
						rspxmlpic.setDescription(description);
						rspxmlpic.setMorelink(CFG.XMLPIC_MORELINK+keyword);
						item.setRspxmlpic(rspxmlpic);
						item.setType(CFG.VIEW_TYPE_XML_PIC);
						Log.d("DEBUG", "the Response execu() CFG.RESULTTYPE_XML descri.equals(CFG.TAB_TYPE_XML_TUPIAN) = ");
					} else if(descri.equals(CFG.TAB_TYPE_XML_TIANQI)) {
						RspXmlTq rspXmltq = null;
						String xml = obj.optString(CFG.RESULT);
						
						if(null == xml || 0 == xml.length()) {
							continue;
						}
						
						rspXmltq = parseXmltq(xml);
						if(null == rspXmltq) continue;
						item.setRspxmltq(rspXmltq);
						item.setType(CFG.VIEW_TYPE_XML_TIANQI);
					} else if(descri.equals(CFG.TAB_TYPE_XML_NB_SH) || descri.equals(CFG.TAB_TYPE_XML_NB_CS)) {
						RspXmlNb rspXmlnb = null;
						String resultName = obj.optString(CFG.RESULT_NAME);
						String xml =null;
						if(resultName!=null) {
							xml = obj.optString(resultName);
						} else {
							xml = obj.optString(CFG.RESULT);
						}
						Log.i("test", "xml = "+xml);
						if(null == xml || 0 == xml.length()) {
							continue;
						}
						
						rspXmlnb = parseXmlnb(xml);
						if(null == rspXmlnb) continue;
						
						if (descri.equals(CFG.TAB_TYPE_XML_NB_CS)) {
							rspXmlnb.setSourceType("20001");
						}
						item.setRspxmlnb(rspXmlnb);
						item.setType(CFG.VIEW_TYPE_XML_NB);
					} else if(descri.equals(CFG.TAB_TYPE_XML_CX_MSG_LC)) {
						RspXmlCxMsgLc rspXmlGoMsgLc = null;
						String xml = obj.optString(CFG.RESULT);
						
						if(null == xml || 0 == xml.length()) {
							continue;
						}
						
						rspXmlGoMsgLc = parseXmlCxMsgLc(xml);
						if(null == rspXmlGoMsgLc) continue;
						item.setRspxmlcxmsglc(rspXmlGoMsgLc);
						item.setType(CFG.VIEW_TYPE_XML_CX_MSG_LC);
					} else if(descri.equals(CFG.TAB_TYPE_XML_CX_LC)) {
						RspXmlCxLc rspXmlCxLc = null;
						String xml = obj.optString(CFG.RESULT);
						
						if(null == xml || 0 == xml.length()) {
							continue;
						}
						
						rspXmlCxLc = parseXmlCxLc(xml);
						if(null == rspXmlCxLc) continue;
						rspXmlCxLc.setTrip_from( obj.optString("trip_from"));
						rspXmlCxLc.setTrip_to(obj.optString("trip_to"));
						item.setRspxmlcxlc(rspXmlCxLc);
						item.setType(CFG.VIEW_TYPE_XML_CX_LC);
					} else if(descri.equals(CFG.TAB_TYPE_XML_CX_KC)) {
						RspXmlCxKc rspXmlcxkc = null;
						String xml = obj.optString(CFG.RESULT);
						String trip_from = obj.optString("trip_from");
						String trip_tod = obj.optString("trip_to");
						
						if(null == xml||0 == xml.length()||null==trip_from||0 == trip_from.length()||null==trip_tod||0 == trip_tod.length()) {
							continue;
						}
						
						rspXmlcxkc = parseXmlCxKc(xml);
						if(null == rspXmlcxkc) continue;
						rspXmlcxkc.setTrip_from(trip_from);
						rspXmlcxkc.setTrip_tod(trip_tod);
						
						item.setRspxmlcxkc(rspXmlcxkc);
						item.setType(CFG.VIEW_TYPE_XML_CX_KC);
					}
					/*
					else if(descri.equals(CFG.TAB_TYPE_XML_CX_MSG_HB))
					{
						RspXmlCxMsgHb rspXmlCxMsgHb = null;
						String xml = obj.optString(CFG.RESULT);
						
						if(null==xml||0 == xml.length())
						{
							continue;
						}
						
						rspXmlCxMsgHb = parseXmlCxMsgHb(xml);
						if(null == rspXmlCxMsgHb) continue;
						rspXmlCxMsgHb.setTrip_from( obj.optString("trip_from"));
						rspXmlCxMsgHb.setTrip_to(obj.optString("trip_to"));
						item.setRspxmlcxmsghb(rspXmlCxMsgHb);
						item.setType(CFG.VIEW_TYPE_XML_CX_MSG_HB);
					}*/
					else if(descri.equals(CFG.TAB_TYPE_XML_CX_HB)) {
						RspXmlCxHb rspXmlCxHb = null;
						String xml = obj.optString(CFG.RESULT);
						
						if(null == xml || 0 == xml.length()) {
							continue;
						}
						
						rspXmlCxHb = parseXmlCxHb(xml,rspData.getSysTime());
						if(null == rspXmlCxHb) continue;
						item.setRspxmlcxhb(rspXmlCxHb);
						item.setType(CFG.VIEW_TYPE_XML_CX_HB);
					} else if(descri.equals(CFG.TAB_TYPE_XML_BAIKE)) {
						RspXmlBk rspXmlBk = null;
						String xml = obj.optString(CFG.RESULT);
						String key = obj.optString("key");
						
						if(null == xml || 0 == xml.length() || null==key || 0 == key.length()) {
							continue;
						}
						
						rspXmlBk = parseXmlbk(xml);
						if(null == rspXmlBk) continue;
						rspXmlBk.setKey(key);
						item.setRspxmlbk(rspXmlBk);
						item.setType(CFG.VIEW_TYPE_XML_BAIKE);
					} else {
						continue;
					}
					
				} else if (CFG.RESULTTYPE_SOGOUMAP_URL.equals(resultType)) {	//type: sogoumap_url
					Log.d("DEBUG", "CFG.RESULTTYPE_SOGOUMAP_URL "+resultType);
					/*String searchContent = obj.optString(CFG.SEARCH_CONTENT);
					if(null != searchContent && 0 != searchContent.length()) {
						rspData.setSearchContent(searchContent);
					}*/
					
					
					RspWebview webview = new RspWebview();
					
					//description
					tmp = obj.optString(CFG.DESCRIPTION);
					if(null == tmp) return null;
					tmp = tmp.replace("\n", "");
					webview.setDescription(tmp);
					
					//result_name
					tmp = obj.optString(CFG.RESULT_NAME);
					if(null == tmp) return null;
					tmp = tmp.replace("\n", "");
					webview.setResultName(tmp);
					
					//get url
					tmp = obj.optString(tmp);
					if(null == tmp) return null;
					tmp = tmp.replace("\n", "");
					
					webview.setUrl(tmp);
					
					item.setRspwebview(webview);
//					item.setType(CFG.VIEW_TYPE_WEBVIEW);
					item.setType(CFG.VIEW_TYPE_SOGOUMAP_URL);
				}
				
				rspData.listAdd(item);
				
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
		return rspData;
	}
}
