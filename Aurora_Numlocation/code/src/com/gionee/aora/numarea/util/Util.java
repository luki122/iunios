package com.gionee.aora.numarea.util;

import android.widget.RemoteViews;

//import com.base.log.JLog;
import com.gionee.aora.numarea.R;

/**
 * Copyright (c) 2011, ÉîÛÚÊÐ°ÂÈíÍøÂç¿Æ¼¼¹«Ë¾ÑÐ·¢²¿
 * All rights reserved.
 *
 * @file AreaSearch.java
 * ÕªÒª:¹¤¾ßÀà
 *
 * @author jiangfan
 * @data 2011-7-6
 * @version v1.0.0
 *
 */
public class Util {
	/**
	 * ´òÓ¡logÀà
	 */
//	final static public JLog LOG = new JLog("NUM_AREA", true);
	
	/**
	 * ÇÐ³ý86
	 * @param inputNum
	 * @return
	 */
	public static String Cut_86_Num(String inputNum){
		try
		{	
			inputNum=inputNum.replaceAll("\\+86", "");
			return inputNum;
			}
		catch(Exception e)
		{
			// TODO: handle exception
			return inputNum;
		}
		
		
	}
	/**
	 * ÅÐ¶ÏÊäÈëÊÇÊý×Ö»¹ÊÇ×Ö·û´®
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str) {
		str=Cut_86_Num(str);
		for (int i = str.length(); --i >= 0;) {
			if (!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}
	/*public static void cutString( RemoteViews rv,String inputString){
		String fristString=null;
		String laString=null;
		if(inputString.toString().trim().length()>6){
			fristString=inputString.substring(0, 6);
			laString=inputString.substring(6,inputString.length());
		}
		else{
			fristString=inputString;
			laString="";
		}
		rv.setTextViewText(R.id.Text, fristString);
		rv.setTextViewText(R.id.Text2, laString);
	}*/
	/**
	 * É¾³ý¿Õ¸ñºÍ-·ûºÅ
	 * @param inpString
	 * @return
	 */
	public static String delete_String(String inpString){
		try
		{
			
			String string2=	inpString.replaceAll(" ", "");
			String string3=string2.replaceAll("-", "");
			return string3;
		}
		catch(Exception e)
		{
			return inpString;
			// TODO: handle exception
		}
	}
}
