package com.aurora.community.utils;

public class StringUtils {

	
    public static String getMonth(String str){
    	String result = new String();
    	String[] arrayString = str.split(" ");
    	if(arrayString[1]!=null){
    		result = arrayString[1];
    	}
    	return result;
    }
	
}
