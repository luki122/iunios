package com.android.phone;

import com.android.phone.PhoneGlobals;

import android.os.Handler;
import android.view.*;
import android.content.Context;
import android.view.WindowManager.LayoutParams;
import android.graphics.*;
public class FloatWindowManager {


	private static AuroraFloatView floatWindow;

	private static LayoutParams WindowParams;

	private static WindowManager mWindowManager;
		
	/**
	 * 创建一个大悬浮窗。位置为屏幕正中间。
	 * 
	 * @param context
	 *            必须为应用程序的Context.
	 */
	public static void createWindow(Context context) {
		WindowManager windowManager = getWindowManager(context);
		int screenWidth = windowManager.getDefaultDisplay().getWidth();
		int screenHeight = windowManager.getDefaultDisplay().getHeight();
		if (floatWindow == null) {
			floatWindow = new AuroraFloatView(context);
			if (WindowParams == null) {
				int marginTop = context.getResources().getDimensionPixelOffset(R.dimen.float_margin_top);  
				WindowParams = new LayoutParams();
				WindowParams.x = 0;
//				WindowParams.y = marginTop
				WindowParams.y = 0;
				WindowParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE 
						|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
						|WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
				WindowParams.type = LayoutParams.TYPE_TOP_MOST;
				WindowParams.format = PixelFormat.RGBA_8888;
				WindowParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
				WindowParams.width = AuroraFloatView.viewWidth;
				WindowParams.height = AuroraFloatView.viewHeight;
			}
			windowManager.addView(floatWindow, WindowParams);
		} else {
			updateUI();
		}
	}

	/**
	 * 将大悬浮窗从屏幕上移除。
	 * 
	 * @param context
	 *            必须为应用程序的Context.
	 */
	public static void removeWindow(Context context) {
		if (floatWindow != null) {
			final WindowManager windowManager = getWindowManager(context);
			floatWindow.HideAnimationV2();
		}
	}
	
	public static void removeWindowFinal(Context context) {
		if (floatWindow != null) {
			final WindowManager windowManager = getWindowManager(context);
			windowManager.removeView(floatWindow);
			floatWindow = null;
		}
	}
	
	private static Handler mHandler = new Handler();


	/**
	 * 是否有悬浮窗(包括小悬浮窗和大悬浮窗)显示在屏幕上。
	 * 
	 * @return 有悬浮窗显示在桌面上返回true，没有的话返回false。
	 */
	public static boolean isWindowShowing() {
		return floatWindow != null;
	}

	/**
	 * 如果WindowManager还未创建，则创建一个新的WindowManager返回。否则返回当前已创建的WindowManager。
	 * 
	 * @param context
	 *            必须为应用程序的Context.
	 * @return WindowManager的实例，用于控制在屏幕上添加或移除悬浮窗。
	 */
	private static WindowManager getWindowManager(Context context) {
		if (mWindowManager == null) {
			mWindowManager = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
		}
		return mWindowManager;
	}
	
	public static void updateUI() {
		if(isWindowShowing()) {
			floatWindow.updateUI();
		}
	} 



}