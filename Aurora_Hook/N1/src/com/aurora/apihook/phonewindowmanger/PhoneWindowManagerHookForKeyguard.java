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

public class PhoneWindowManagerHookForKeyguard implements Hook{
	
    
    
}
