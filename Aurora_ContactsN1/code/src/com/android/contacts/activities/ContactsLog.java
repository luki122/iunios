package com.android.contacts.activities;

import android.util.Log;

public class ContactsLog {
	private static boolean need_debug=true;
	public static  void log(String msg)
	{
	  if(need_debug)	
		Log.v("iunicontacts", msg);
	}
	public static void log(Object o,String msg)
	{
	  if(need_debug)	
		Log.v("iunicontacts"+o.getClass().getName(), msg);
	}
	
	public static void log(String cname,String msg)
	{
	  if(need_debug)	
		Log.v("iunicontacts"+cname, msg);
	}
	
	public static void logt(Object o,String msg)
	{
	  if(need_debug)	
		Log.v("iunicontacts"+o.getClass().getName(), msg+" time="+System.currentTimeMillis()%10000);
	}
	public static void logt(String cname,String msg)
	{
	  if(need_debug)	
		Log.v("iunicontacts"+cname, msg+" time="+System.currentTimeMillis()%10000);
	}
}
