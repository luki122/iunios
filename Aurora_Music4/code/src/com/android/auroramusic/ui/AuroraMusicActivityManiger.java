package com.android.auroramusic.ui;

import java.util.LinkedList;
import java.util.List;

import com.android.auroramusic.util.LogUtil;

import android.app.Activity;

public class AuroraMusicActivityManiger {

	private static final String TAG = "AuroraMusicActivityManiger";
	private List<Activity> activitys = null;
	private static AuroraMusicActivityManiger instance;

	private AuroraMusicActivityManiger() {
		activitys = new LinkedList<Activity>();
	}

	/**
	 * 单例模式中获取唯一的AuroraMusicActivityManiger实例
	 * 
	 * @return
	 */
	public static AuroraMusicActivityManiger getInstance() {
		if (null == instance) {
			instance = new AuroraMusicActivityManiger();
		}
		return instance;

	}

	// 添加Activity到容器中
	public void addActivity(Activity activity) {
		if (activitys != null && activitys.size() > 0) {
			if (!activitys.contains(activity)) {
				activitys.add(activity);
			}
		} else {
			activitys.add(activity);
		}

	}

	public void removeActivity(Activity activity) {
		if (activitys != null && activitys.size() > 0) {
			if (activitys.contains(activity)) {
				activitys.remove(activity);
			}
		}
	}

	// 遍历所有Activity并finish
	public void exit() {
		LogUtil.d(TAG, "activitys.size:" + activitys.size());
		if (activitys != null && activitys.size() > 0) {
			for (Activity activity : activitys) {
				activity.finish();
			}
		}
		// System.exit(0);
	}
}
