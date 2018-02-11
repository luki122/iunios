package com.aurora.util;

public class StringUtils {
	public static boolean isEmpty(String str){
		if(str == null || str.trim().equals("")) return true;
		return false;
	}
}
