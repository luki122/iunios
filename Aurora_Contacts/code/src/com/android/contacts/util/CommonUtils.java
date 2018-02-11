package com.android.contacts.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class CommonUtils {
	public static String StringFilter(String str)   throws   PatternSyntaxException   {      
		// 只允许字母和数字        
		// String   regEx  =  "[^a-zA-Z0-9]";
		// 清除掉所有特殊字符
		String regEx = "[^0-9*,#+]";
//		String regEx="[`~!@#$%^&*()+-=|{}':;',//[//].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]P";
		Pattern   p   =   Pattern.compile(regEx);     
		Matcher   m   =   p.matcher(str);     
		return   m.replaceAll("").trim();     
	} 
	

}
