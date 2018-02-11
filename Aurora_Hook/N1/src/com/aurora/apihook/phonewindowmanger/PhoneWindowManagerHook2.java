 package com.aurora.apihook.phonewindowmanger;
 
import android.database.ContentObserver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import com.android.internal.R;
import java.lang.reflect.Method;

import com.aurora.apihook.ClassHelper;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;
import com.aurora.apihook.touch.AuroraPointerInterceptor;
import com.aurora.apihook.Hook;
import android.view.WindowManagerPolicy.WindowManagerFuncs;
//Aurora <tongyh> <2014-10-30> full Screen drop-down begin
import static android.view.WindowManager.LayoutParams.*;
import android.view.InputChannel;
import android.view.WindowManagerGlobal;
import android.view.WindowManagerPolicy.WindowState;
import com.android.internal.policy.impl.BarController;
import android.os.Looper;
import android.view.WindowManager;
//Aurora <tongyh> <2014-10-30> full Screen drop-down end
/**
 * Hook AuroraPointerInterceptor feature
 * @author: Felix.Duan
 * @date:   2014-10-28
 */
public class PhoneWindowManagerHook2  implements Hook{
    private static final String TAG = "PhoneWinMgrHook2";
    //private static final String TAG = "felixxp";

    public void after_init(MethodHookParam param) {
        auroraPointerInterceptorInit(param);
        //Aurora <tongyh> <2014-10-30> full Screen drop-down begin
        auroraInitSystemGestures(param);
        //Aurora <tongyh> <2014-10-30> full Screen drop-down end

        // Aurora <Felix.Duan> <2015-2-2> <BEGIN> Re-support AuroraHook "has_navigation_bar" to Settings.System
        auroraInitNaviBarDetector(param);
        // Aurora <Felix.Duan> <2015-2-2> <END> Re-support AuroraHook "has_navigation_bar" to Settings.System
    }

    private void auroraPointerInterceptorInit(MethodHookParam param) {
        Log.d(TAG, "auroraPointerInterceptorInit");
        Context context = (Context)param.args[0];
        WindowManagerFuncs windowManagerFuncs = (WindowManagerFuncs) param.args[2];
        Handler handler = (Handler) ClassHelper.getObjectField(param.thisObject, "mHandler");

        new AuroraPointerInterceptor(context, windowManagerFuncs);

        // TODO check mHasNavigationBar state
        if (android.os.Build.VERSION.SDK_INT == 19) {
            // kitkat
            AuroraSettingsObserver observer = new AuroraSettingsObserver(context, handler, param.thisObject);
            observer.observe();
        }
    }

    class AuroraSettingsObserver extends ContentObserver {
        Context context;
        Object target;

        AuroraSettingsObserver(Context ctx, Handler handler, Object object) {
            super(handler);
            context = ctx;
            target = object;
        }

        void observe() {
            Log.d(TAG, "observe");
            ContentResolver resolver = context.getContentResolver();
            resolver.registerContentObserver(
                    Settings.System.getUriFor(AURORA_NAVI_KEY_HIDE),
                    false, this);
            ClassHelper.callMethod(target, "updateSettings");
        }

        @Override public void onChange(boolean selfChange) {
            Log.d(TAG, "onChange " + this);
            ClassHelper.callMethod(target, "updateSettings");
            ClassHelper.callMethod(target, "updateRotation", false);
        }
    }
	
	public void after_updateSettings(MethodHookParam param) {
        auroraUpdateHideSystemUi(param);
    }

    private static final String AURORA_NAVI_KEY_HIDE = "navigation_key_hide";
    private void auroraUpdateHideSystemUi(MethodHookParam param) {
        if (android.os.Build.VERSION.SDK_INT != 19) {
            // only for kitkat
            return;
        }
		Log.d(TAG, "auroraUpdateHideSystemUi()");

        // Get fields
        Context context = (Context) ClassHelper.getObjectField(param.thisObject, "mContext");
        int mLandscapeRotation = 0;  // default landscape rotation
        int mSeascapeRotation = 0;   // "other" landscape rotation, 180 degrees from mLandscapeRotation
        int mPortraitRotation = 0;   // default portrait rotation
        int mUpsideDownRotation = 0; // "other" portrait rotation
        mLandscapeRotation = ClassHelper.getIntField(param.thisObject, "mLandscapeRotation");
        mSeascapeRotation = ClassHelper.getIntField(param.thisObject, "mSeascapeRotation");
        mPortraitRotation = ClassHelper.getIntField(param.thisObject, "mPortraitRotation");
        mUpsideDownRotation = ClassHelper.getIntField(param.thisObject, "mUpsideDownRotation");

        int[] mNavigationBarHeightForRotation =
            (int[]) ClassHelper.getObjectField(param.thisObject, "mNavigationBarHeightForRotation");
        int[] mNavigationBarWidthForRotation =
            (int[]) ClassHelper.getObjectField(param.thisObject, "mNavigationBarWidthForRotation");

        int auroraHideSystemUi = Settings.System.getInt(
            context.getContentResolver(),
            AURORA_NAVI_KEY_HIDE, 0 /*default */);
        final Resources res = context.getResources();
        boolean hide = (auroraHideSystemUi != 0);

        // TODO These internal values probably are wrong on non-IUNI device
        if (!hide) {
            mNavigationBarHeightForRotation[mPortraitRotation] =
            mNavigationBarHeightForRotation[mUpsideDownRotation] =
                    res.getDimensionPixelSize(com.android.internal.R.dimen.navigation_bar_height);
            mNavigationBarHeightForRotation[mLandscapeRotation] =
            mNavigationBarHeightForRotation[mSeascapeRotation] = res.getDimensionPixelSize(
                    com.android.internal.R.dimen.navigation_bar_height_landscape);
            mNavigationBarWidthForRotation[mPortraitRotation] =
            mNavigationBarWidthForRotation[mUpsideDownRotation] =
            mNavigationBarWidthForRotation[mLandscapeRotation] =
            mNavigationBarWidthForRotation[mSeascapeRotation] =
                    res.getDimensionPixelSize(com.android.internal.R.dimen.navigation_bar_width);
        } else {
            mNavigationBarHeightForRotation[mPortraitRotation] =
            mNavigationBarHeightForRotation[mUpsideDownRotation] =
                    0;
            mNavigationBarHeightForRotation[mLandscapeRotation] =
            mNavigationBarHeightForRotation[mSeascapeRotation] =
                    0;
            mNavigationBarWidthForRotation[mPortraitRotation] =
            mNavigationBarWidthForRotation[mUpsideDownRotation] =
            mNavigationBarWidthForRotation[mLandscapeRotation] =
            mNavigationBarWidthForRotation[mSeascapeRotation] =
                    0;
        }
    }

    // debug helper
    //public void before_auroraSetupPointerInterceptor(MethodHookParam param) {
    //        Log.d(TAG, "before_auroraSetupPointerInterceptor");
    //        param.setResult(null);
    //}
    
    //Aurora <tongyh> <2014-10-30> full Screen drop-down begin
    private AuroraSystemGesturesPointerEventListener auroraSystemGestures;

	private void auroraInitSystemGestures(MethodHookParam param) {
		if (android.os.Build.VERSION.SDK_INT >= 19) {
            return;
        }
		Context context = (Context) param.args[0];
		WindowManagerFuncs mWindowManagerFuncs = (WindowManagerFuncs) ClassHelper
				.getObjectField(param.thisObject, "mWindowManagerFuncs");

		InputChannel inputChannel = null;
		Method monitorInput = ClassHelper.findMethodBestMatch(mWindowManagerFuncs.getClass(),
				"monitorInput", String.class);
		if(monitorInput != null){
			inputChannel = (InputChannel) ClassHelper.callMethod(mWindowManagerFuncs, "monitorInput", "AuroraSystemUiInputChannel");
			auroraSystemGestures = new AuroraSystemGesturesPointerEventListener(
					context,
					inputChannel,
					Looper.myLooper());
		}
		
	}

	public void before_prepareAddWindowLw(MethodHookParam param) {
		if (android.os.Build.VERSION.SDK_INT >= 19) {
            return;
        }
		WindowState win = (WindowState) param.args[0];
		WindowManager.LayoutParams attrs = (WindowManager.LayoutParams) param.args[1];
		Context mContext = (Context) ClassHelper.getObjectField(
				param.thisObject, "mContext");
		WindowState mStatusBar = (WindowState) ClassHelper.getObjectField(
				param.thisObject, "mStatusBar");
		WindowState mKeyguard = (WindowState) ClassHelper.getObjectField(
				param.thisObject, "mKeyguard");
		WindowState mNavigationBar = (WindowState) ClassHelper.getObjectField(
				param.thisObject, "mNavigationBar");

		switch (attrs.type) {
		case TYPE_STATUS_BAR:
			mContext.enforceCallingOrSelfPermission(
					android.Manifest.permission.STATUS_BAR_SERVICE,
					"PhoneWindowManager");
			if(auroraSystemGestures != null){
				auroraSystemGestures.setWindow(win);
			}
			break;
		}
	}
	
	//add for systemUI crash后，屏幕不响应事件
	public void before_removeWindowLw(MethodHookParam param) {
		WindowState win = (WindowState) param.args[0];
		WindowState mStatusBar = (WindowState) ClassHelper.getObjectField(param.thisObject, "mStatusBar");
		if (mStatusBar == win && android.os.Build.VERSION.SDK_INT < 19) {
			if(auroraSystemGestures != null){
				auroraSystemGestures.setWindow(null);
			}
		}
		if (android.os.Build.VERSION.SDK_INT >= 19) {
			BarController mStatusBarController = (BarController) ClassHelper.getObjectField(param.thisObject, "mStatusBarController");
			WindowState mKeyguard = (WindowState) ClassHelper.getObjectField(param.thisObject, "mKeyguard");
			WindowState mKeyguardScrim = (WindowState) ClassHelper.getObjectField(param.thisObject, "mKeyguardScrim");
			WindowState mNavigationBar = (WindowState) ClassHelper.getObjectField(param.thisObject, "mNavigationBar");
			BarController mNavigationBarController = (BarController) ClassHelper.getObjectField(param.thisObject, "mNavigationBarController");
			if (mStatusBar == win) {
	            //mStatusBar = null;
	            ClassHelper.setObjectField(param.thisObject, "mStatusBar",null);
	            mStatusBarController.setWindow(null);
	        } else if (mKeyguard == win) {
	            Log.v(TAG, "Removing keyguard window (Did it crash?)");
	            //mKeyguard = null;
	            ClassHelper.setObjectField(param.thisObject, "mKeyguard",null);
	            //mKeyguardDelegate.showScrim();
	        } else if (mKeyguardScrim == win) {
	            Log.v(TAG, "Removing keyguard scrim");
	            //mKeyguardScrim = null;
	            ClassHelper.setObjectField(param.thisObject, "mKeyguardScrim",null);
	        } if (mNavigationBar == win) {
	            //mNavigationBar = null;
	            ClassHelper.setObjectField(param.thisObject, "mNavigationBar",null);
	            mNavigationBarController.setWindow(null);
	        }
	        param.setResult(null);
		}
		Log.d(TAG, TAG+"---before_removeWindowLw...end");
	}
	//Aurora <tongyh> <2014-10-30> full Screen drop-down end

    // Aurora <Felix.Duan> <2015-1-16> <BEGIN> AuroraHook "has_navigation_bar" to Settings.System
    public void after_setInitialDisplaySize(MethodHookParam param) {
        // Causing BUG #11424

		//boolean hasNavigationBar = ClassHelper.getBooleanField(
		//		param.thisObject, "mHasNavigationBar");
        //Log.d(TAG, "after_setInitialDisplaySize() hasNavigationBar = " + hasNavigationBar);
        //Context context = (Context) ClassHelper.getObjectField(param.thisObject, "mContext");
        //if(context != null){
        //Settings.System.putInt(context.getContentResolver(),
        //    "has_navigation_bar",
        //    (hasNavigationBar)?1:0);
        //}
    	Context context = (Context) ClassHelper.getObjectField(param.thisObject, "mContext");
    	if(context != null){
    	    int statusbarHeight = context.getResources().getDimensionPixelSize(com.aurora.R.dimen.status_bar_height);
    	    ClassHelper.setIntField(param.thisObject,"mStatusBarHeight", statusbarHeight);
    	}
    }
    // Aurora <Felix.Duan> <2015-1-16> <END> AuroraHook "has_navigation_bar" to Settings.System

    // Aurora <Felix.Duan> <2015-2-2> <BEGIN> Re-support AuroraHook "has_navigation_bar" to Settings.System
    private void auroraInitNaviBarDetector(MethodHookParam param) {
      Context context = (Context)param.args[0];
      IntentFilter filter = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
      context.registerReceiver(new BootCompletedReceiver(param), filter);
    }

    private class BootCompletedReceiver extends BroadcastReceiver {
        private MethodHookParam mParam;

        public BootCompletedReceiver(MethodHookParam param) {
            this.mParam = param;
        }

        public void onReceive(Context context, Intent intent) {
            boolean hasNaviBar = ClassHelper.getBooleanField(mParam.thisObject, "mHasNavigationBar");
            Log.d("PhoneWinMgrHook2", "onReceive hasNavigationBar = " + hasNaviBar);
            if (context != null) {
                ContentResolver resolver = context.getContentResolver();
                Settings.System.putInt(resolver, "has_navigation_bar", (hasNaviBar?1:0));
            }
        }
    }
    // Aurora <Felix.Duan> <2015-2-2> <END> Re-support AuroraHook "has_navigation_bar" to Settings.System
}
