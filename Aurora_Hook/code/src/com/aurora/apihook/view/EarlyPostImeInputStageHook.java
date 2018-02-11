package com.aurora.apihook.view;

import android.os.SystemProperties;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewRootImpl;

import com.aurora.apihook.Hook;
import com.aurora.apihook.ClassHelper;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;


/**
 * Hook AuroraPointerInterceptor feature on Android 4.3/4.4 (JELLY_BEAN_MR2/KITKAT)
 * @author: Felix.Duan
 * @date:   2014-10-28
 */
public class EarlyPostImeInputStageHook implements Hook {
    private static final String TAG = "EPIISHook";

    public void before_processPointerEvent(MethodHookParam param) {
        if (android.os.Build.VERSION.SDK_INT != 18
                && android.os.Build.VERSION.SDK_INT != 19  && android.os.Build.VERSION.SDK_INT != 21 
                && android.os.Build.VERSION.SDK_INT != 22){
            // !JELLY_BEAN_MR2 && !KITKAT
            return;
        }

        Object q = param.args[0];

        ViewRootImpl impl = (ViewRootImpl)ClassHelper.getSurroundingThis(param.thisObject);
        View mView = (View)ClassHelper.getObjectField(impl, "mView");
        int FINISH_HANDLED = ClassHelper.getIntField(param.thisObject, "FINISH_HANDLED");

        final MotionEvent event = (MotionEvent)ClassHelper.getObjectField(q, "mEvent");
        if (SystemProperties.getBoolean("sys.aurora.input.intercept", false)) {
            if (mView != null) {
                Log.d(TAG, "ViewRootImpl sys.aurora.input.intercept 1");
                event.setAction(MotionEvent.ACTION_CANCEL);
                mView.dispatchPointerEvent(event);
            }
            param.setResult(FINISH_HANDLED);
        }
    }

}
