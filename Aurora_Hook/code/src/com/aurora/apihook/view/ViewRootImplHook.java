package com.aurora.apihook.view;

import android.os.SystemProperties;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.InputEventConsistencyVerifier;

import com.aurora.apihook.Hook;
import com.aurora.apihook.ClassHelper;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;


/**
 * Hook AuroraPointerInterceptor feature on Android 4.2 (JELLY_BEAN_MR1)
 * @author: Felix.Duan
 * @date:   2014-10-28
 */
public class ViewRootImplHook implements Hook{
    private static final String TAG = "ViewRootImplHook";

    public void before_deliverPointerEvent(MethodHookParam param){
        if (android.os.Build.VERSION_CODES.JELLY_BEAN_MR1
                != android.os.Build.VERSION.SDK_INT)
            return;

        if (SystemProperties.getBoolean("sys.aurora.input.intercept", false)) {

            // Reflect fields
            Object q = param.args[0];

            InputEventConsistencyVerifier mInputEventConsistencyVerifier =
                (InputEventConsistencyVerifier)ClassHelper.getObjectField(param.thisObject,
                    "mInputEventConsistencyVerifier");
            View mView = (View) ClassHelper.getObjectField(param.thisObject, "mView");
            boolean mAdded = ClassHelper.getBooleanField(param.thisObject, "mAdded");

            final MotionEvent event = (MotionEvent)ClassHelper.getObjectField(q, "mEvent");
            final boolean isTouchEvent = event.isTouchEvent();
        
            if (mInputEventConsistencyVerifier != null) {
                if (isTouchEvent) {
                    mInputEventConsistencyVerifier.onTouchEvent(event, 0);
                } else {
                    mInputEventConsistencyVerifier.onGenericMotionEvent(event, 0);
                }
            }

            // If there is no view, then the event will not be handled.
            if (mView == null || !mAdded) {
                ClassHelper.callMethod(param.thisObject, "finishInputEvent", q, false);
                param.setResult(null);
            }

            // Aurora core function
            boolean handled = false;
            if (mView != null) {
                Log.d(TAG, "aurora input intercept");
                event.setAction(3);
                handled = mView.dispatchPointerEvent(event);
            }
            ClassHelper.callMethod(param.thisObject, "finishInputEvent", q, handled);
            param.setResult(null);
        }
    }

    // debug helper
    //public void before_auroraDeliverPointerEvent(MethodHookParam param) {
    //    //param.setResult(null);
    //}
}
