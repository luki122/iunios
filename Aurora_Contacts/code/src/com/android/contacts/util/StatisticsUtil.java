package com.android.contacts.util;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

public class StatisticsUtil {
	
	private static StatisticsUtil mStatisticsUtil;
	private static Object clock = new Object();
	private Thread mThread = null;
	private List<ConcludeData> mList = new ArrayList<ConcludeData>();
	private Context mContext;
	private static final Uri report_uri = Uri.parse("content://com.iuni.reporter/module/");
    private static final String MODULE = "module_key"; 
    private static final String ITEM = "item_tag"; 
    private static final String VALUE = "value"; 
    
    public static final int Contact_Detail =  0;
    public static final int Detail_Send_Mms =  1;
    public static final int Contact_Search = 2;
    public static final int Contact_Delete =  3;
    public static final int Contact_Setting =  4;
    
    private final String TAG = "StatisticsUtil";
	
	private StatisticsUtil(Context context){
		mContext = context;
	}
	
	public static StatisticsUtil getInstance(Context context){ //必须调用application的contxt
		if(mStatisticsUtil == null){
			synchronized (clock) {
				if(mStatisticsUtil == null){
					mStatisticsUtil = new StatisticsUtil(context);
				}
			}
		}
		return mStatisticsUtil;
	}
	
	public void report(int id){
		switch(id){
		case Contact_Detail:
			conclude(getContactDetail());
			break;
		case Detail_Send_Mms:
			conclude(getDetailSendMms());
			break;
		case Contact_Search:
			conclude(getContactSearch());
			break;
		case Contact_Delete:
			conclude(getContactDelete());
			break;
		case Contact_Setting:
			conclude(getContactSetting());
			break;
		}
	}
	
	private void conclude(ConcludeData data){
		synchronized (clock) {
			if(mThread == null){
				mThread = new ConcludeThread(data);
				mThread.start();
			} else {
				mList.add(data);
			}
		}
	}
	
	private class ConcludeThread extends Thread{
		ConcludeData cData = null;
		public ConcludeThread(ConcludeData data){
			cData = data;
		}
		@Override
		public void run() {
			while(cData != null){
		       	ContentValues values = new ContentValues(3);
		        values.put(MODULE, cData.modeid);
		        values.put(ITEM, cData.concludeid);
		        values.put(VALUE, Integer.valueOf(1));
		        int id = mContext.getContentResolver().update(report_uri, values, null, null);
		        Log.d(TAG, cData.toString() + " insert id is " + id);
				synchronized (clock) {
					if(mList.size() > 0){
						cData = mList.get(0);
						mList.remove(0);
					} else {
						mThread = null;
						cData = null;
					}
				}
			}
		}
	}
	
	private ConcludeData getContactDetail(){
		ConcludeData data = new ConcludeData();
		data.modeid = "100";
		data.concludeid = "006";
		data.reportid = "100001";
		return data;
	}
	
	private ConcludeData getDetailSendMms(){
		ConcludeData data = new ConcludeData();
		data.modeid = "100";
		data.concludeid = "007";
		data.reportid = "100001";
		return data;
	}
	
	private ConcludeData getContactSearch(){
		ConcludeData data = new ConcludeData();
		data.modeid = "100";
		data.concludeid = "008";
		data.reportid = "100001";
		return data;
	}
	
	private ConcludeData getContactDelete(){
		ConcludeData data = new ConcludeData();
		data.modeid = "100";
		data.concludeid = "009";
		data.reportid = "100001";
		return data;
	}
	
	private ConcludeData getContactSetting(){
		ConcludeData data = new ConcludeData();
		data.modeid = "100";
		data.concludeid = "010";
		data.reportid = "100001";
		return data;
	}

	private class ConcludeData{
		public String modeid;
		public String concludeid;
		public String reportid;
		public String toString(){
			return modeid + ";" + concludeid + ";" + reportid;
		}
	}

}
