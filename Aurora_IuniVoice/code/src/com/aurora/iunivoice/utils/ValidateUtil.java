package com.aurora.iunivoice.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;

import com.aurora.iunivoice.R;

/**
 * 验证帐号的有效性的工具类
 * 
 * @author JimXia 2014-9-23 上午10:22:29
 */
public class ValidateUtil {

	public static boolean isAccountValid(String account) {
		if (TextUtils.isDigitsOnly(account)) {
			// 只有数字，把输入的参数当手机号来校验
			return isMobilePhoneNumVaild(account);
		} else {
			// 数字和其他字符混合，把输入的参数当邮箱帐号来校验
			return isEmailValid(account);
		}
	}

	public static boolean isMobilePhoneNumVaild(String mobilePhoneNumber) {
		boolean flag = false;
		try {
			Pattern p = Pattern
					.compile("^((13[0-9])|(15[^4,\\D])|(18[0-9]))\\d{8}$");
			Matcher m = p.matcher(mobilePhoneNumber);
			flag = m.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}

	public static boolean isEmailValid(String email) {
		/*
		 * boolean flag = false; try { String check =
		 * "^([a-z0-9A-Z]+[_|-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$"
		 * ; Pattern regex = Pattern.compile(check); Matcher matcher =
		 * regex.matcher(email); flag = matcher.matches(); } catch (Exception e)
		 * { flag = false; }
		 * 
		 * return flag;
		 */
		boolean flag = false;
		try {
			String regex = "^([\\w-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([\\w-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";

			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(email);
			flag = matcher.matches();
		} catch (Exception e) {
			flag = false;
		}

		return flag;
	}

	private static final Pattern sPwdPattern = Pattern
			.compile("^(?![^a-zA-Z]+$)(?!\\D+$).{"
					+ CommonUtil.getResInteger(R.integer.password_min_length)
					+ ","
					+ CommonUtil.getResInteger(R.integer.password_max_length)
					+ "}$");

	public static boolean isCorrectFormatPwd(String pwd) {
		boolean flag = false;
		try {
			Matcher matcher = sPwdPattern.matcher(pwd);
			flag = matcher.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}
	
	/**
	 * 统计字符串的长度，英文字符一个算一个，中文字符一个算两个
	 * @param str
	 * @return
	 */
	public static int getCharCount(String str) {
	    if (TextUtils.isEmpty(str)) {
	        return 0;
	    }
	    
	    final int realLength = str.length();
        int length = 0;
        for (int i = 0; i < realLength; i++) {
            int temp = str.charAt(i);
            if (temp > 0 && temp < 127) {
                length ++;
            } else {
                length += 2;
            }
        }
        
        return length;
	}
}
