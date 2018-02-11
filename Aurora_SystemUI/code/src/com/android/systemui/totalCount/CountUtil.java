package com.android.systemui.totalCount;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

public class CountUtil {
	
	private static CountUtil mCountUtil;
	private Context mContext;
	private final String SETTING_KEY = "230";

	private final boolean IS_SUPPORT_TOTALCOUNT = true;

	public static final String COUNT_ITEM_001 = "001";
	public static final String COUNT_ITEM_002 = "002";
	public static final String COUNT_ITEM_003 = "003";
	public static final String COUNT_ITEM_004 = "004";
	public static final String COUNT_ITEM_005 = "005";
	public static final String COUNT_ITEM_006 = "006";
	public static final String COUNT_ITEM_007 = "007";
	public static final String COUNT_ITEM_008 = "008";
	public static final String COUNT_ITEM_009 = "009";
	public static final String COUNT_ITEM_010 = "010";
	public static final String COUNT_ITEM_011 = "011";
	public static final String COUNT_ITEM_012 = "012";
	public static final String COUNT_ITEM_013 = "013";
	public static final String COUNT_ITEM_014 = "014";
	public static final String COUNT_ITEM_015 = "015";
	public static final String COUNT_ITEM_016 = "016";
	public static final String COUNT_ITEM_017 = "017";
	public static final String COUNT_ITEM_018 = "018";
	public static final String COUNT_ITEM_019 = "019";
	public static final String COUNT_ITEM_020 = "020";
	public static final String COUNT_ITEM_021 = "021";
	public static final String COUNT_ITEM_022 = "022";
	public static final String COUNT_ITEM_023 = "023";

	
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
		if(!IS_SUPPORT_TOTALCOUNT) return;
		new TotalCount(mContext, SETTING_KEY, Tag, Value).CountData();
	}

}
