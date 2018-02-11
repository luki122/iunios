package com.aurora.community.utils;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.aurora.community.R;
/**
 * 获取默认值的工具类
 * @author j
 *
 */
public class DefaultUtil {

	/**
	 * 显示的图片默认图
	 * @return
	 */
	public static int getDefaultImageRes(){
		return R.drawable.common_defalt_bg;
	}
   
	/**
	 * 用户头像默认图
	 * @return
	 */
	public static int getUserDeaultIconRes(){
		return R.drawable.default_user_icon;
	}
	
	public static Drawable getDefaultUserDrawable(Context context){
		return context.getResources().getDrawable(getUserDeaultIconRes());
	}
	
	public static Drawable getDefaultImageDrawable(Context context){
		return new ColorDrawable(0xffe2e2e2);
	}
	
	public static boolean checkNetWork(Context context){
		ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
		boolean isEnable = false;
		if (mNetworkInfo != null) {
			isEnable = mNetworkInfo.isAvailable();
		}
		if(!isEnable)
		{
			Toast.makeText(context, R.string.net_error, 200).show();
		}
		return isEnable;
		
	}
	
}
