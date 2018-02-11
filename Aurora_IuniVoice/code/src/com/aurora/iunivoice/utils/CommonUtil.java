package com.aurora.iunivoice.utils;

import android.content.Context;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.aurora.iunivoice.IuniVoiceApp;

public class CommonUtil {

	private static final String TAG = "CommonUtil";

	public static int getResInteger(int integerResId) {
		return IuniVoiceApp.getInstance().getResources()
				.getInteger(integerResId);
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

   /**
	* @Title: isSoftInputOpen
	* @Description: TODO 获取输入法是否已经打开
	* @param @param context
	* @param @return
	* @return boolean
	* @throws
	 */
	public static boolean isSoftInputOpen(Context context) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		boolean isOpen = imm.isActive();// isOpen若返回true，则表示输入法打开
		return isOpen;
	}
	
   /**
	* @Title: hideSoftInput
	* @Description: TODO 隐藏软键盘
	* @param @param context
	* @param @param editText
	* @return void
	* @throws
	 */
	public static void hideSoftInput(Context context, EditText editText) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0); //强制隐藏键盘  
	}

}
