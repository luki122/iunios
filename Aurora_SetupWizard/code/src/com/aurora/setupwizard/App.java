package com.aurora.setupwizard;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Context;

public class App {
    
    private Context context;
     
    private static App activityManager;
     
    public static App getActivityManager(Context context){
            if(activityManager == null){
                    activityManager = new App(context);
            }
            return activityManager;
    }
     
    private App(Context context){
            this.context = context;
    }
     
    /**
     * task map，用于记录activity栈，方便退出程序（这里为了不影响系统回收activity，所以用软引用）
     */
    private final HashMap<String, SoftReference<Activity>> taskMap = new HashMap<String, SoftReference<Activity>>();
     
    /**
     * 往应用task map加入activity
     */
    public final void putActivity(Activity atv) {
            taskMap.put(atv.toString(), new SoftReference<Activity>(atv));
    }
     
    /**
     * 往应用task map加入activity
     */
    public final void removeActivity(Activity atv) {
            taskMap.remove(atv.toString());
    }
     
    /**
     * 清除应用的task栈，如果程序正常运行这会导致应用退回到桌面
     */
    public final void exit() {
            for (Iterator<Entry<String, SoftReference<Activity>>> iterator = taskMap.entrySet().iterator(); iterator.hasNext();) {
                    SoftReference<Activity> activityReference =  iterator.next().getValue();
                    Activity activity = activityReference.get();
                    if (activity != null) {
                            activity.finish();
                    }
            }
            taskMap.clear();
    }

}
