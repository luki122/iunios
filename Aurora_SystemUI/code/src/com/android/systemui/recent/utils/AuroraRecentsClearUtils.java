package com.android.systemui.recent.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.android.systemui.R;
import aurora.app.AuroraActivity;
import java.util.HashMap;


public abstract class AuroraRecentsClearUtils {
	
	public static final String TAG = "AuroraRecentsClearUtils";

	private static final String PREFRENCE_NOTCLEAR = "recents_not_clear";
	private static final String LOCKED_NUM = "num_loked";
	private static final String LOCKED_ITME = "li";
	//private static LinkedList<String> mLockedPkg = new LinkedList<String>();
	private static HashMap<String,Boolean> mLockedPkg = new HashMap<String,Boolean>();

	private static boolean mLoaded = false;
    public static void loadLockFlag(Context context){
		if(mLoaded){
			return;
		}
		
		try{
			SharedPreferences sharedPreferences = context.getSharedPreferences(
					PREFRENCE_NOTCLEAR, AuroraActivity.MODE_PRIVATE);
			int num = sharedPreferences.getInt(LOCKED_NUM, 0);
			mLockedPkg.clear();
			for(int i = 0; i < num; ++i){
				String item = LOCKED_ITME + i;
				mLockedPkg.put(sharedPreferences.getString(item,""),new Boolean(true));
			}

			mLoaded = true;
		}catch(Exception ex){
			
		}

    }

	public static void setLockFlag(String pkg,boolean toLock) {
		mLockedPkg.put(pkg,new Boolean(toLock));
	}

	public static boolean getLockFlag(String pkg) {
		Boolean v = mLockedPkg.get(pkg);
		if(null != v){
			return v.booleanValue();	
		}
		return false;
		 
	}

    // Aurora <Felix.Duan> <2014-4-22> <BEGIN> Fix BUG #4470. Move IO task off UI thread.
    // Move IO task off UI thread.
    public static void saveLockFlag(final Context context) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    SharedPreferences sharedPreferences = context
                            .getSharedPreferences(PREFRENCE_NOTCLEAR,
                                    AuroraActivity.MODE_PRIVATE);
                    Editor editor = sharedPreferences.edit();
                    editor.clear();
                    int num = 0;
                    int size = mLockedPkg.size();
                    for (String k : mLockedPkg.keySet()) {
                        if (mLockedPkg.get(k).booleanValue()) {
                            editor.putString(LOCKED_ITME + num, k);
                            ++num;
                        }
                    }
                    editor.putInt(LOCKED_NUM, num);
                    editor.commit();
                } catch (Exception ex) {

                }
            }

        }).start();
    }
    // Aurora <Felix.Duan> <2014-4-22> <END> Fix BUG #4470. Move IO task off UI thread.
}
