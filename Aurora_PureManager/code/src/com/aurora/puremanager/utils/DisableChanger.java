package com.aurora.puremanager.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;

public class DisableChanger extends AsyncTask<Object, Object, Object> {
    final PackageManager mPm;
    final ApplicationInfo mInfo;
    final int mState;
    final Handler mCallBackHandler;

    public DisableChanger(Context activity, ApplicationInfo info, int state,Handler callBackHandler) {
        mPm = activity.getPackageManager();
        mInfo = info;
        mState = state;
        mCallBackHandler = callBackHandler;
    }

    @Override
    protected Object doInBackground(Object... params) {
        mPm.setApplicationEnabledSetting(mInfo.packageName, mState, 0);
        if(mCallBackHandler != null){
        	mCallBackHandler.sendEmptyMessage(0);
        }
        return null;
    }
}
