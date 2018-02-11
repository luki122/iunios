package com.netmanage.utils;

import com.netmanage.data.DateData;
import com.netmanage.data.NotificationData;

import android.content.Context;
import android.content.SharedPreferences;

public class MySharedPref {	
	
//	/**
//	 * 记录sim卡的imsi号
//	 * @param context
//	 */
//	public synchronized static void saveImsi(Context context,String imsi){
//		if(context == null){
//			return ;
//		}		
//		SharedPreferences setting = context.getSharedPreferences(mConfig.SIM_INFO_KEY, 0);	
//		if(setting == null){
//			return ;
//		}	
//		SharedPreferences.Editor editor = setting.edit();	
//		if(editor == null){
//			return ;
//		}
//		editor.putString(mConfig.IMSI_KEY,imsi);
//		editor.commit();
//	}
	
//	/**
//	 * sim卡的imsi号
//	 * @param context
//	 */
//	public synchronized static String getImsi(Context context){
//		if(context == null){
//			return null;
//		}	
//		SharedPreferences setting= context.getSharedPreferences(mConfig.SIM_INFO_KEY,0);	
//		if(setting == null){
//			return null;
//		}
//		return setting.getString(mConfig.IMSI_KEY,null);
//	}
	
	/**
	 * 记录最后sim卡的imsi号
	 * @param context
	 */
	public synchronized static void saveLastImsi(Context context,String imsi){
		if(context == null){
			return ;
		}		
		SharedPreferences setting = context.getSharedPreferences(mConfig.SIM_INFO_KEY, 0);	
		if(setting == null){
			return ;
		}	
		SharedPreferences.Editor editor = setting.edit();	
		if(editor == null){
			return ;
		}
		editor.putString(mConfig.LAST_IMSI_KEY,imsi);
		editor.commit();
	}
	
	/**
	 * 最后sim卡的imsi号
	 * @param context
	 */
	public synchronized static String getLastImsi(Context context){
		if(context == null){
			return null;
		}	
		SharedPreferences setting= context.getSharedPreferences(mConfig.SIM_INFO_KEY,0);	
		if(setting == null){
			return null;
		}
		return setting.getString(mConfig.LAST_IMSI_KEY,null);
	}
	
	/**
	 * 预警提示用户的数据
	 * @param context
	 * @param mode
	 */
	public static void saveEarlyWarningNotifyData(Context context,
			NotificationData data){
		if(context == null){
			return ;
		}		
		saveNotifyData(context.getSharedPreferences(mConfig.EarlyWarning_NOTIFY_KEY, 0),data);
	}
	
	/**
	 * 预警提示用户的数据
	 * @param context
	 * @return 
	 */
	public static NotificationData getEarlyWarningNotifyData(Context context){
		if(context == null){
			return null;
		}	
		return getNotifyData(context.getSharedPreferences(mConfig.EarlyWarning_NOTIFY_KEY,0));
	}
	
	/**
	 * 每天提示用户的数据
	 * @param context
	 * @param mode
	 */
	public static void saveEveryDayNotifyData(Context context,
			NotificationData data){
		if(context == null){
			return ;
		}		
		saveNotifyData(context.getSharedPreferences(mConfig.EveryDay_NOTIFY_KEY, 0),data);
	}
	
	/**
	 * 每天提示用户的数据
	 * @param context
	 * @return 
	 */
	public static NotificationData getEveryDayNotifyData(Context context){
		if(context == null){
			return null;
		}	
		return getNotifyData(context.getSharedPreferences(mConfig.EveryDay_NOTIFY_KEY,0));
	}
	
	/**
	 * 后台流量提示
	 * @param context
	 * @param mode
	 */
	public static void clearBackgroundFlowNotifyData(Context context){
		if(context == null){
			return ;
		}		
		clearNotifyData(context.getSharedPreferences(mConfig.BackgroundFlow_NOTIFY_KEY, 0));
	}
	
	/**
	 * 后台流量提示
	 * @param context
	 * @param mode
	 */
	public static void saveBackgroundFlowNotifyData(Context context,
			NotificationData data){
		if(context == null){
			return ;
		}		
		saveNotifyData(context.getSharedPreferences(mConfig.BackgroundFlow_NOTIFY_KEY, 0),data);
	}
	
	/**
	 * 后台流量提示
	 * @param context
	 * @return 
	 */
	public static NotificationData getBackgroundFlowNotifyData(Context context){
		if(context == null){
			return null;
		}	
		return getNotifyData(context.getSharedPreferences(mConfig.BackgroundFlow_NOTIFY_KEY,0));
	}
	
	/**
	 * 超额提示
	 * @param context
	 * @param mode
	 */
	public static void saveExcessNotifyData(Context context,
			NotificationData data){
		if(context == null){
			return ;
		}		
		saveNotifyData(context.getSharedPreferences(mConfig.Excess_NOTIFY_KEY, 0),data);
	}
	
	/**
	 * 超额提示
	 * @param context
	 * @return 
	 */
	public static NotificationData getExcessNotifyData(Context context){
		if(context == null){
			return null;
		}	
		return getNotifyData(context.getSharedPreferences(mConfig.Excess_NOTIFY_KEY,0));
	}
	
	/**
	 * 更新sim卡
	 * @param context
	 * @param mode
	 */
	public static void clearSimChangeNotifyData(Context context){
		if(context == null){
			return ;
		}		
		clearNotifyData(context.getSharedPreferences(mConfig.SIM_CHANGE_KEY, 0));
	}
	
	/**
	 * 更新sim卡
	 * @param context
	 * @param mode
	 */
	public static void saveSimChangeNotifyData(Context context,
			NotificationData data){
		if(context == null){
			return ;
		}		
		saveNotifyData(context.getSharedPreferences(mConfig.SIM_CHANGE_KEY, 0),data);
	}
	
	/**
	 * 更新sim卡
	 * @param context
	 * @return 
	 */
	public static NotificationData getSimChangeNotifyData(Context context){
		if(context == null){
			return null;
		}	
		return getNotifyData(context.getSharedPreferences(mConfig.SIM_CHANGE_KEY,0));
	}
	
	private static synchronized void clearNotifyData(SharedPreferences setting){
		if(setting == null){
			return ;
		}		
        SharedPreferences.Editor editor = setting.edit();		
		if(editor == null){
			return ;
		}
		editor.putInt(mConfig.ALREADY_NOTIFY_TIMES_KEY, 0);
		editor.putInt(mConfig.LAST_NOTIFY_YEAR_KEY,1970);
		editor.putInt(mConfig.LAST_NOTIFY_MONTH_KEY,1);
		editor.putInt(mConfig.LAST_NOTIFY_DAY_KEY,1);

		editor.commit();
	}
	
	private static synchronized void saveNotifyData(SharedPreferences setting,NotificationData data){
		if(setting == null || data == null){
			return ;
		}		
        SharedPreferences.Editor editor = setting.edit();		
		if(editor == null){
			return ;
		}
		editor.putInt(mConfig.ALREADY_NOTIFY_TIMES_KEY, data.getAlreadyNotifyTimes());
		if(data.getLastNotifyTime() != null){
			editor.putInt(mConfig.LAST_NOTIFY_YEAR_KEY,data.getLastNotifyTime().getYear());
			editor.putInt(mConfig.LAST_NOTIFY_MONTH_KEY,data.getLastNotifyTime().getMonth());
			editor.putInt(mConfig.LAST_NOTIFY_DAY_KEY,data.getLastNotifyTime().getDay());
		}
		editor.commit();
	}
	
	private static synchronized NotificationData getNotifyData(SharedPreferences setting){	
		if(setting == null){
			return null;
		}
		NotificationData data = new NotificationData();
		data.setAlreadyNotifyTimes(setting.getInt(mConfig.ALREADY_NOTIFY_TIMES_KEY, 0));
		
		DateData lastNotifyTime = new DateData();
		lastNotifyTime.setYear(setting.getInt(mConfig.LAST_NOTIFY_YEAR_KEY, 0));
		lastNotifyTime.setMonth(setting.getInt(mConfig.LAST_NOTIFY_MONTH_KEY, 0));
		lastNotifyTime.setDay(setting.getInt(mConfig.LAST_NOTIFY_DAY_KEY, 0));
		
		data.setLastNotifyTime(lastNotifyTime);
		return data;
	}
	
    
	/**
	 * 所有应用的全部流量值
	 * @param context
	 * @param mode
	 */
	public static void saveAllApkTotalTraffic(Context context,
			long trafficValue){
		if(context == null){
			return ;
		}		
		SharedPreferences setting = 
				context.getSharedPreferences(mConfig.TRAFFIC_VALUE_KEY, 0);
		
		if(setting == null){
			return ;
		}
		
		SharedPreferences.Editor editor = setting.edit();
		
		if(editor == null){
			return ;
		}
		editor.putLong(mConfig.ALL_APK_TOTAL_TRAFFIC_KEY,trafficValue);
		editor.commit();
	}
	
	/**
	 * 所有应用的全部流量值
	 * @param context
	 * @return
	 */
	public static long getAllApkTotalTraffic(Context context){
		if(context == null){
			return 0;
		}	
		SharedPreferences setting= 
				context.getSharedPreferences(mConfig.TRAFFIC_VALUE_KEY,0);
		
		if(setting == null){
			return 0;
		}
		long result = setting.getLong(mConfig.ALL_APK_TOTAL_TRAFFIC_KEY,0);
		
		return result;
	}
	
	/**
	 * 所有应用的Mobile流量值
	 * @param context
	 * @param mode
	 */
	public static void saveAllApkMobileTraffic(Context context,
			long trafficValue){
		if(context == null){
			return ;
		}		
		SharedPreferences setting = 
				context.getSharedPreferences(mConfig.TRAFFIC_VALUE_KEY, 0);
		
		if(setting == null){
			return ;
		}
		
		SharedPreferences.Editor editor = setting.edit();
		
		if(editor == null){
			return ;
		}
		editor.putLong(mConfig.ALL_APK_MOBILE_TRAFFIC_KEY,trafficValue);
		editor.commit();
	}
	
	/**
	 * 所有应用的Mobile流量值
	 * @param context
	 * @return
	 */
	public static long getAllApkMobileTraffic(Context context){
		if(context == null){
			return 0;
		}	
		SharedPreferences setting= 
				context.getSharedPreferences(mConfig.TRAFFIC_VALUE_KEY,0);
		
		if(setting == null){
			return 0;
		}
		long result = setting.getLong(mConfig.ALL_APK_MOBILE_TRAFFIC_KEY,0);
		
		return result;
	}
	
	/**
	 * 记录是不是已经初始化了数据库
	 * @param context
	 * @param isInitSqlite true:已经初始化了；false还没有初始化 
	 */
	public static void saveInitSqliteRecord(Context context,
			boolean isInitSqlite){
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
		editor.putBoolean(mConfig.INIT_SQLITE_KEY,isInitSqlite);
		editor.commit();
	}
	
	/**
	 * 获取是不是已经初始化了数据库
	 * @param context
	 * @return true:已经初始化了；false还没有初始化 
	 */
	public static boolean getInitSqliteRecord(Context context){
		if(context == null){
			return false;
		}	
		SharedPreferences setting= 
				context.getSharedPreferences(mConfig.CONFIG_KEY,0);
		
		if(setting == null){
			return false;
		}
		boolean result = setting.getBoolean(mConfig.INIT_SQLITE_KEY,false);
		
		return result;
	}
	
	/**
	 * 记录是不是已经根据自启动白名单进行相应的配置
	 * @param context
	 * @param isInit true:已经初始化了；false还没有初始化 
	 */
	public synchronized static void saveInitAutoStartRecord(Context context,
			boolean isInit){
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
		editor.putBoolean(mConfig.INIT_AUTO_START,isInit);
		editor.commit();
	}
	
	/**
	 * 获取是不是已经根据自启动白名单进行相应的配置
	 * @param context
	 * @return true:已经配置了；false还没有配置 
	 */
	public synchronized static boolean getInitAutoStartRecord(Context context){
		if(context == null){
			return false;
		}	
		SharedPreferences setting= 
				context.getSharedPreferences(mConfig.CONFIG_KEY,0);
		
		if(setting == null){
			return false;
		}
		boolean result = setting.getBoolean(mConfig.INIT_AUTO_START,false);
		
		return result;
	}	
}
