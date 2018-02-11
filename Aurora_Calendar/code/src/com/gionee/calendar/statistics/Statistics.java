package com.gionee.calendar.statistics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.gionee.calendar.view.Log;
import com.youju.statistics.YouJuAgent;

import aurora.app.AuroraActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

/*
 * Statistical information and to the server
 * 
 * @version 1.0
 * 
 * @author pengwei
 * 
 * @since 2012-11-26
 * */
//Gionee <pengwei><2013-04-12> modify for DayView begin
//Gionee <pengwei><2013-06-07> modify for CR00000000 begin
//Gionee <pengwei><20130807> modify for CR00850530 begin
public class Statistics {
	public static final String DAY_VIEW_Time_JUMP = "日视图-时间跳转 ";
	public static final String DAY_VIEW_BACK_TODAY = "日视图-回今天";
	public static final String DAY_VIEW_CLICK_SCHEDULE_LIST = "日视图-点击活动列表";
	public static final String DAY_VIEW_LONG_CLICK_SCHEDULE_LIST = "日视图-长按活动列表";
	public static final String DAY_VIEW_LONG_CLICK_SCHEDULE_LIST_CHECK = "日视图-长按-查看活动";
	public static final String DAY_VIEW_LONG_CLICK_SCHEDULE_LIST_EDIT = "日视图-长按-编辑活动";
	public static final String DAY_VIEW_LONG_CLICK_SCHEDULE_LIST_DEL = "日视图-长按-删除活动";
	public static final String DAY_VIEW_LONG_CLICK_SCHEDULE_LIST_SHARE = "日视图-长按-分享活动";
	public static final String DAY_VIEW_ADD_SCHEDULE = "日视图-添加活动";
	public static final String DAY_VIEW_GOTO_MONTH_VIEW = "日视图-月视图minitable";
	public static final String DAY_VIEW_GOTO_WEEK_VIEW = "日视图-周视图minitable";
	public static final String DAY_VIEW_DRAWER = "日视图-抽屉按钮";
	public static final String DAY_VIEW_SCROLL_PRE_DAY = "日视图-滑动切换至前一天";
	public static final String DAY_VIEW_SCROLL_NEXT_DAY = "日视图-滑动切换至后一天";
	//Week View
	public static final String WEEK_VIEW_Time_JUMP = "周视图-时间跳转 ";
	public static final String WEEK_VIEW_CLICK_HAVE_SCHEDULE = "周视图-时间段点击（有活动）";
	public static final String WEEK_VIEW_CLICK_NO_SCHEDULE = "周视图-时间段点击（无活动）";
	public static final String WEEK_VIEW_CLICK_ADD_SCHEDULE = "周视图-时间段点击（+号）";
	public static final String WEEK_VIEW_BACK_TODAY = "周视图-回今天";
	public static final String WEEK_VIEW_LONG_CLICK_SCHEDULE = "周视图-长按活动";
	public static final String WEEK_VIEW_LONG_CLICK_SCHEDULE_CHECK = "周视图-长按-查看活动";
	public static final String WEEK_VIEW_LONG_CLICK_SCHEDULE_EDIT = "周视图-长按-编辑活动";
	public static final String WEEK_VIEW_LONG_CLICK_SCHEDULE_DEL = "周视图-长按-删除活动";
	public static final String WEEK_VIEW_LONG_CLICK_SCHEDULE_SHARE = "周视图-长按-分享活动";
	public static final String WEEK_VIEW_ADD_SCHEDULE = "周视图-添加活动";
	public static final String WEEK_VIEW_GOTO_MONTH_VIEW = "周视图-月视图minitable";
	public static final String WEEK_VIEW_GOTO_DAY_VIEW = "周视图-日视图minitable";
	public static final String WEEK_VIEW_DRAWER = "周视图-抽屉按钮";
	public static final String WEEK_VIEW_SCROLL_PRE_WEEK = "周视图-滑动切换至前一周";
	public static final String WEEK_VIEW_SCROLL_NEXT_WEEK = "周视图-滑动切换至后一周";

	
	//Sliding View
	
	public static final String SLIDING_VIEW_SERCH_CLICK="抽屉-搜索";
	public static final String SLIDING_VIEW_ALL_ACTIVITY_CLICK="抽屉-全部活动";
	public static final String SLIDING_VIEW_ALL_ACTIVITY_LONG_CLICK_ITEM="抽屉-全部活动-长按活动列表";
	public static final String SLIDING_VIEW_ALL_ACTIVITY_LONG_CLICK_LOOK="抽屉-全部活动-长按-查看活动";
	public static final String SLIDING_VIEW_ALL_ACTIVITY_LONG_CLICK_EDIT="抽屉-全部活动-长按-编辑活动";
	public static final String SLIDING_VIEW_ALL_ACTIVITY_LONG_CLICK_DELETE="抽屉-全部活动-长按-删除活动";
	public static final String SLIDING_VIEW_ALL_ACTIVITY_LONG_CLICK_SHARE="抽屉-全部活动-长按-分享活动";
	public static final String SLIDING_VIEW_ALL_ACTIVITY_RETURN_TODAY_CLICK="抽屉-全部活动-回今天";
	public static final String SLIDING_VIEW_ALL_ACTIVITY_TIME_CHANGE="抽屉-全部活动-时间跳转";
	public static final String SLIDING_VIEW_CLEAR_ALL_ACTIVITY="抽屉-清除所有事件";
	public static final String SLIDING_VIEW_SETTING="抽屉-设置";
	public static final String SLIDING_VIEW_SETTING_ADD_ACOUNT="抽屉-设置-添加账户";
	public static final String SLIDING_VIEW_SETTING_DISPLAY="抽屉-设置-要显示的日历";
	public static final String SLIDING_VIEW_SETTING_SYNC="抽屉-设置-要同步的日历";
	public static final String SLIDING_VIEW_SETTING_VIEW_SETTING="抽屉-设置-日历视图设置";
	public static final String SLIDING_VIEW_SETTING_VIEW_START_ON="抽屉-设置-日历视图设置-一周开始日";
	public static final String SLIDING_VIEW_SETTING_VIEW_START_ON_LOCAL="抽屉-设置-日历视图设置-一周开始日-语言区域的默认设置";
	public static final String SLIDING_VIEW_SETTING_VIEW_START_ON_SAT="抽屉-设置-日历视图设置-一周开始日-星期六";
	public static final String SLIDING_VIEW_SETTING_VIEW_START_ON_SUN="抽屉-设置-日历视图设置-一周开始日-星期日";
	public static final String SLIDING_VIEW_SETTING_VIEW_START_ON_MON="抽屉-设置-日历视图设置-一周开始日-星期一";
	public static final String SLIDING_VIEW_SETTING_VIEW_START_HOME_TIME_ZONE_ON="抽屉-设置-日历视图设置-使用家所在时区-开";
	public static final String SLIDING_VIEW_SETTING_VIEW_START_HOME_TIME_ZONE_OFF="抽屉-设置-日历视图设置-使用家所在时区-关";
	public static final String SLIDING_VIEW_SETTING_VIEW_REMINDER_SETTINGS="抽屉-设置-提醒设置";
	public static final String SLIDING_VIEW_SETTING_VIEW_REMINDER_SETTINGS_NOTIFICATIONS_ON="抽屉-设置-提醒设置-通知-开";
	public static final String SLIDING_VIEW_SETTING_VIEW_REMINDER_SETTINGS_NOTIFICATIONS_OFF="抽屉-设置-提醒设置-通知-关";
	public static final String SLIDING_VIEW_SETTING_VIEW_REMINDER_SETTINGS_CHOOSE_RINGTONE="抽屉-设置-提醒设置-选择铃声";
	public static final String SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_NOTIFICATION_ON="抽屉-设置-提醒设置-弹出式通知-开";
	public static final String SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_NOTIFICATION_OFF="抽屉-设置-提醒设置-弹出式通知-关";
	public static final String SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIMEF="抽屉-设置-提醒设置-默认提醒时间";
	public static final String SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_NONE="抽屉-设置-提醒设置-默认提醒时间-无";
	public static final String SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_0="抽屉-设置-提醒设置-默认提醒时间-0分钟";
	public static final String SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_1="抽屉-设置-提醒设置-默认提醒时间-1分钟";
	public static final String SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_5="抽屉-设置-提醒设置-默认提醒时间-5分钟";
	public static final String SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_10="抽屉-设置-提醒设置-默认提醒时间-10分钟";
	public static final String SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_15="抽屉-设置-提醒设置-默认提醒时间-15分钟";
	public static final String SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_20="抽屉-设置-提醒设置-默认提醒时间-20分钟";
	public static final String SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_25="抽屉-设置-提醒设置-默认提醒时间-25分钟";
	public static final String SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_30="抽屉-设置-提醒设置-默认提醒时间-30分钟";
	public static final String SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_45="抽屉-设置-提醒设置-默认提醒时间-45分钟";
	public static final String SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_1h="抽屉-设置-提醒设置-默认提醒时间-1小时";
	public static final String SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_2h="抽屉-设置-提醒设置-默认提醒时间-2小时";
	public static final String SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_3h="抽屉-设置-提醒设置-默认提醒时间-3小时";
	public static final String SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_12h="抽屉-设置-提醒设置-默认提醒时间-12小时";
	public static final String SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_24h="抽屉-设置-提醒设置-默认提醒时间-24小时";
	public static final String SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_2D="抽屉-设置-提醒设置-默认提醒时间-2天";
	public static final String SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_1D="抽屉-设置-提醒设置-默认提醒时间-1周";
	public static final String SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_ABOUT="抽屉-设置-关于日历";

	//Look Activity
	
	public static final String EVENT_INFO_REMINDE="查看活动-提醒";
	public static final String EVENT_INFO_REMINDE_ZERO="查看活动-提醒-0分钟";
	public static final String EVENT_INFO_REMINDE_ONE="查看活动-提醒-1分钟";
	public static final String EVENT_INFO_REMINDE_FIVE="查看活动-提醒-5分钟";
	public static final String EVENT_INFO_REMINDE_TEN="查看活动-提醒-10分钟";
	public static final String EVENT_INFO_REMINDE_FIFTEEN="查看活动-提醒-15分钟";
	public static final String EVENT_INFO_REMINDE_TWETY="查看活动-提醒-20分钟";
	public static final String EVENT_INFO_REMINDE_TWETY_FIVE="查看活动-提醒-25分钟";
	public static final String EVENT_INFO_REMINDE_THIRTY="查看活动-提醒-30分钟";
	public static final String EVENT_INFO_REMINDE_FORTY_FIVE="查看活动-提醒-45分钟";
	public static final String EVENT_INFO_REMINDE_ONE_HOUR="查看活动-提醒-1小时";
	public static final String EVENT_INFO_REMINDE_TWO_HOURS="查看活动-提醒-2小时";
	public static final String EVENT_INFO_REMINDE_THREE_HOURS="查看活动-提醒-3小时";
	public static final String EVENT_INFO_REMINDE_TWELVE_HOURS="查看活动-提醒-12小时";
	public static final String EVENT_INFO_REMINDE_TWETY_FOUR="查看活动-提醒-24小时";
	public static final String EVENT_INFO_REMINDE_TWO_DAYS="查看活动-提醒-2天";
	public static final String EVENT_INFO_REMINDE_ONE_WEEK="查看活动-提醒-1周";
	public static final String EVENT_INFO_DELETE="查看活动-删除";
	public static final String EVENT_INFO_EDIT="查看活动-编辑";
	public static final String EVENT_INFO_SHARE="查看活动-分享";
	
	
	//CREATE ACTIVITY
	
	public static final String EDIT_EVENT_START_TIME="添加活动详情-开始时间";
	
	public static final String EDIT_EVENT_END_TIME="添加活动详情-结束时间";
	public static final String EDIT_EVENT_REMINDE="添加活动详情-提醒";
	public static final String EDIT_EVENT_REMINDE_ZERO="添加活动详情-提醒-0分钟";
	public static final String EDIT_EVENT_REMINDE_ONE="添加活动详情-提醒-1分钟";
	public static final String EDIT_EVENT_REMINDE_FIVE="添加活动详情-提醒-5分钟";
	public static final String EDIT_EVENT_REMINDE_TEN="添加活动详情-提醒-10分钟";
	public static final String EDIT_EVENT_REMINDE_FIFTEEN="添加活动详情-提醒-15分钟";
	public static final String EDIT_EVENT_REMINDE_TWETY="添加活动详情-提醒-20分钟";
	public static final String EDIT_EVENT_REMINDE_TWETY_FIVE="添加活动详情-提醒-25分钟";
	public static final String EDIT_EVENT_REMINDE_THIRTY="添加活动详情-提醒-30分钟";
	public static final String EDIT_EVENT_REMINDE_FORTY_FIVE="添加活动详情-提醒-45分钟";
	public static final String EDIT_EVENT_REMINDE_ONE_HOUR="添加活动详情-提醒-1小时";
	public static final String EDIT_EVENT_REMINDE_TWO_HOURS="添加活动详情-提醒-2小时";
	public static final String EDIT_EVENT_REMINDE_THREE_HOURS="添加活动详情-提醒-3小时";
	public static final String EDIT_EVENT_REMINDE_TWELVE_HOURS="添加活动详情-提醒-12小时";
	public static final String EDIT_EVENT_REMINDE_TWETY_FOUR="添加活动详情-提醒-24小时";
	public static final String EDIT_EVENT_REMINDE_TWO_DAYS="添加活动详情-提醒-2天";
	public static final String EDIT_EVENT_REMINDE_ONE_WEEK="添加活动详情-提醒-1周";
	public static final String EDIT_EVENT_CLICK_MORE="添加活动详情-更多";
	public static final String EDIT_EVENT_CLICK_VOICE_BUTTON="添加活动详情-语音添加日程";
	public static final String EDIT_EVENT_CANCLE="添加活动详情-舍弃";
	public static final String EDIT_EVENT_SHAVE_NO_MORE="添加活动详情-保存（更多收起）";
	public static final String EDIT_EVENT_SHAVE_MORE="添加活动详情-保存（更多展开）";
	public static final String EDIT_EVENT_CLICK_ADD_REMINDER="添加活动详情-添加提醒";
	public static final String EDIT_EVENT_REPITE="添加活动详情-重复";
	public static final String EDIT_EVENT_REPITE_ONCE="添加活动详情-重复-一次性活动";
	public static final String EDIT_EVENT_REPITE_DAY="添加活动详情-重复-每天";
	public static final String EDIT_EVENT_REPITE_WEEK_DAY="添加活动详情-重复-每个工作日";
	public static final String EDIT_EVENT_REPITE_EVERY_WEEK="添加活动详情-重复-每周";
	public static final String EDIT_EVENT_REPITE_EVERY_MONTHLY_ON_DAY_COUNT="添加活动详情-重复-每月（每月的第三个周三）";
	public static final String EDIT_EVENT_REPITE_EVERY_REPEATS_MONTHLY_ON_DAY="添加活动详情-重复-每月（15日）";
	public static final String EDIT_EVENT_REPITE_EVERY_YEAR="添加活动详情-重复-每年";
	public static final String EDIT_EVENT_DESCRIPTION="添加活动详情-说明";
	public static final String EDIT_EVENT_ACOUNT="添加活动详情-选择账号";
	
	// month view
	public static final String MONTH_DAY_PICKED = "月视图-时间跳转 ";
	public static final String MONTH_DAY_TAPPED_WITH_AGENDA = "月视图-点击日期（有活动）";
	public static final String MONTH_DAY_TAPPED_WITHOUT_AGENDA = "月视图-点击日期（无活动）";
	public static final String MONTH_DAY_TAPPED_CURRENT = "月视图-点击日期（本月）";
	public static final String MONTH_DAY_TAPPED_PREVIOUS = "月视图-点击日期（上一个月）";
	public static final String MONTH_DAY_TAPPED_NEXT = "月视图-点击日期（下一个月）";
	public static final String MONTH_DAY_BACK_TO_TODAY = "月视图-回今天";
	public static final String MONTH_DAY_AGENDA_COUNT = "月视图-选中日期的活动数目";
	public static final String MONTH_AGENDA_LIST_CLICK = "月视图-点击活动列表";
	public static final String MONTH_AGENDA_LIST_LONG_CLICK = "月视图-长按活动列表";
	public static final String MONTH_AGENDA_LIST_LONG_CLICK_VIEW = "月视图-长按-查看活动";
	public static final String MONTH_AGENDA_LIST_LONG_CLICK_EDIT = "月视图-长按-编辑活动";
	public static final String MONTH_AGENDA_LIST_LONG_CLICK_DELETE = "月视图-长按-删除活动";
	public static final String MONTH_AGENDA_LIST_LONG_CLICK_SHARE = "月视图-长按-分享活动";
	public static final String MONTH_VIEW_ADD_NEW_AGENDA = "月视图-添加活动";
	public static final String MONTH_VIEW_GOTO_WEEK_VIEW = "月视图-周视图minitable";
	public static final String MONTH_VIEW_GOTO_DAY_VIEW = "月视图-日视图minitable";
	public static final String MONTH_VIEW_GOTO_SETTING = "月视图-抽屉按钮";
	public static final String MONTH_VIEW_FLING_TO_PREVIOUS = "月视图-滑动切换至前一个月";
	public static final String MONTH_VIEW_FLING_TO_NEXT = "月视图-滑动切换至后一个月";
	
	public static  int MONTH_VIEW_GOTO_WEEK_VIEW_NUM = 0;
	public static  int MONTH_VIEW_GOTO_DAY_VIEW_NUM = 0;
	public static  int WEEK_VIEW_GOTO_DAY_VIEW_NUM = 0;
	public static  int WEEK_VIEW_GOTO_MONTH_VIEW_NUM = 0;
	public static  int DAY_VIEW_GOTO_WEEK_VIEW_NUM = 0;
	public static  int DAY_VIEW_GOTO_MONTH_VIEW_NUM = 0;



	
	private Map<String, Integer> statisticsMap = new HashMap<String, Integer>();


	private static Statistics single = null;

	public synchronized static Statistics getInstance() {
		if (single == null) {
			single = new Statistics();
		}
		return single;
	}

	public void setStatisticsCount(String key) {
		int count = 0;
		if (statisticsMap.containsKey(key)) {
			count = statisticsMap.get(key);
		}
		statisticsMap.put(key, ++count);
	}

	/*
	 * clear all Statistical data
	 * 
	 * @param void
	 * 
	 * @return void
	 * 
	 * @since 2012-11-27
	 */
	private void clearAllStatistics() {
		statisticsMap.clear();
	}

	private String versionName = null;
	private String packageName = null;

	/*
	 * Get VersionName
	 * 
	 * @param context Context environment of Activity
	 * 
	 * @return void
	 * 
	 * @throws NameNotFoundException
	 */
	public void getInfos(Context context) {
		try {
			PackageManager pmManager = context.getPackageManager();
			PackageInfo pinfo;
			pinfo = pmManager.getPackageInfo(context.getPackageName(),
					PackageManager.GET_CONFIGURATIONS);
			versionName = pinfo.versionName;
			packageName = pinfo.packageName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			Log.d("Statistics---getInfos---error" + e);
		}
	}
	
	//Gionee <Author: lihongyu> <2013-05-07> add for CR000000 begin
	public String getVersionName() {
		return versionName;
	}
	//Gionee <Author: lihongyu> <2013-05-07> add for CR000000 end

	/*
	 * send Statistics Message
	 * 
	 * @param context Context environment of Activity strMsg The information you
	 * wish to send
	 * 
	 * strMsg the message which you want to send
	 * 
	 * @return void
	 * 
	 * @throws Excetion
	 */
	private boolean sendMessage(String strMsg, Context context) {
		try {
			if (strMsg == null || "".equals(strMsg)) {
				Log.d("Statistics---sendMsg---strMsg==" + strMsg);
				return false;
			}
			Log.d("Statistics---sendMessage---strMsg==" + strMsg);
			Intent intent = new Intent(
					"gn.com.android.statistics.WRITE_STATISTICS_INFO");
			intent.putExtra("packagename", packageName);
			intent.putExtra("versionName", versionName);
			intent.putExtra("message", strMsg);
			context.startService(intent);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			Log.e("Statistics---sendMessage---error==" + strMsg, e);
			return false;
		}
	}
	public void sendMsg(Context context) {
		if (null == versionName || null == packageName) {
			getInfos(context);
		}
		if (null == versionName || null == packageName) {
			Log.d("Statistics---sendMsg---versionName==" + versionName
					+ " or packageName==" + packageName);
			return;
		}
		String strMsg = CombineData();
		if (sendMessage(strMsg, context)) {// If failure, there is no clear
											// statistical data
			clearAllStatistics();
		}
	}
	// Gionee <pengwei><2013-05-28> modify for CR00820281 end
	private final String DSCOLLATOR = ":";
	private String CombineData() {
		try {
			StringBuffer stringBuffer = new StringBuffer();
			Iterator<Entry<String, Integer>> iter = statisticsMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String,Integer> entry = (Map.Entry<String,Integer>) iter.next();
				String key = String.valueOf(entry.getKey());
				stringBuffer.append(key);
				stringBuffer.append(DSCOLLATOR);
				String val = String.valueOf(entry.getValue());
				stringBuffer.append(val);
			}
			Log.v("Statistics---CombineData---stringBuffer==" + stringBuffer.toString());
			return stringBuffer.toString();
		} catch (Exception e) {
			// TODO: handle exception
			Log.e("Statistics---CombineData---error==", e);
			return null;
		}
	}
	// Gionee <pengwei><2013-05-28> modify for CR00820281 end
	
    // call when oncreate()
    public static void setAssociateUserImprovementPlan(Context content,
            boolean isAssociate) {
		Log.v("Statistics---setAssociateUserImprovementPlan---");
        YouJuAgent.setAssociateUserImprovementPlan(content, isAssociate);
    }

    // call when oncreate()
    public static void setReportUncaughtExceptions(Boolean enabled) {
		Log.v("Statistics---setReportCaughtExceptions---enabled == " + enabled);
        YouJuAgent.setReportUncaughtExceptions(enabled);
    }

    public static void onResume(final Context con) {
        /*new Thread() {
            public void run() {
        		Log.v("Statistics---onResume---Context == " + con);
                YouJuAgent.onResume(con);
            }
        }.start();*/
    }

    public static  void onPause(final Context con) {
        /*new Thread() {
            public void run() {
        		Log.v("Statistics---onPause---Context == " + con);
                YouJuAgent.onPause(con);
            }
        }.start();*/
    }

    public static void onEvent(final AuroraActivity activity, final String EVENT_ID) {
        Log.i("Statistics---onEvent1 == " + EVENT_ID);
        /*new Thread() {
            public void run() {
                YouJuAgent.onEvent(activity, EVENT_ID);
            }
        }.start();*/
    }

    public static void onEvent(final AuroraActivity activity, final String EVENT_ID,
            final String EVENT_LABEL) {
        Log.i("Statistics---onEvent2---EVENT_ID == " + EVENT_ID);
        Log.i("Statistics---onEvent2---EVENT_LABEL == " + EVENT_LABEL);
        /*new Thread() {
            public void run() {
                YouJuAgent.onEvent(activity, EVENT_ID, EVENT_LABEL);
            }
        }.start();*/
    }

    public static  void onEvent(final AuroraActivity activity, final String EVENT_ID,
            final String EVENT_LABEL, final Map<String, Object> map) {
        /*new Thread() {
            public void run() {
                YouJuAgent.onEvent(activity, EVENT_ID, EVENT_LABEL, map);
            }
        }.start();*/
    }

    public static void onError(final AuroraActivity activity,
            final Throwable throwable) {
        /*new Thread() {
            public void run() {
                YouJuAgent.onError(activity, throwable);
            }
        }.start();*/
    }

    
    public static void onEvent(final Context context, final String EVENT_ID) {
        Log.i("Statistics---onEvent1 == " + EVENT_ID);
        /*new Thread() {
            public void run() {
                YouJuAgent.onEvent(context, EVENT_ID);
            }
        }.start();*/
    }

    public static void onEvent(final Context context, final String EVENT_ID,
            final String EVENT_LABEL) {
        /*new Thread() {
            public void run() {
                YouJuAgent.onEvent(context, EVENT_ID, EVENT_LABEL);
            }
        }.start();*/
    }

    public static  void onEvent(final Context context, final String EVENT_ID,
            final String EVENT_LABEL, final Map<String, Object> map) {
        /*new Thread() {
            public void run() {
                YouJuAgent.onEvent(context, EVENT_ID, EVENT_LABEL, map);
            }
        }.start();*/
    }

    public static void onError(final Context context,
            final Throwable throwable) {
        /*new Thread() {
            public void run() {
                YouJuAgent.onError(context, throwable);
            }
        }.start();*/
    }
    
}
// Gionee <pengwei><2013-04-12> modify for DayView end
//Gionee <pengwei><2013-06-07> modify for CR00000000 end
