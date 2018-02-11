package com.aurora.powersaver.launcher.util;

import android.os.Handler;
import android.util.Log;

public abstract class DebouncedClickAction {
    private static final String TAG = "PowerLauncher/DebouncedClickAction";
    private static final int DEFAULT_DEBOUNCE_TIME = 800;

    private boolean mRepetiveResponed = true;
    private int mDebounceTime = DEFAULT_DEBOUNCE_TIME;

    public void onClick() {
        if (mRepetiveResponed) {
            Log.d(TAG, "exec onClick event" + mDebounceTime);
            setRepetiveResponed(false);
            if (mDebounceTime >= 0) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "setRepetiveResponed in thread");
                        setRepetiveResponed(true);
                    }
                }, mDebounceTime);
            }
            debouncedAction();
        }
    }

    public abstract void debouncedAction();

    // 设置标志位，防止响应多次快速点击事件
    private void setRepetiveResponed(boolean canResponed) {
        mRepetiveResponed = canResponed;
    }
}
