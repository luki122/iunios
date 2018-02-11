package com.aurora.community.utils;

import android.text.TextUtils;

import com.aurora.community.CommunityApp;



public class CommonUtil {
	
    private static final String TAG = "CommonUtil";
    
    public static int getResInteger(int integerResId) {
	    return CommunityApp.getInstance().getResources().getInteger(integerResId);
	}
    
    /**
	* @Title: hidePhoneNum
	* @Description: TODO 隐藏手机号中间部分，如18*******5
	* @param @param phoneNum
	* @param @return
	* @return String
	* @throws
	 */
	public static String hidePhoneNum(String phoneNum) {
		if (!TextUtils.isEmpty(phoneNum) && phoneNum.length() > 2) {
			StringBuffer sb = new StringBuffer();
			sb.append(phoneNum.substring(0, 2));
			sb.append("******");
			sb.append(phoneNum.charAt(phoneNum.length() - 1));
			return sb.toString();
		}
		return phoneNum;
	}
	
	/**
	* @Title: hideEmail
	* @Description: TODO 隐藏email中间部分，如zzyan387@163.com
	* @param @param email
	* @param @return
	* @return String
	* @throws
	 */
	public static String hideEmail(String email) {
		int index = email.indexOf('@');
		if (index != -1) {
			StringBuffer sb = new StringBuffer();
			sb.append(email.charAt(0));
			sb.append("******");
			if (index > 1) {
				sb.append(email.substring(index - 1, email.length()));
			} else {
				sb.append(email.substring(index, email.length()));
			}
			return sb.toString();
		}
		return email;
	}
	
}
