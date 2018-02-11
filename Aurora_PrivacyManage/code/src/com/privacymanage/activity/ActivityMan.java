/**
 * Vulcan created this file in 2014年10月16日 上午11:32:51 .
 */
package com.privacymanage.activity;

import android.app.Activity;

import com.privacymanage.utils.LogUtils;

import java.lang.ref.SoftReference;
import java.util.HashSet;

/**
 * Vulcan created ActivityMan in 2014年10月16日 .
 * 
 */
public class ActivityMan {
	
	protected static HashSet<SoftReference<Activity>> mActivityList = new HashSet<SoftReference<Activity>>();
	
	public static void addActivity(Activity a) {
		mActivityList.add(new SoftReference<Activity>(a));
	}

	/**
	 * 
	 */
	public ActivityMan() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月16日 上午11:46:16 .
	 */
	public static void killAllActivities() {
		killAllActivitiesWithExcept(null);
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月16日 下午2:35:41 .
	 * @param a
	 */
	public static void killAllActivitiesWithExcept(Activity a) {
		LogUtils.printWithLogCat("vexit", "killAllActivitiesWithExcept: a = " + a);
		Activity activity = null;
		for(SoftReference<Activity> softActivity: mActivityList) {
			activity = softActivity.get();
			if(activity != null && activity != a) {
				activity.finish();
			}
		}
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月28日 下午2:37:17 .
	 * @param a
	 */
	public static void killAllPrivateActivitiesExcept(Activity a) {
		LogUtils.printWithLogCat("vexit", "killAllPrivateActivitiesExcept: a = " + a);
		Activity activity = null;
		for(SoftReference<Activity> softActivity: mActivityList) {
			activity = softActivity.get();
			if(activity != null && activity != a) {
				if(activity instanceof FounderPage) {
					if(((FounderPage)activity).isPrivateActivity()) {
						activity.finish();
						LogUtils.printWithLogCat("vexit", "killAllPrivateActivitiesExcept: it is finished" + activity);
					}
				}
			}
		}
		return;
	}

}
