package com.aurora.reject.util;
import gionee.telephony.GnTelephonyManager;
import java.util.List;
import android.app.Application;
public class RejectApplication extends Application {
	private static RejectApplication application;
	public static boolean MTK_GEMINI_SUPPORT = false;
	public static int isSelectMode=0;
    private List<String> blackList;
    private int countCall=0;
    private int countSms=0;
	public int getCountCall() {
		return countCall;
	}
	public void setCountCall(int countCall) {
		this.countCall = countCall;
	}
	public int getCountSms() {
		return countSms;
	}
	public void setCountSms(int countSms) {
		this.countSms = countSms;
	}
	public List<String> getBlackList() {
		return blackList;
	}
	public void setBlackList(List<String> blackList) {
		this.blackList = blackList;
	}
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		application=this;
		MTK_GEMINI_SUPPORT = GnTelephonyManager.isMultiSimEnabled();
		YuloreUtil.getInstance(this);
		YuloreUtil.bind();
	}
	
	
	
	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
	}
	
	public static RejectApplication getInstance() {
		return application;
	}

}
