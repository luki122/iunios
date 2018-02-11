package com.aurora.account.service;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.aurora.account.AccountApp;
import com.aurora.account.bean.AppConfigInfo;
import com.aurora.account.bean.syncDataObject;
import com.aurora.account.contentprovider.AccountsAdapter;
import com.aurora.account.http.data.HttpRequestGetAccountData;
import com.aurora.account.util.AccountPreferencesUtil;
import com.aurora.account.util.BooleanPreferencesUtil;
import com.aurora.account.util.FileLog;
import com.aurora.account.util.Globals;
import com.aurora.account.util.Log;
import com.aurora.account.util.SystemUtils;
import com.aurora.datauiapi.data.bean.CountResultObject;
import com.aurora.datauiapi.data.bean.DownDataObject;
import com.aurora.datauiapi.data.bean.GetInitMapResultObject;
import com.aurora.datauiapi.data.bean.InitMapInfo;
import com.aurora.datauiapi.data.bean.ServerTimeResultObject;

import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class ModuleDataWorker implements Runnable {

	private static final String TAG = "ModuleDataWorker";

	private Context m_context;
	private String timeStamp = "";
	private String servertime = "";
	private int m_type = 0;
	// type -0 完全重新取得数据 1 - 取没传完的数据

	private static boolean isSucGetDownCount = true;
	private String mModule; // 同步指定模块的包名，空字符串表示同步所有模块
	
	private AccountPreferencesUtil mPref;

	public ModuleDataWorker(Context context, int type) {
		this(context, type, "");
	}

	public ModuleDataWorker(Context context, int type, String module) {
	    if (context == null) {
	        context = AccountApp.getInstance();
	    }
	    m_context = context.getApplicationContext();
		m_type = type;
		mModule = module;
		mPref = AccountPreferencesUtil.getInstance(context);
	}

	private void doDownloadWork() {

		String result = null;
		syncDataObject obj = new syncDataObject();
		if (ExtraFileUpService.getM_downtotalcount() != 0) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(
						DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
						true);
				mapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
				mapper.getDeserializationConfig()
						.set(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
								false);
				List<AppConfigInfo> info = SystemUtils.getAppConfigInfo(m_context);
				if(info.get(ExtraFileUpService.getModule_index()).isIsrepeatSync())
				{
					timeStamp = "0";
				}
				result = HttpRequestGetAccountData.getDownDataObject(
				        mPref.getUserID(),
				        mPref.getUserKey(),
						ExtraFileUpService.getM_apptype(), timeStamp,
						servertime, ExtraFileUpService.getM_currentcount(),
						ExtraFileUpService.getM_percount(),
						ExtraFileUpService.getM_downtotalcount());
				DownDataObject dataObject = (DownDataObject) mapper.readValue(
						result, DownDataObject.class);
				FileLog.i(TAG, "zhangwei the download result="+result);
				obj.setSycndata(dataObject.getRecords());
				ExtraFileUpService.startDownload(m_context, obj);

			} catch (Exception e) {
				e.printStackTrace();
				ExtraFileUpService.pauseOperation(m_context,1);

			}
		} else {
			Log.i(TAG, "zhangwei the down data is 0");
			ExtraFileUpService.startDownload(m_context, obj);
		}
	}

	private void doInitDownloadWork(String type, List<String> ids, Boolean bl) {
		//Log.i(TAG, "zhangwei the doInitDownloadWork0");
		String result = null;
		syncDataObject obj = new syncDataObject();
		if (ids.size() != 0) {
			try {
				//Log.i(TAG, "zhangwei the doInitDownloadWork1");
				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(
						DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
						true);
				mapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
				mapper.getDeserializationConfig()
						.set(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
								false);

				result = HttpRequestGetAccountData.getInitDataObject(
				        mPref.getUserID(),
						mPref.getUserKey(), type, ids, bl);
				DownDataObject dataObject = (DownDataObject) mapper.readValue(
						result, DownDataObject.class);

				
				obj.setSycndata(dataObject.getRecords());
				ExtraFileUpService.startDownload(m_context, obj);

			} catch (Exception e) {
				e.printStackTrace();
				ExtraFileUpService.pauseOperation(m_context,1);
			}
		} else {
			//Log.i(TAG, "zhangwei the doInitDownloadWork2");
			List<AppConfigInfo> apps = SystemUtils.getAppConfigInfo(m_context);
			SystemUtils.updateAppSwitch(m_context, 
					apps.get(ExtraFileUpService.getModule_index()).getApp_packagename(), false);
			ExtraFileUpService.startDownload(m_context, obj);
		}
	}

	private void setDownCount() {
		try {
			if (((ExtraFileUpService.getM_downtotalcount() == 0)
					&& (m_type == 0)) || (!isSucGetDownCount)) {
				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(
						DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
						true);
				mapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
				mapper.getDeserializationConfig()
						.set(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
								false);
				List<AppConfigInfo> info = SystemUtils.getAppConfigInfo(m_context);
				if(info.get(ExtraFileUpService.getModule_index()).isIsrepeatSync())
				{
					timeStamp = "0";
				}
				String countResult = HttpRequestGetAccountData
						.getDownloadCount(mPref.getUserID(),
						        mPref.getUserKey(),
								ExtraFileUpService.getM_apptype(), timeStamp,
								servertime);
				CountResultObject countResultObject = mapper.readValue(
						countResult, CountResultObject.class);
				
				
				if (countResultObject.getCode() == CountResultObject.CODE_SUCCESS) {
					ExtraFileUpService.setM_downtotalcount(countResultObject
							.getCount());
					isSucGetDownCount = true;
					FileLog.i(TAG, "zhangwei the download count="+countResultObject.getCount());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			isSucGetDownCount = false;
			ExtraFileUpService.pauseOperation(m_context,1);
			
		}
	}

	private void getServerTime(SharedPreferences mSyncTime, String app_type) {
		try {
			if (m_type == 0) {
				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(
						DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
						true);
				mapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
				mapper.getDeserializationConfig()
						.set(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
								false);
				String countResult = HttpRequestGetAccountData.getServerTime(
				        mPref.getUserID(),
				        mPref.getUserKey(), app_type);
				ServerTimeResultObject countResultObject = mapper.readValue(
						countResult, ServerTimeResultObject.class);
				if (countResultObject.getCode() == CountResultObject.CODE_SUCCESS) {
					Editor ed = mSyncTime.edit();
					if (BooleanPreferencesUtil.getInstance(m_context).isFirstTimeSync()) {
						servertime = countResultObject.getSysTime();
						timeStamp = "0";
					} else {
						servertime = countResultObject.getSysTime();
						timeStamp = countResultObject.getSyncTime();
					}
					Log.i(TAG, "the server time=" + servertime
							+ " the timeStamp=" + timeStamp);
					FileLog.i(TAG, "the server time=" + servertime
							+ " the timeStamp=" + timeStamp);
					ed.putString(Globals.SHARED_SERVERTIME_SYNC_KEY, servertime);
					ed.putString(Globals.SHARED_TIMESTMAP_SYNC_KEY, timeStamp);
					ed.commit();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			ExtraFileUpService.pauseOperation(m_context,1);
		}
	}

	private ArrayList<InitMapInfo> getInitMap(String app_type) {
		try {

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(
					DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
					true);
			mapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
			mapper.getDeserializationConfig()
					.set(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
							false);
			String initResult = HttpRequestGetAccountData.getInitMap(
			        mPref.getUserID(),
			        mPref.getUserKey(), app_type);
			GetInitMapResultObject initResultObject = mapper.readValue(
					initResult, GetInitMapResultObject.class);
			if (initResultObject.getCode() == initResultObject.CODE_SUCCESS) {

				return initResultObject.getRecords();
			}

		} catch (Exception e) {
			e.printStackTrace();
			ExtraFileUpService.pauseOperation(m_context,1);
			
		}
		return null;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		int index = ExtraFileUpService.getModule_index();
		Log.i(TAG, "zhangwei the moduleDataWork0");
		List<AppConfigInfo> apps = SystemUtils.getAppConfigInfo(m_context);

		// 开关没开直接下一个模块
		for (int i = index; i < apps.size(); i++) {
			if (!apps.get(i).isSync()) {
				index++;
			} else
				break;
		}
		
		if (TextUtils.isEmpty(mModule)) {
			
			ExtraFileUpService.setModule_index(index);
		} else {
		    for (int i = 0, size = apps.size(); i < size; i++) {
                AppConfigInfo info = apps.get(i);
                if (info.getApp_packagename().equalsIgnoreCase(mModule)) {
                    index = i;
                    ExtraFileUpService.setModule_index(index);
                    break;
                }
            }
		    ExtraFileUpService.setTotalModuleCount(1);
		}
		
		// 防止由于后面的return导致界面进度没有刷新
		ExtraFileUpService.updateProgress();

		if (index >= apps.size()) {
			Log.i(TAG, "zhangwei the moduleDataWork");
			ExtraFileUpService.dotheFinish(true);
			return;
		}
		
		// 如果是模块自己同步
		if (apps.get(index).isApp_syncself()) {
			Log.i(TAG, "tell app sync");
			
			ExtraFileUpService.doNextModule();
			return;
		}
		
		if (ExtraFileUpService.isPaused())
		{
			ExtraFileUpService.isCanContinue = true;
			return;
		}
		
		String packageName = apps.get(index).getApp_packagename();
		String uri = apps.get(index).getApp_uri();
		String app_type = apps.get(index).getApp_type();

		ExtraFileUpService.setM_packageName(packageName);
		ExtraFileUpService.setM_uri(uri);
		ExtraFileUpService.setM_apptype(app_type);

		int type = ExtraFileUpService.getSync_type();

		SharedPreferences mSyncTime = m_context.getSharedPreferences(
				Globals.SHARED_WIFI_SYNC, Activity.MODE_PRIVATE);
		timeStamp = mSyncTime.getString(Globals.SHARED_TIMESTMAP_SYNC_KEY, "0");
		servertime = mSyncTime.getString(Globals.SHARED_SERVERTIME_SYNC_KEY,
				"0");

		getServerTime(mSyncTime, apps.get(index).getApp_type());

//		ExtraFileUpService.updateProgress();

		// 上传
		if (type == 0) {
			Log.i(TAG, "upload Module_index: " + index);

			AccountsAdapter db = new AccountsAdapter(m_context, packageName,
					uri);

			if ((ExtraFileUpService.getM_uptotalcount() == 0) && (m_type == 0)) {
				int total_count = (int) db.getSyncCount();
				String isFirstSync = db.getisFirstSync();
				boolean bl = isFirstSync.endsWith("true")?true:false;
				SystemUtils.updateAppRepeat(m_context, packageName,bl);
				ExtraFileUpService.setM_currentcount(0);
				ExtraFileUpService.setM_uptotalcount(total_count);
				Log.i(TAG, "zhangwei the upload total_count=" + total_count);
				FileLog.i(TAG, "zhangwei the upload total_count=" + total_count);
			}
			if (ExtraFileUpService.isPaused())
				return;
			if (apps.get(index).isSwitch() && (m_type == 0)) {

				ArrayList<InitMapInfo> init_map = getInitMap(app_type);

				List<String> m_ids = db.initmapdata(packageName, init_map);

				ExtraFileUpService.setM_downtotalcount(m_ids.size());
				
				ExtraFileUpService.setM_ids(m_ids);
			} else {
				setDownCount();
			}
			Log.i(TAG, "zhangwei the c_count="+ExtraFileUpService.getM_currentcount());
			FileLog.i(TAG, "zhangwei the c_count="+ExtraFileUpService.getM_currentcount());
			syncDataObject obj = db.syncUp(servertime, timeStamp,
					ExtraFileUpService.getError_count(),
					ExtraFileUpService.getM_percount());

			if (obj.getSycndata().size() == 0) {
				ExtraFileUpService.setSync_type(1);
				Log.i(TAG, "zhangwei the up data is 0");
				if (ExtraFileUpService.isPaused())
				{
					ExtraFileUpService.isCanContinue = true;
					return;
				}
				doDownload(apps, index, app_type);
			} else {
				if (ExtraFileUpService.isPaused())
				{
					ExtraFileUpService.isCanContinue = true;
					return;
				}
				ExtraFileUpService.startUpload(m_context, obj);
				
			}
		}
		// 下载
		else if (type == 1) {
			if (ExtraFileUpService.isPaused())
			{
				ExtraFileUpService.isCanContinue = true;
				return;
			}
			doDownload(apps, index, app_type);

		}

	}

	private void doDownload(List<AppConfigInfo> apps, int index, String app_type) {
		if (apps.get(index).isSwitch()) {

			int c_count = ExtraFileUpService.getM_currentcount();
			int p_count = ExtraFileUpService.getM_percount();
			int t_count = ExtraFileUpService.getM_downtotalcount();
			//Log.i(TAG, "zhangwei the c_count="+c_count+" the t_count="+t_count);
			//FileLog.i(TAG, "zhangwei the c_count="+c_count+" the t_count="+t_count);
			List<String> ids = new ArrayList<String>();
			Boolean bl = false;
			if (c_count + p_count >= t_count - 1) {
				bl = true;
			}
			if(ExtraFileUpService.getM_ids().size() > 0)
			{
				for (int i = c_count; i < c_count+p_count ; i++) {
					if(i < t_count)
						ids.add(ExtraFileUpService.getM_ids().get(i));
				}
			}
			doInitDownloadWork(app_type, ids, bl);

		} else {
			doDownloadWork();
		}
	}

}
