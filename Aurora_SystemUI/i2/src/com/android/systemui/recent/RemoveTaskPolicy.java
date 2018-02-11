/**
 * All rights belongs to IUNI
 */
package com.android.systemui.recent;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemProperties;
import android.util.Log;

import java.lang.StringBuffer;

import com.android.systemui.R;

// Aurora <Felix.Duan> <2014-9-3> <NEW FILE> Reference app kill policy from auto-start
/**
 * Policy of force stop package when removing task from Recents
 * Kill logic priority in shouldForceStopPackage() :
 *      1. hard coded, kill
 * 2. system package, keep
 * 3. black list, kill
 * 4. white list, keep
 * 5. default, kill
 *
 * @author Felix.Duan
 * @date 2014-9-3
 */
public class RemoveTaskPolicy {

    private static final String TAG = "RemoveTaskPolicy";
    private static final boolean DBG = (SystemProperties.getInt("ro.debuggable", 0) == 1);
    // Got this uri from com.android.secure
    private static final String WHITE_LIST = "content://com.secure.provider.open.AutoStartAppProvider";
    private Context mContext;
    private String[] mBlackList; // Always these kill apps, higher priority
    private String[] mWhiteList; // Do not kill apps, lower priroity

    public RemoveTaskPolicy(Context context) {
        log("RemoveTaskPolicy");
        mContext = context;
    	mBlackList = mContext.getResources().getStringArray(R.array.aurora_blacklist);
        observeWhiteList();
    }

    // Refresh white list
    private void observeWhiteList() {
        mWhiteList = readWhiteList();
    }

    // Read white list from database
    private String[] readWhiteList() {
        logd("readWhiteList");
        Uri mUri = Uri.parse(WHITE_LIST);
        if(mUri == null){
        	return null;
        }
        Cursor c = null;
        try {
			c = mContext.getContentResolver().query(mUri, null, null,
			        null, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return null;
		}
        if (c == null) {
            logd("c == null");
            return null;
        }
        if (c.getCount() == 0) {
            logd("c size is 0");
            return null;
        }

        //String[] colums = c.getColumnNames();
        //StringBuffer buffer = new StringBuffer();
        //for (String col : colums) {
        //    logd("colume " + col);
        //    buffer.append(col).append("   ");
        //}

        //buffer.append("\n---\n");

        String[] list = new String[c.getCount()];
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            //buffer.append(c.getString(1)).append("\n");
            list[c.getPosition()] = c.getString(1);
        }
        c.close();
        return list;
    }

    /**
     * Decide kill logic base on
     * 1. hard coded, kill
     * 2. system package, keep
     * 3. black list, kill
     * 4. white list, keep
     * 5. default, kill
     */
    public boolean shouldForceStopPackage(TaskDescription task) {
        // Always refresh white list
        observeWhiteList();

        logd("shouldForceStopPackage() mBlackList = "
            + mBlackList.length
            + " mWhiteList = " + ((mWhiteList == null) ? "null" :  mWhiteList.length));

        // Hard coded. always kill.
		//Aurora <Steve.Tang> 2014-12-08 add privacy manager to force stop.
        if(("com.android.music".equals(task.packageName)) || 
				("com.aurora.privacymanage".equals(task.packageName)) || 
                ("com.android.contacts".equals(task.packageName)) || 
                ("com.android.mms".equals(task.packageName)) || 
                ("com.sec.android.app.videoplayer".equals(task.packageName)) || 
                ("com.samsung.evergltaskes.video".equals(task.packageName)) || 
                ("com.sec.android.app.music".equals(task.packageName))){
            logd("shouldForceStopPackage() 1 true");
            return true;
        }

        // Aurora <Felix.Duan> <2014-9-15> <BEGIN> Add system app survive logic
        // System app survive
        if(isSystemPackage(task.packageName)) {
            logd("shouldForceStopPackage() 2 false");
            return false;
        // Aurora <Felix.Duan> <2014-9-15> <END> Add system app survive logic
        } else {
            // Black list. always kill. Copied from former implementation.
        	String label = task.getLabel().toString().toLowerCase();
            if(label != null){
				int length=mBlackList.length;//tymy_20150514_bug13118
                for(int i = 0; i < length; i++) {
        			if(label.contains(mBlackList[i].toString().toLowerCase())){
                        logd("shouldForceStopPackage() 3 true");
                        return true;
        			}
        		}
        	}
        }

        // White list, do not kill
        if (mWhiteList != null ) {
            for(String exempt : mWhiteList) {
                if (task.packageName.equals(exempt)) {
                    logd("shouldForceStopPackage() 4 false");
                    return false;
                }
            }
        }

        // Default, kill
        logd("shouldForceStopPackage() 5 true");
        return true;
    }

    private boolean isSystemPackage(String packageName) {
        try {
			PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(packageName,
					PackageManager.GET_DISABLED_COMPONENTS
					|PackageManager.GET_UNINSTALLED_PACKAGES
                    |PackageManager.GET_SIGNATURES);	
			ApplicationInfo appInfo = packageInfo.applicationInfo;
			if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0){
				return true;
			}else{
				return false;
			}
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
	}

    // Normal log
    private void log(String msg) {
        Log.d(TAG, msg);
    }

    // Debug log
    private void logd(String msg) {
        if (DBG) Log.d(TAG, msg);
    }
}
