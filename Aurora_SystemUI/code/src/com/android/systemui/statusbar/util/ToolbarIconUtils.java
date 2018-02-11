/**
	File Description:
		Utils for sorting tool icon in notification bar.
	Author: fengjy@gionee.com
	Create Date: 2013/04/24
	Change List:
*/


package com.android.systemui.statusbar.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import aurora.app.AuroraActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.SystemProperties;
import android.util.Log;

import com.android.systemui.R;

public abstract class ToolbarIconUtils {
	
	public static final String TAG = "ToolbarIconUtils";
	// Aurora <zhanggp> <2013-10-08> modified for systemui begin
	public static final boolean GN_QUICK_SETTINGS_SUPPORT = false;//SystemProperties.get("ro.gn.quick.settings.support").equals("yes");
	// Aurora <zhanggp> <2013-10-08> modified for systemui end
	public static final String ACTION_REFRESH_TOOLLIST = "gionee.systemui.action.REFRESH_TOOLLIST";
	public static final String ACTION_RESET_TOOLLIST = "gionee.systemui.action.RESET_TOOLLIST";
	public static final String ACTION_REFRESH_TOOLBAR = "gionee.systemui.action.REFRESH_TOOLBAR";
	
	public static final int MAX_SHOWED_COUNT = 11;
	
	private static final String PREFRENCE_TOOLS_SORT = "tools_sort";
	private static final String KEY_TOOLS_COUNT = "tools_count";
	private static final String KEY_SORTED_HEAD = "sorted_";
	private static final String KEY_TEMPORARY_FLAG = "temporary_flag";
	private static final String KEY_TEMPORARY_HEAD = "temporary_";
	private static final int INDEX_CAN_INSERT = 10;

	private static boolean mIsUpdated = true;
	private static Map<Integer, ToolbarIcon> mOriginalIconMap;
    private static List<ToolbarIcon> mSortedIconList;
    
    public static ToolbarIcon getToolbarIconById(int id) {
    	return mOriginalIconMap.get(id);
    }
    
    public static void setOriginalIconList(Map<Integer, ToolbarIcon> originalIconMap) {
    	mOriginalIconMap = originalIconMap;
    }
    
    private static void initDefaultIconList(Context context) {
    	mSortedIconList = new ArrayList<ToolbarIcon>();
    	int[] defaultIconOrder = context.getResources().getIntArray(R.array.default_icon_order);
    	for (int id : defaultIconOrder) {
    		mSortedIconList.add(mOriginalIconMap.get(id));
	        Log.d(TAG, "Default order: icon " + id);
    	}
    }
    
    public static void setTemporaryIconVisible(Context context, ToolbarIcon icon, boolean visible) {
    	boolean isSortedListInited = mSortedIconList != null && mSortedIconList.size() > 0;
    	if (isSortedListInited) {
    		if (visible) {
        		if (!mSortedIconList.contains(icon)) {
        			mSortedIconList.add(INDEX_CAN_INSERT, icon);
        	        updateIconSortPreferences(context, mSortedIconList);
        	    	context.sendBroadcast(new Intent(ToolbarIconUtils.ACTION_RESET_TOOLLIST));
        		}
            	icon.setVisible(context, visible);
    		} else if (mSortedIconList.contains(icon)) {
            	icon.setVisible(context, visible);
    		}
    	}
    }

	public static List<ToolbarIcon> getSortedIconList(Context context) {
		if (mIsUpdated) {
        	Log.d(TAG, "Preferences updated, reset list.");
	        SharedPreferences sharedPreferences = context.getSharedPreferences(
	        		PREFRENCE_TOOLS_SORT, AuroraActivity.MODE_PRIVATE);
	        
	        int count = sharedPreferences.getInt(KEY_TOOLS_COUNT, 0);
	        if (count == 0) {
	        	Log.d(TAG, "No preferences, init default tool icon list.");
	        	initDefaultIconList(context);
	        	updateIconSortPreferences(context, mSortedIconList);
	        } else {
		        mSortedIconList = new ArrayList<ToolbarIcon>();
		        for (int i = 0; i < count; i++) {
		            int id = sharedPreferences.getInt(KEY_SORTED_HEAD + i, -1);
		            if (id == -1) {
		            	Log.d(TAG, "Get tool id -1, continue!");
		            	continue;
		            }
		            ToolbarIcon icon = mOriginalIconMap.get(id);
		            if (icon == null || icon.getId() != id) {
		            	Log.d(TAG, "Get error tool id, continue!");
		            	continue;
		            }
	            	mSortedIconList.add(icon);
		        }
	        }
	        mIsUpdated = false;
		}
		return mSortedIconList;
	}

	public static void updateIconSortPreferences(Context context, List<ToolbarIcon> sortedIconList) {
		mSortedIconList = sortedIconList;
        SharedPreferences sharedPreferences = context.getSharedPreferences(
        		PREFRENCE_TOOLS_SORT, AuroraActivity.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_TOOLS_COUNT, sortedIconList.size());
        for (int i = 0; i < sortedIconList.size(); i++) {
            editor.putInt(KEY_SORTED_HEAD + i, sortedIconList.get(i).getId());
        }
        editor.commit();
        mIsUpdated = true;
	}
	
}
