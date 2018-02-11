package com.gionee.settings.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

public class CountUtil {
	
	private static CountUtil mCountUtil;
	private Context mContext;
	private final String SETTING_KEY = "110";
	
	private CountUtil(Context context){
		this.mContext = context;
	}
	public static CountUtil getInstance(Context context){
		if(null == mCountUtil){
			mCountUtil = new CountUtil(context);
		}
		return mCountUtil;
	}
	
	public void update(String Tag,int Value){
		new TotalCount(mContext, SETTING_KEY, Tag, Value).CountData();
	}

}
