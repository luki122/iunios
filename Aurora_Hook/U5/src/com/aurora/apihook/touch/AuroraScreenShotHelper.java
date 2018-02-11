package com.aurora.apihook.touch;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.UserHandle;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;

public class AuroraScreenShotHelper implements AuroraITakeScreenShot{

	private Context mContext;
	private Handler mHandler = new Handler();

	private static final float KEYGUARD_SCREENSHOT_CHORD_DELAY_MULTIPLIER = 2.5f;

	final Object mScreenshotLock = new Object();
	ServiceConnection mScreenshotConnection = null;

	public AuroraScreenShotHelper(Context mContext) {
		super();
		this.mContext = mContext;
	}

	final Runnable mScreenshotTimeout = new Runnable() {
		@Override
		public void run() {
			synchronized (mScreenshotLock) {
				if (mScreenshotConnection != null) {
					mContext.unbindService(mScreenshotConnection);
					mScreenshotConnection = null;
				}
			}
		}
	};

	private final Runnable mScreenshotRunnable = new Runnable() {
		@Override
		public void run() {
			startScreenshot();
		}
	};

	public void takeScreenShot() {
		cancelPendingScreenshotChordAction();
		mHandler.postDelayed(mScreenshotRunnable,
				getScreenshotChordLongPressDelay());

	}

	private void cancelPendingScreenshotChordAction() {
		mHandler.removeCallbacks(mScreenshotRunnable);
	}

	private long getScreenshotChordLongPressDelay() {
		/*
		 * if (mKeyguardDelegate.isShowing()) { // Double the time it
		 * takes to take a screenshot from // the keyguard return (long)
		 * (KEYGUARD_SCREENSHOT_CHORD_DELAY_MULTIPLIER *
		 * ViewConfiguration .getGlobalActionKeyTimeout()); }
		 */
		return 20;
		//return ViewConfiguration.getGlobalActionKeyTimeout();
	}

	private void startScreenshot() {
		synchronized (mScreenshotLock) {
			if (mScreenshotConnection != null) {
				return;
			}
			ComponentName cn = new ComponentName(
					"com.android.systemui",
					"com.android.systemui.screenshot.TakeScreenshotService");
			Intent intent = new Intent();
			intent.setComponent(cn);
			ServiceConnection conn = new ServiceConnection() {
				@Override
				public void onServiceConnected(
						ComponentName name,
						IBinder service) {
					synchronized (mScreenshotLock) {
						if (mScreenshotConnection != this) {
							return;
						}
						Messenger messenger = new Messenger(
								service);
						Message msg = Message.obtain(
								null, 1);
						final ServiceConnection myConn = this;
						Handler h = new Handler(
								mHandler.getLooper()) {
							@Override
							public void handleMessage(
									Message msg) {
								synchronized (mScreenshotLock) {
									if (mScreenshotConnection == myConn) {
										mContext.unbindService(mScreenshotConnection);
										mScreenshotConnection = null;
										mHandler.removeCallbacks(mScreenshotTimeout);
									}
								}
							}
						};
						msg.replyTo = new Messenger(h);
						msg.arg1 = msg.arg2 = 0;
						/*
						 * if (mStatusBar != null &&
						 * mStatusBar.isVisibleLw())
						 * msg.arg1 = 1; if
						 * (mNavigationBar != null &&
						 * mNavigationBar
						 * .isVisibleLw()) msg.arg2 = 1;
						 */
						try {
							messenger.send(msg);
						} catch (RemoteException e) {
						}
					}
				}

				@Override
				public void onServiceDisconnected(
						ComponentName name) {
				}
			};

			if (mContext.bindService(intent, conn, Context.BIND_AUTO_CREATE)) {
				mScreenshotConnection = conn;
				mHandler.postDelayed(mScreenshotTimeout, 10000);
			}
		}
	}
}
