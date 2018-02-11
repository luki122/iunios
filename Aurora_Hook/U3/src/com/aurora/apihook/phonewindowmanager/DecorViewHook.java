/**
 * All rights owned by IUNI
 */
package com.aurora.apihook.phonewindowmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.InputChannel;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.IWindowManager;
import android.view.WindowManager;
import android.widget.Toast;

// IPC
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.content.ComponentName;
import static android.view.WindowManagerPolicy.WindowManagerFuncs;
import android.provider.Settings;

// steve.tang PointerEventListener instead InputEventReceiver
import android.view.WindowManagerPolicy.PointerEventListener;
import android.content.res.Resources;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.provider.Settings;
import android.graphics.Rect;
import java.lang.Runnable;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;
import android.content.res.Configuration;
import android.view.View;
import aurora.app.AuroraActivity;
// Aurora <Felix.Duan> <2014-7-10> New feature: ThreeFingerScreenshot
/**
 * TODO Be a touch event framework of all aurora gesture
 * TODO Apply a better state machine
 *
 * @Author Felix Duan
 * @Date 2014-7-4
 */

public class DecorViewHook {
	
	public void before_fitSystemWindows(MethodHookParam param){
		boolean aurora  = false;
		View decorview = (View)param.thisObject;
		if(decorview != null){
			Context context = decorview.getContext();
			if(context instanceof AuroraActivity){
				aurora = true;
			}
		}
		param.setResult(aurora);
		
	}
	
}
