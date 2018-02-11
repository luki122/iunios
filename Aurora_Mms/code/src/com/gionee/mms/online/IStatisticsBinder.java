package com.gionee.mms.online;

import gn.com.android.statistics.aidl.IStatisticsInterface;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class IStatisticsBinder {

    private Context mContext;
    private String mPackageName;
    private String mVerName;
    private IStatisticsInterface mBinder;
    private static final String TAG = "IStatisticsBinder";

    private ServiceConnection mService = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            mBinder = IStatisticsInterface.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            mBinder = null;
        }
    };

    public IStatisticsBinder(Context ctx) {
        mContext = ctx;
        mPackageName = mContext.getPackageName();
        mVerName = OnlineUtils.getMmsVersion(ctx);
        start();
    }

    public void start() {
        mContext.bindService(new Intent(IStatisticsInterface.class.getName()),
                mService, Context.BIND_AUTO_CREATE);
    }

    public void stop() {
        mContext.unbindService(mService);
    }

    public void writeStatistisMessage(String msg) {
        try {
            if (mBinder != null && mBinder.isHasAuthorize(mPackageName)) {
                mBinder.WriteStatistisMessage(mPackageName, mVerName, msg);
                Log.i(TAG, "writeStatistisMessage   msg=" + msg);
            } else if (mBinder == null) {
                start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "writeStatistisMessage() e=" + e.toString());
        }
    }

    public boolean setUserImprovementState(boolean value) {
        if (mBinder != null) {
            try {
                mBinder.setUserImprovementState(value);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, e.toString());
            }
        }
        return true;
    }

    public IStatisticsInterface getBinder() {
        return mBinder;
    }
}
