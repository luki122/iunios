/**
 * Vulcan created this file in 2015年1月29日 下午1:53:21 .
 */
package com.android.phase1.model;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import com.android.phase1.activity.SimpleActivity;

/**
 * Vulcan created ActivityMan in 2015年1月29日 .
 * 
 */
public class ActivityMan {

	/**
	 * 
	 */
	private ActivityMan() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月29日 下午1:55:58 .
	 * @return
	 */
	public static ActivityMan getInstance() {
		if(mInstance == null) {
			mInstance = new ActivityMan();
		}
		return mInstance;
	}
	
	
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月29日 下午1:56:49 .
	 * @param sa
	 */
	public void addSimpleActivity(SimpleActivity sa) {
		mActivities.add(new SoftReference<SimpleActivity>(sa));
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月29日 下午2:05:36 .
	 * @return
	 */
	public List<SimpleActivity> getActivities() {
		final List<SimpleActivity> list = new ArrayList<SimpleActivity>();
		for(SoftReference<SimpleActivity> rsa: mActivities) {
			SimpleActivity sa = rsa.get();
			if(sa != null) {
				list.add(sa);
			}
		}
		return list;
	}
	
	private final List<SoftReference<SimpleActivity>> mActivities = new ArrayList<SoftReference<SimpleActivity>>();
	private static ActivityMan mInstance = null;

}
