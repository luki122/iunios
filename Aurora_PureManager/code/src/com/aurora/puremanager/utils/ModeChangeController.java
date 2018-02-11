package com.aurora.puremanager.utils;

import android.content.Context;
import android.content.Intent;

public class ModeChangeController {
    private StateController mStateController;
    private Context mContext;
    private static final String TAG = "SystemManager/ModeChangeController";
    public static final String EVENT_MODE_CHANGED = "com.aurora.puremanager.utils.MODE_CHANGED";

    public ModeChangeController(Context context) {
        mContext = context;
        mStateController = new StateController(mContext);
    }

    public void savePreviousMode(int mode) {
        switch (mode) {
            case Consts.NONE_MODE:
                Log.e(TAG, "ModeChangeController$savePreviousMode NONE_MODE");
                mStateController.saveState(Consts.NONE_POWER);
                break;
            case Consts.NORMAL_MODE:
                Log.e(TAG, "ModeChangeController$savePreviousMode NORMAL_MODE");
                break;
            case Consts.SUPER_MODE:
                Log.e(TAG, "ModeChangeController$savePreviousMode SUPER_MODE");
                mStateController.exitSuperMode();
                break;
            default:
                break;
        }
    }

    public void startCurrentMode(int from, int to) {
        Intent intent = new Intent(EVENT_MODE_CHANGED);
        switch (to) {
            case Consts.NONE_MODE:
                Log.e(TAG, "ModeChangeController$startCurrentMode NONE_MODE");
                mStateController.restoreNoneModeState(from);
                mContext.sendBroadcast(intent);
                break;
            case Consts.NORMAL_MODE:
                Log.e(TAG, "ModeChangeController$startCurrentMode NORMAL_MODE");
                mContext.sendBroadcast(intent);
                break;
            case Consts.SUPER_MODE:
                Log.e(TAG, "ModeChangeController$startCurrentMode SUPER_MODE");
                mStateController.startSuperMode();
                mContext.sendBroadcast(intent);
                break;
            default:
                break;
        }
    }

}
