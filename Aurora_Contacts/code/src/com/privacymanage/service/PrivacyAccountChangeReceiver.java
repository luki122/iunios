package com.privacymanage.service;

import java.util.ArrayList;
import java.util.List;

import com.privacymanage.data.AidlAccountData;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PrivacyAccountChangeReceiver extends BroadcastReceiver{
	
	private static final String TAG = "PrivacyAccountChangeReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		
		if (intent.getExtras() != null) {
			String action = intent.getAction();
			AidlAccountData account = intent.getParcelableExtra("account");
        	Log.i(TAG, "PrivacyAccountChangeReceiver  "
        			+ "onReceive action: " + action 
        			+ "  account id: " + account.getAccountId() 
        			+ "  path: " + account.getHomePath());
        	
			if (action != null && action.equals("com.aurora.privacymanage.SWITCH_ACCOUNT")) {
	        	AuroraPrivacyUtils.mCurrentAccountId = account.getAccountId();
	        	AuroraPrivacyUtils.mCurrentAccountHomePath = account.getHomePath();
	        	
	        	if (AuroraPrivacyUtils.mCurrentAccountId > 0) {
	        		AuroraPrivacyUtils.mIsPrivacyMode = true;
	        	} else {
	        		AuroraPrivacyUtils.mIsPrivacyMode = false;
	        	}
	        	
			} else if (action != null && action.equals("com.aurora.privacymanage.DELETE_ACCOUNT")) {
				boolean delete = intent.getBooleanExtra("delete", false);
				AuroraPrivacyUtils.mIsPrivacyMode = false;
				AuroraPrivacyUtils.mCurrentAccountId = 0;
				AuroraPrivacyUtils.mCurrentAccountHomePath = null;
			}
			
			AuroraPrivacyUtils.killPrivacyActivity();
		}
	}
	
//	private void killTask(Context context, ActivityManager am) {
//        List<RecentTaskInfo> tasks = am.getRecentTasks(TASK_MAX, 0);
//        List<RunningTaskInfo> runTasks = am.getRunningTasks(TASK_MAX);
//        RunningTaskInfo topTask = null;
//        RunningTaskInfo secTask = null;
//        
//        if (runTasks != null && runTasks.size() > 0) {
//            topTask = runTasks.get(0);
//            if (runTasks.size() > 1) {
//                secTask = runTasks.get(1);
//            }
//        }
//        
//        if (tasks == null) {
//            tasks = new ArrayList<ActivityManager.RecentTaskInfo>();
//        }
//        
//        for (int i = 0; i < tasks.size(); i++) {
//            RecentTaskInfo taskInfo = tasks.get(i);
//            
//            if ((topTask != null && topTask.id == taskInfo.persistentId) || 
//                    ((secTask != null && secTask.id == taskInfo.persistentId))) {
//            } else {
//            	try {
//            		String packageName = taskInfo.origActivity.getPackageName();
//                	Log.e("wangth", "packageName = " + packageName);
//                    if (packageName.equals("com.android.contacts")) {
//                    	am.removeTask(taskInfo.persistentId, 0);
//                    }
//            	} catch (Exception e) {
//            		e.printStackTrace();
//            	}
//            }
//            
//        }
//    }

}
