package com.aurora.puremanager.utils;

import com.aurora.puremanager.activity.AllAppListActivity;
import com.aurora.puremanager.data.StorageLowNotifyData;
import android.content.Context;
import android.content.SharedPreferences;

public class MySharedPref {	
	/**
	 * 保存存储空间不足提示用户的数据
	 * @param context
	 * @param mode
	 */
	public static void saveStorageLowNotifyData(Context context,
			StorageLowNotifyData data){
		if(context == null || data == null){
			return ;
		}		
		SharedPreferences setting = 
				context.getSharedPreferences(mConfig.STORAGE_NOTIFY_KEY, 0);
		
		if(setting == null){
			return ;
		}
		
		SharedPreferences.Editor editor = setting.edit();
		
		if(editor == null){
			return ;
		}
		editor.putBoolean(mConfig.IS_ALREADY_LOW_KEY,data.getIsAlreadyLow());
		editor.putInt(mConfig.ALREADY_NOTIFY_TIMES_KEY, data.getAlreadyNotifyTimes());
		editor.putLong(mConfig.LAST_NOTIFY_TIME_KEY,data.getLastNotifyTime());
		editor.commit();
	}
	
	/**
	 * 存储空间不足提示用户的数据
	 * @param context
	 * @return 注：返回值不可能为null
	 */
	public static StorageLowNotifyData getStorageLowNotifyData(Context context){
		StorageLowNotifyData data = new StorageLowNotifyData();
		if(context == null){
			return data;
		}	
		SharedPreferences setting= 
				context.getSharedPreferences(mConfig.STORAGE_NOTIFY_KEY,0);
		
		if(setting == null){
			return data;
		}
		
		data.setIsAlreadyLow(setting.getBoolean(mConfig.IS_ALREADY_LOW_KEY, false));
		data.setAlreadyNotifyTimes(setting.getInt(mConfig.ALREADY_NOTIFY_TIMES_KEY, 0));
		data.setLastNotifyTime(setting.getLong(mConfig.LAST_NOTIFY_TIME_KEY, 0));
		
		return data;
	}
	
	/**
	 * 记录空间清理界面，是否按照缓存排序
	 * @param context
	 * @param is
	 */
	public synchronized static void saveIsSortByCache(Context context,
			boolean is){
		if(context == null){
			return ;
		}		
		SharedPreferences setting = 
				context.getSharedPreferences(mConfig.CONFIG_KEY, 0);
		
		if(setting == null){
			return ;
		}
		
		SharedPreferences.Editor editor = setting.edit();
		
		if(editor == null){
			return ;
		}
		editor.putBoolean(mConfig.SORT_BY_CACHE_KEY,is);
		editor.commit();
	}
	
	/**
	 * 记录空间清理界面，是否按照缓存排序
	 * @param context
	 * @return
	 */
	public synchronized static boolean getIsSortByCache(Context context){
		if(context == null){
			return false;
		}	
		SharedPreferences setting= 
				context.getSharedPreferences(mConfig.CONFIG_KEY,0);
		
		if(setting == null){
			return false;
		}
		boolean result = setting.getBoolean(mConfig.SORT_BY_CACHE_KEY,true);
		
		return result;
	}	
	
	/**
	 * 记录全部应用界面应用的展示的内容（个人应用，系统应用，系统组件）
	 * @param context
	 * @param state
	 */
	public synchronized static void saveAllAppSortRecord(Context context,
			int state){
		if(context == null){
			return ;
		}		
		SharedPreferences setting = 
				context.getSharedPreferences(mConfig.CONFIG_KEY, 0);
		
		if(setting == null){
			return ;
		}
		
		SharedPreferences.Editor editor = setting.edit();
		
		if(editor == null){
			return ;
		}
		editor.putInt(mConfig.ALL_APP_SORT_KEY,state);
		editor.commit();
	}
	
	/**
	 * 记录全部应用界面应用的展示的内容（个人应用，系统应用，系统组件）
	 * @param context
	 * @return 
	 */
	public synchronized static int getAllAppSortRecord(Context context){
		if(context == null){
			return AllAppListActivity.SORT_BY_USER_APP;
		}	
		SharedPreferences setting= 
				context.getSharedPreferences(mConfig.CONFIG_KEY,0);
		
		if(setting == null){
			return AllAppListActivity.SORT_BY_USER_APP;
		}
		
		return setting.getInt(mConfig.ALL_APP_SORT_KEY,AllAppListActivity.SORT_BY_USER_APP);
	}
	
	/**
	 * 记录本地存储的广告插件的版本号
	 * @param context
	 * @param state
	 */
	public synchronized static void saveAdLibVersion(Context context,
			int version){
		if(context == null){
			return ;
		}		
		SharedPreferences setting = 
				context.getSharedPreferences(mConfig.CONFIG_KEY, 0);
		
		if(setting == null){
			return ;
		}
		
		SharedPreferences.Editor editor = setting.edit();
		
		if(editor == null){
			return ;
		}
		editor.putInt(mConfig.AD_LIB_VERSION,version);
		editor.commit();
	}
	
	/**
	 * 记录本地存储的广告插件的版本号
	 * @param context
	 * @return 
	 */
	public synchronized static int getAdLibVersion(Context context){
		if(context == null){
			return 0;
		}	
		SharedPreferences setting= 
				context.getSharedPreferences(mConfig.CONFIG_KEY,0);
		
		if(setting == null){
			return 0;
		}
		
		return setting.getInt(mConfig.AD_LIB_VERSION,0);
	}
	
	/**
	 * 是否已经手动扫描过广告应用
	 * @param context
	 * @param state
	 */
	public synchronized static void saveAlreadyScanAd(Context context,
			boolean isAlready){
		if(context == null){
			return ;
		}		
		SharedPreferences setting = 
				context.getSharedPreferences(mConfig.CONFIG_KEY, 0);
		
		if(setting == null){
			return ;
		}
		
		SharedPreferences.Editor editor = setting.edit();
		
		if(editor == null){
			return ;
		}
		editor.putBoolean(mConfig.IS_ALREADY_MANUAL_SCAN_AD_APP,isAlready);
		editor.commit();
	}
	
	/**
	 * 是否已经手动扫描过广告应用
	 * @param context
	 * @return 
	 */
	public synchronized static boolean getAlreadyScanAd(Context context){
		if(context == null){
			return false;
		}	
		SharedPreferences setting= 
				context.getSharedPreferences(mConfig.CONFIG_KEY,0);
		
		if(setting == null){
			return false;
		}
		
		return setting.getBoolean(mConfig.IS_ALREADY_MANUAL_SCAN_AD_APP,false);
	}
	
	/**
	 * 是否已经手动完整的扫描过广告应用
	 * @param context
	 * @param state
	 */
	public synchronized static void saveIsCompleteScanAdApp(Context context,
			boolean isAlready){
		if(context == null){
			return ;
		}		
		SharedPreferences setting = 
				context.getSharedPreferences(mConfig.CONFIG_KEY, 0);
		
		if(setting == null){
			return ;
		}
		
		SharedPreferences.Editor editor = setting.edit();
		
		if(editor == null){
			return ;
		}
		editor.putBoolean(mConfig.IS_ALREADY_COMPLETE_MANUAL_SCAN_AD_APP,isAlready);
		editor.commit();
	}
	
	/**
	 * 是否已经手动完整的扫描过广告应用
	 * @param context
	 * @return 
	 */
	public synchronized static boolean getIsCompleteScanAdApp(Context context){
		if(context == null){
			return false;
		}	
		SharedPreferences setting= 
				context.getSharedPreferences(mConfig.CONFIG_KEY,0);
		
		if(setting == null){
			return false;
		}
		
		return setting.getBoolean(mConfig.IS_ALREADY_COMPLETE_MANUAL_SCAN_AD_APP,false);
	}
	
	/**
	 * 是否已经读取存放在本地的广告库
	 * @param context
	 * @param state
	 */
	public synchronized static void saveAlreadyReadAssetsAdlib(Context context,
			boolean isAlready){
		if(context == null){
			return ;
		}		
		SharedPreferences setting = 
				context.getSharedPreferences(mConfig.CONFIG_KEY, 0);
		
		if(setting == null){
			return ;
		}
		
		SharedPreferences.Editor editor = setting.edit();
		
		if(editor == null){
			return ;
		}
		editor.putBoolean(mConfig.IS_ALREADY_READ_ASSETS_ADLIB,isAlready);
		editor.commit();
	}
	
	/**
	 * 是否已经读取存放在本地的广告库
	 * @param context
	 * @return 
	 */
	public synchronized static boolean getAlreadyReadAssetsAdlib(Context context){
		if(context == null){
			return false;
		}	
		SharedPreferences setting= 
				context.getSharedPreferences(mConfig.CONFIG_KEY,0);
		
		if(setting == null){
			return false;
		}
		
		return setting.getBoolean(mConfig.IS_ALREADY_READ_ASSETS_ADLIB,false);
	}
}
