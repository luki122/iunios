package com.aurora.community;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import com.aurora.community.activity.MainActivity;
import com.aurora.community.utils.AccountUtil;
import com.aurora.community.utils.CheckImageLoaderConfiguration;
import com.aurora.community.utils.ImageLoaderHelper;
import com.aurora.datauiapi.data.bean.TagInfo;





import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CommunityApp extends Application {
	private List<WeakReference<Activity>> activityList = new LinkedList<WeakReference<Activity>>();
	private static CommunityApp instance;
	public static MainActivity m_content = null;
	private ArrayList<TagInfo> tagInfos = new ArrayList<TagInfo>();
	
	public void addTag(TagInfo tag){
		tagInfos.add(tag);
	}
	
	public ArrayList<TagInfo> getTagInfos(){
		return tagInfos;
	}
	
	@Override
	public void onCreate() {

		instance = this;
		tagInfos.clear();
		Log.i("CommunityApp", "oncreate");
		super.onCreate();
		if (!ImageLoaderHelper.checkImageLoader())
			CheckImageLoaderConfiguration.initImageLoader(getApplicationContext());
		AccountUtil.getInstance();
	}
	public MainActivity getinstance() {
		return m_content;
	}

	public void setInstance(MainActivity m_obj) {
		m_content = m_obj;
	}
	// 单例模式中获取唯一的ExitApplication 实例
	public static CommunityApp getInstance() {
		return instance;
	}

	// 添加Activity 到容器中
	public void addActivity(Activity activity) {
		activityList.add(new WeakReference<Activity>(activity));
	}

	// 遍历所有Activity 并finish

	public void exit() {
		for (WeakReference<Activity> activity : activityList) {

			if (null != activity.get())
				activity.get().finish();
		}
		activityList.clear();
		System.exit(0);
	}
	
	public void clearActivityByIndex(String clsName){
		for (WeakReference<Activity> activity : activityList) {
			if(activity.get()!=null){
				if(activity.get().getClass().getName().equals(clsName)){
					activity.get().finish();
					activityList.remove(activity);
				}
			}
		}
	}
}
