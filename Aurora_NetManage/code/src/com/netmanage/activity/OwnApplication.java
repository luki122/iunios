package com.netmanage.activity;

import java.util.HashMap;
import java.util.Map;

import tmsdk.common.IDualPhoneInfoFetcher;
import tmsdk.common.ITMSApplicaionConfig;
import tmsdk.common.TMSDKContext;
import android.app.Application;
import android.util.Log;

import com.gionee.internal.telephony.GnTelephonyManagerEx;
import com.netmanage.service.TmsSecureService;
import com.netmanage.utils.Utils;

/**
 * 使用tms.jar，必须具备一个Application的子类
 */
public final class OwnApplication extends Application {
	
	private static final String TAG = "OwnApplication";

	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.i(TAG, "isMultiSimEnabled: " + Utils.isMultiSimEnabled());
		
		// 判断手机是否双卡
		if (Utils.isMultiSimEnabled()) {
			/***
			 * TMSDKContext.setDualPhoneInfoFetcher()方法为流量校准支持双卡情况时设置，其它情况不需要调用该函数。
			 * 该函数中需要返回第一卡槽和第二卡槽imsi的读取内容。
			 * 
			 * 实现此方法时。一定在TMSDKContext.init前调用
			 */
			TMSDKContext.setDualPhoneInfoFetcher(new tmsdk.common.IDualPhoneInfoFetcher(){
	
				@Override
				public String getIMSI(int simIndex) {
					String imsi = "";
					if (simIndex == IDualPhoneInfoFetcher.FIRST_SIM_INDEX) {
						// 卡槽1的imsi，需要厂商自己实现获取方法
						imsi = Utils.getImsi(OwnApplication.this, 0);
					} else if(simIndex == IDualPhoneInfoFetcher.SECOND_SIM_INDEX) {
						// 卡槽2的imsi，需要厂商自己实现获取方法
						imsi = Utils.getImsi(OwnApplication.this, 1);
					}
					
	/* 				try {
						TelephonyManager telephonyManager = (TelephonyManager) DemoApplication.this.getSystemService(Context.TELEPHONY_SERVICE);
						imsi = telephonyManager.getSubscriberId();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					android.util.Log.v("TrafficCorrectionUser", "getIMSI--imsi:[" + imsi + "]");
	 */				
					return imsi;
				}
				
			});
		}

		// TMS总入口
		TMSDKContext.init(this, TmsSecureService.class, new ITMSApplicaionConfig() {
				@Override
				public HashMap<String, String> config(
						Map<String, String> src) {
					return new HashMap<String, String>(src);
				}
		});
	}
}
