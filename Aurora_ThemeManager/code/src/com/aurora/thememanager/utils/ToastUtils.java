package com.aurora.thememanager.utils;

import android.content.Context;
import android.widget.Toast;
import com.aurora.thememanager.R;
public class ToastUtils {
	
	
	/**
	 * 显示相应的Toast
	 * @param context
	 * @param textRes
	 */
	public static void showToast(Context context,int textRes){
		
		Toast.makeText(context, context.getResources().getText(textRes), Toast.LENGTH_SHORT).show();
		
	}
	
	/**
	 * 显示操作成功的Toast
	 * @param context
	 */
	public static void showApplySuccessToast(Context context){
		Toast.makeText(context, context.getResources().getText(R.string.apply_success), Toast.LENGTH_SHORT).show();
	}

}
