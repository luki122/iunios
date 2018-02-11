package com.aurora.apihook.phonewindowmanger;

import java.lang.reflect.Method;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IRemoteCallback;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.EventLog;
import android.util.Log;

import com.aurora.apihook.ClassHelper;
import com.aurora.apihook.Hook;
import com.aurora.apihook.keyguard.AuroraKeyguardServiceDelegate;

import com.aurora.apihook.XC_MethodHook.MethodHookParam;
import android.view.KeyEvent;
import android.view.IWindowManager;
import android.view.WindowManagerPolicy;
import android.view.WindowManagerPolicy.OnKeyguardExitResult;
import android.view.WindowManagerPolicy.WindowState;
import static android.view.WindowManagerPolicy.WindowManagerFuncs.LID_OPEN;
import static android.view.WindowManagerPolicy.WindowManagerFuncs.LID_CLOSED;
import com.android.internal.statusbar.IStatusBarService;
import android.app.ActivityManagerNative;
import com.android.internal.policy.impl.BarController;
import java.util.ArrayList;

public class PhoneWindowManagerHookForKeyguard implements Hook{
	private static final String TAG = "PhoneWindowManagerHookForKeyguard";
	public static final String HOOK_CLASS_NAME = "com.android.internal.policy.impl.PhoneWindowManager";
	
	public AuroraKeyguardServiceDelegate mKeyguardDelegate;
	
	//modify for samsung 4.4
	public void before_systemReady(final MethodHookParam param) {
		Log.d(TAG, TAG+"---before_systemReady...start...");
		boolean mHeadless = ClassHelper.getBooleanField(param.thisObject, "mHeadless");
		Context context = (Context) ClassHelper.getObjectField(param.thisObject, "mContext");
		Log.d(TAG, context+"---before_systemReady...mHeadless="+mHeadless);
    	if (!mHeadless) {
    		if (mKeyguardDelegate == null) {
    			mKeyguardDelegate = new AuroraKeyguardServiceDelegate(context, null);
			}
    		mKeyguardDelegate.onSystemReady();
        }
    	String device = Build.BRAND;
		if (isNeedReflect()) {
			Log.d(TAG, TAG+"---before_systemReady...end...samsung");  
		}else {
			Object mLock = ClassHelper.getObjectField(param.thisObject, "mLock");
		    Handler mHandler = (Handler) ClassHelper.getObjectField(param.thisObject, "mHandler");
		    synchronized (mLock) {
	            //updateOrientationListenerLp();
	    		ClassHelper.callMethod(callSuper(param.thisObject), param.thisObject, "updateOrientationListenerLp");
	            //mSystemReady = true;
	            ClassHelper.setBooleanField(param.thisObject, "mSystemReady",true);
	            mHandler.post(new Runnable() {
	                public void run() {
	                    //updateSettings();
	                    ClassHelper.callMethod(callSuper(param.thisObject), param.thisObject, "updateSettings");
	                }
	            });
	        }
		    Log.d(TAG, TAG+"---before_systemReady...end2..."+device); 
		    param.setResult(null);
		}
	}
	
	public void before_screenTurningOn(MethodHookParam param) {
    	EventLog.writeEvent(70000, 1);
        if (false) {
            RuntimeException here = new RuntimeException("here");
            here.fillInStackTrace();
            Log.d(TAG, "Screen turning on...", here);
        }
        
		Object mLock = ClassHelper.getObjectField(param.thisObject, "mLock");
        synchronized (mLock) {
            //mScreenOnEarly = true;
            ClassHelper.setBooleanField(param.thisObject, "mScreenOnEarly",true);
            //updateOrientationListenerLp();
            ClassHelper.callMethod(callSuper(param.thisObject), param.thisObject, "updateOrientationListenerLp");
            //updateLockScreenTimeout();
            try{
	        	 Class<?> clz = Class.forName(HOOK_CLASS_NAME);
	        	 Method updateLockScreenTimeout = clz.getDeclaredMethod("updateLockScreenTimeout");
	        	 updateLockScreenTimeout.setAccessible(true);
	        	 updateLockScreenTimeout.invoke(param.thisObject);
	        	 //ClassHelper.callMethod(callSuper(param.thisObject), param.thisObject, "updateLockScreenTimeout");
	        }catch(Exception e){
	        	e.printStackTrace();
	        }
        }

        waitForKeyguard(param, (WindowManagerPolicy.ScreenOnListener) param.args[0]);
        Log.d(TAG, TAG+"---before_screenTurningOn...end");
        param.setResult(null);
	}
	
	public void before_screenTurnedOff(MethodHookParam param) {
    	EventLog.writeEvent(70000, 0);
    	Object mLock = ClassHelper.getObjectField(param.thisObject, "mLock");
        synchronized (mLock) {
            //mScreenOnEarly = false;
        	ClassHelper.setBooleanField(param.thisObject, "mScreenOnEarly",false);
            //mScreenOnFully = false;
        	ClassHelper.setBooleanField(param.thisObject, "mScreenOnFully",false);
        }
        if (mKeyguardDelegate != null) {
        	mKeyguardDelegate.onScreenTurnedOff((Integer) param.args[0]);
        }
        synchronized (mLock) {
            //updateOrientationListenerLp();
        	ClassHelper.callMethod(callSuper(param.thisObject),param.thisObject, "updateOrientationListenerLp");
            //updateLockScreenTimeout();
        	try{
	        	 Class<?> clz = Class.forName(HOOK_CLASS_NAME);
	        	 Method updateLockScreenTimeout = clz.getDeclaredMethod("updateLockScreenTimeout");
	        	 updateLockScreenTimeout.setAccessible(true);
	        	 updateLockScreenTimeout.invoke(param.thisObject);
	        	 //ClassHelper.callMethod(callSuper(param.thisObject),param.thisObject, "updateLockScreenTimeout");
	        }catch(Exception e){
	        	e.printStackTrace();
	        }
        }
        Log.d(TAG, TAG+"---before_screenTurnedOff...end");
        param.setResult(null);
	}
	
	//for 锁屏页面出现通知栏黑条及可以上拉多任务
	public void before_keyguardIsShowingTq(MethodHookParam param) {
		if (mKeyguardDelegate == null) {
			param.setResult(false);
		}else {
			param.setResult(mKeyguardDelegate.isShowingAndNotHidden());
		}
	}
	
	public void before_inKeyguardRestrictedKeyInputMode(MethodHookParam param) {
		if (mKeyguardDelegate == null) {
			param.setResult(false);
		}else {
			param.setResult(mKeyguardDelegate.isInputRestricted());
		}
	}
	
	//for 从通知栏进入锁屏不会自动解锁
	public void before_dismissKeyguardLw(MethodHookParam param) {
		Log.d(TAG, TAG+"---before_inKeyguardRestrictedKeyInputMode..."+mKeyguardDelegate);
		Handler mHandler = (Handler) ClassHelper.getObjectField(param.thisObject, "mHandler");
		if (mKeyguardDelegate != null && mKeyguardDelegate.isShowing()) { 
            mHandler.post(new Runnable() {
                public void run() {
                    if (mKeyguardDelegate.isDismissable()) {
                        // Can we just finish the keyguard straight away?
                        mKeyguardDelegate.keyguardDone(false, true);
                    } else {
                        // ask the keyguard to prompt the user to authenticate if necessary
                        mKeyguardDelegate.dismiss();
                    }
                }
            });
        }
	}
	
	//for 锁屏界面会响应HOME键
	public void before_launchHomeFromHotKey(final MethodHookParam param) {
		boolean mHideLockScreen = ClassHelper.getBooleanField(param.thisObject, "mHideLockScreen");
		Log.d(TAG, "---before_launchHomeFromHotKey..."+mHideLockScreen+"..."+mKeyguardDelegate.isShowingAndNotHidden()+"..."+mKeyguardDelegate.isInputRestricted());
		if (mKeyguardDelegate != null && mKeyguardDelegate.isShowingAndNotHidden()) {
            // don't launch home if keyguard showing
        } else if (!mHideLockScreen && mKeyguardDelegate.isInputRestricted()) {
        	Log.d(TAG, "2---before_launchHomeFromHotKey...");
            // when in keyguard restricted mode, must first verify unlock
            // before launching home
        	mKeyguardDelegate.verifyUnlock(new OnKeyguardExitResult() {
                public void onKeyguardExitResult(boolean success) {
                    if (success) {
                        try {
                            ActivityManagerNative.getDefault().stopAppSwitches();
                        } catch (RemoteException e) {
                        }
                        //sendCloseSystemWindows(SYSTEM_DIALOG_REASON_HOME_KEY);
                        ClassHelper.callMethod(callSuper(param.thisObject),param.thisObject, "sendCloseSystemWindows", "homekey");
                        //startDockOrHome();
                        ClassHelper.callMethod(callSuper(param.thisObject),param.thisObject, "startDockOrHome");
                    }
                }
            });
        } else {
        	Log.d(TAG, "3---before_launchHomeFromHotKey...");
            // no keyguard stuff to worry about, just launch home!
            try {
                ActivityManagerNative.getDefault().stopAppSwitches();
            } catch (RemoteException e) {
            }
            //sendCloseSystemWindows(SYSTEM_DIALOG_REASON_HOME_KEY);
            ClassHelper.callMethod(callSuper(param.thisObject),param.thisObject, "sendCloseSystemWindows", "homekey");
            //startDockOrHome();
            ClassHelper.callMethod(callSuper(param.thisObject),param.thisObject, "startDockOrHome");
        }
		param.setResult(null);
	}
	
	//for 拨号中两次短按power键后，再按返回键，会自动挂电，页面进入pin码解锁页面
	public void after_finishPostLayoutPolicyLw(MethodHookParam param) {
		Log.d(TAG, TAG+"---after_finishPostLayoutPolicyLw..."+mKeyguardDelegate);
		int DISMISS_KEYGUARD_NONE = 0; // Keyguard not being dismissed.
	    int DISMISS_KEYGUARD_START = 1; // Keyguard needs to be dismissed.
		boolean mHideLockScreen = ClassHelper.getBooleanField(param.thisObject, "mHideLockScreen");
		int mDismissKeyguard = ClassHelper.getIntField(param.thisObject, "mDismissKeyguard");
		WindowState mKeyguard = (WindowState) ClassHelper.getObjectField(param.thisObject, "mKeyguard");
		Handler mHandler = (Handler) ClassHelper.getObjectField(param.thisObject, "mHandler");
		// Hide the key guard if a visible window explicitly specifies that it wants to be
        // displayed when the screen is locked.
        if (mKeyguard != null && mKeyguardDelegate != null) {
            if (mDismissKeyguard != DISMISS_KEYGUARD_NONE && !mKeyguardDelegate.isSecure()) {
                
                if (mKeyguardDelegate.isShowing()) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                        	mKeyguardDelegate.keyguardDone(false, false);
                        }
                    });
                }
            } else if (mHideLockScreen) {
                mKeyguardDelegate.setHidden(true);
            } else if (mDismissKeyguard != DISMISS_KEYGUARD_NONE) {
                // This is the case of keyguard isSecure() and not mHideLockScreen.
                if (mDismissKeyguard == DISMISS_KEYGUARD_START) {
                	mKeyguardDelegate.setHidden(false);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                        	mKeyguardDelegate.dismiss();
                        }
                    });
                }
            } else {
            	mKeyguardDelegate.setHidden(false);
            }
        }
	}
	
	//add for systemUI crash后，屏幕不响应事件
	/*public void before_removeWindowLw(MethodHookParam param) {
		WindowState mStatusBar = (WindowState) ClassHelper.getObjectField(param.thisObject, "mStatusBar");
		BarController mStatusBarController = (BarController) ClassHelper.getObjectField(param.thisObject, "mStatusBarController");
		WindowState mKeyguard = (WindowState) ClassHelper.getObjectField(param.thisObject, "mKeyguard");
		WindowState mKeyguardScrim = (WindowState) ClassHelper.getObjectField(param.thisObject, "mKeyguardScrim");
		WindowState mNavigationBar = (WindowState) ClassHelper.getObjectField(param.thisObject, "mNavigationBar");
		BarController mNavigationBarController = (BarController) ClassHelper.getObjectField(param.thisObject, "mNavigationBarController");
		if (mStatusBar == (WindowState)param.args[0]) {
            //mStatusBar = null;
            ClassHelper.setObjectField(param.thisObject, "mStatusBar",null);
            mStatusBarController.setWindow(null);
        } else if (mKeyguard == (WindowState)param.args[0]) {
            Log.v(TAG, "Removing keyguard window (Did it crash?)");
            //mKeyguard = null;
            ClassHelper.setObjectField(param.thisObject, "mKeyguard",null);
            //mKeyguardDelegate.showScrim();
        } else if (mKeyguardScrim == (WindowState)param.args[0]) {
            Log.v(TAG, "Removing keyguard scrim");
            //mKeyguardScrim = null;
            ClassHelper.setObjectField(param.thisObject, "mKeyguardScrim",null);
        } if (mNavigationBar == (WindowState)param.args[0]) {
            //mNavigationBar = null;
            ClassHelper.setObjectField(param.thisObject, "mNavigationBar",null);
            mNavigationBarController.setWindow(null);
        }
        Log.d(TAG, TAG+"---before_removeWindowLw...end");
        param.setResult(null);
	}*/
	
	public void before_isKeyguardSecure(MethodHookParam param) {
		Log.d(TAG, TAG+"---before_isKeyguardSecure..."+mKeyguardDelegate);
		if (mKeyguardDelegate == null) {
			param.setResult(false);
		}else {
			param.setResult(mKeyguardDelegate.isSecure());
		}
	}
	
	public void before_exitKeyguardSecurely(MethodHookParam param) {
		if (mKeyguardDelegate != null) {
			mKeyguardDelegate.verifyUnlock((OnKeyguardExitResult)param.args[0]);
		}
		param.setResult(null);
	}
	
	public void before_enableKeyguard(MethodHookParam param) {
		if (mKeyguardDelegate != null) {
			Log.d(TAG, TAG+"---before_enableKeyguard..."+param.args);
			if (param.args != null) {
				mKeyguardDelegate.setKeyguardEnabled((Boolean) param.args[0]);
			}else {
				mKeyguardDelegate.setKeyguardEnabled(false);
			}
		}
		param.setResult(null);
	}
	
	public void before_notifyLidSwitchChanged(MethodHookParam param) {
		Log.d(TAG, TAG+"---before_notifyLidSwitchChanged..."+mKeyguardDelegate);
		boolean mHeadless = ClassHelper.getBooleanField(param.thisObject, "mHeadless");
		int mLidState = ClassHelper.getIntField(param.thisObject, "mLidState");
		PowerManager mPowerManager = (PowerManager) ClassHelper.getObjectField(param.thisObject, "mPowerManager");
		boolean mLidControlsSleep = ClassHelper.getBooleanField(param.thisObject, "mLidControlsSleep");
		// do nothing if headless
        if (mHeadless) param.setResult(null);
        boolean lidOpen = (Boolean) param.args[1];
        // lid changed state
        final int newLidState = lidOpen ? LID_OPEN : LID_CLOSED;
        if (newLidState == mLidState) {
        	param.setResult(null);
        }

        mLidState = newLidState;
        //applyLidSwitchState();
        ClassHelper.callMethod(callSuper(param.thisObject),param.thisObject, "applyLidSwitchState");
        //updateRotation(true);
        ClassHelper.callMethod(callSuper(param.thisObject),param.thisObject, "updateRotation", true);

        if (lidOpen) {
            if (keyguardIsShowingTq()) {
                mKeyguardDelegate.auroraWakeWhenReady(KeyEvent.KEYCODE_POWER);
            } else {
                mPowerManager.wakeUp(SystemClock.uptimeMillis());
            }
        } else if (!mLidControlsSleep) {
            mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
        }
		param.setResult(null);
	}
	
	public void before_interceptMotionBeforeQueueingWhenScreenOff(MethodHookParam param) {
		int result = 0;
		int policyFlags = (Integer) param.args[0];
        final boolean isWakeMotion = (policyFlags
                & (WindowManagerPolicy.FLAG_WAKE | WindowManagerPolicy.FLAG_WAKE_DROPPED)) != 0;
        if (isWakeMotion) {
            if (mKeyguardDelegate != null && mKeyguardDelegate.isShowing()) {
                // If the keyguard is showing, let it decide what to do with the wake motion.
            	mKeyguardDelegate.auroraWakeWhenReady(KeyEvent.KEYCODE_UNKNOWN);
            } else {
                // Otherwise, wake the device ourselves.
                result |= WindowManagerPolicy.ACTION_WAKE_UP;
            }
        }
        Log.d(TAG, TAG+"---before_interceptMotionBeforeQueueingWhenScreenOff...end");
		param.setResult(result);
	}
	
	public void before_setCurrentUserLw(MethodHookParam param) {
		int newUserId = (Integer) param.args[0];
		if (mKeyguardDelegate != null) {
			mKeyguardDelegate.setCurrentUser(newUserId);
        }
		IStatusBarService mStatusBarService = (IStatusBarService) ClassHelper.getObjectField(param.thisObject, "mStatusBarService");
        if (mStatusBarService != null) {
            try {
                mStatusBarService.setCurrentUser(newUserId);
            } catch (RemoteException e) {
                // oh well
            }
        }
        //setLastInputMethodWindowLw(null, null);
        ClassHelper.callMethod(callSuper(param.thisObject), param.thisObject, "setLastInputMethodWindowLw", "null", "null");
		 Log.d(TAG, TAG+"---before_setCurrentUserLw...end");
		 param.setResult(null);
	}

	private boolean callSuper(Object obj){
		return ClassHelper.needCallSuperMethod(obj, "com.android.internal.policy.impl.PhoneWindowManager");
	}
	
	public void before_showAssistant(MethodHookParam param) {
		if (mKeyguardDelegate != null) {
			mKeyguardDelegate.showAssistant();
		}
		param.setResult(null);
	}
	
	private void waitForKeyguard(final MethodHookParam param,
			final WindowManagerPolicy.ScreenOnListener screenOnListener) {
		if (mKeyguardDelegate != null) {
			Log.d(TAG, TAG + "---waitForKeyguard...start..." + mKeyguardDelegate.isShowing());
			if (screenOnListener != null) {
				mKeyguardDelegate
						.onScreenTurnedOn(new AuroraKeyguardServiceDelegate.ShowListener() {
							@Override
							public void onShown(IBinder windowToken) {
								//waitForKeyguardWindowDrawn(windowToken, screenOnListener);
								try {
									Log.d(TAG, "1---waitForKeyguardWindowDrawn...");
									Class<?> clz = Class.forName(HOOK_CLASS_NAME);
									Method waitForKeyguardWindowDrawn = clz
											.getDeclaredMethod("waitForKeyguardWindowDrawn", IBinder.class, WindowManagerPolicy.ScreenOnListener.class);
									waitForKeyguardWindowDrawn.setAccessible(true);
									waitForKeyguardWindowDrawn.invoke(param.thisObject, windowToken, screenOnListener);
									// ClassHelper.callMethod(callSuper(param.thisObject),param.thisObject, "waitForKeyguardWindowDrawn", windowToken, screenOnListener);
								} catch (Exception e) {
									e.printStackTrace();
									Log.d(TAG, "2---waitForKeyguardWindowDrawn...");
									waitForKeyguardWindowDrawn(param, windowToken, screenOnListener);
								}
							}
						});
				return;
			} else {
				mKeyguardDelegate.onScreenTurnedOn(null);
			}
		} else {
			Log.d(TAG, "No keyguard mediator!");
		}
		// finishScreenTurningOn(screenOnListener);
		ClassHelper.callMethod(callSuper(param.thisObject), param.thisObject, "finishScreenTurningOn",
				screenOnListener);
	}
	
	private void waitForKeyguardWindowDrawn(final MethodHookParam param, IBinder windowToken,
            final WindowManagerPolicy.ScreenOnListener screenOnListener) {
		final IWindowManager mWindowManager = (IWindowManager) ClassHelper.getObjectField(param.thisObject, "mWindowManager");
        if (windowToken != null) {
            try {
                if (mWindowManager.waitForWindowDrawn(
                        windowToken, new IRemoteCallback.Stub() {
                    @Override
                    public void sendResult(Bundle data) {
                        Log.d(TAG, "Lock screen displayed!");
                        finishScreenTurningOn(param, mWindowManager, screenOnListener);
                    }
                })) {
                    return;
                }
            } catch (RemoteException ex) {
                // Can't happen in system process.
            }
        }

        Log.d(TAG, "No lock screen!");
        finishScreenTurningOn(param, mWindowManager, screenOnListener);
    }

    private void finishScreenTurningOn(MethodHookParam param, IWindowManager windowManager, WindowManagerPolicy.ScreenOnListener screenOnListener) {
    	Object mLock = ClassHelper.getObjectField(param.thisObject, "mLock");
        synchronized (mLock) {
            //mScreenOnFully = true;
        	ClassHelper.setBooleanField(param.thisObject, "mScreenOnFully",true);
        }

        try {
            windowManager.setEventDispatching(true);
        } catch (RemoteException unhandled) {
        }

        if (screenOnListener != null) {
            screenOnListener.onScreenOn();
        }
    }
	
    private boolean keyguardIsShowingTq() {
        if (mKeyguardDelegate == null) return false;
        return mKeyguardDelegate.isShowingAndNotHidden();
    }
    
    //add for adjust device begin
    public static ArrayList<String> ARRAY_LIST = new ArrayList<String>(){{add("samsung"); add("d802");
	add("hammerhead");add("m7");add("dior");}};
    private boolean isNeedReflect(){
		String device = Build.BRAND;
		boolean isNeed = true;
		if (ARRAY_LIST.contains(device)&& Build.VERSION.SDK_INT > 18) {
			isNeed = false;
		}
		Log.i("xiejun","isNeed = "+isNeed);
		return isNeed;
	}
    //add for adjust device end
    
    
}
