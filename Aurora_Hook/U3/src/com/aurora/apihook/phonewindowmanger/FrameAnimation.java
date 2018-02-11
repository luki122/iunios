package com.aurora.apihook.phonewindowmanger;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Notification.Builder;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.View.OnLongClickListener;
import android.view.WindowManagerPolicy.WindowManagerFuncs;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.internal.policy.impl.EnableAccessibilityController;
import com.android.internal.policy.impl.PhoneWindowManager;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;

import android.view.WindowManagerPolicy.WindowManagerFuncs;

import com.android.server.power.ShutdownThread;
public  class FrameAnimation{

	public static final int ICON_SHUT_DOWN = 1;
	public static final int ICON_AIRMODE_DOWN = 2;
	public static final int ICON_REBOOT_DOWN = 3;

	public int mIconType;


	private Handler handler;
	private View view;
	private AnimationImageListener animationImageListener;

	private FrameCallback[] callbacks;
	private Drawable[] frames;
	private List<Drawable> mFrames;
	private ArrayList<Integer> mDrawables;
	private int[] durations;
	private int frameCount;

	private boolean isRun;
	private boolean fillAfter;
	private boolean isOneShot;
	private boolean isLimitless;
	private int repeatTime;

	private int currentRepeat;
	private int currentFrame;
	private int currentTime;
	private int mLoadDivider;
	private AnimationDrawable mAnimDrawable;
	private Context mContext;

	private int[] mAirmodePics = new int[]{
			com.aurora.R.drawable.aurora_airmode_normal,
/*			com.aurora.R.drawable.aurora_airmode004015,
			com.aurora.R.drawable.aurora_airmode004016,
			com.aurora.R.drawable.aurora_airmode004017,
			com.aurora.R.drawable.aurora_airmode004018,
			com.aurora.R.drawable.aurora_airmode004019,
			com.aurora.R.drawable.aurora_airmode004020,
			com.aurora.R.drawable.aurora_airmode004021,
			com.aurora.R.drawable.aurora_airmode004022,
			com.aurora.R.drawable.aurora_airmode004023,
			com.aurora.R.drawable.aurora_airmode004024,
			com.aurora.R.drawable.aurora_airmode004025,
			com.aurora.R.drawable.aurora_airmode004026,
			com.aurora.R.drawable.aurora_airmode004027,
			com.aurora.R.drawable.aurora_airmode004028,
			com.aurora.R.drawable.aurora_airmode004029,
			com.aurora.R.drawable.aurora_airmode004030,
			com.aurora.R.drawable.aurora_airmode004031,
			com.aurora.R.drawable.aurora_airmode004032,
			com.aurora.R.drawable.aurora_airmode004033,
			com.aurora.R.drawable.aurora_airmode004034,
			com.aurora.R.drawable.aurora_airmode004035,
			com.aurora.R.drawable.aurora_airmode004036,
			com.aurora.R.drawable.aurora_airmode004037,
			com.aurora.R.drawable.aurora_airmode004038,
			com.aurora.R.drawable.aurora_airmode004039,*/
			com.aurora.R.drawable.aurora_airmode004040
	};
	private int[] mShutdownPics = new int[]{
			com.aurora.R.drawable.aurora_shutdown_normal,
/*			com.aurora.R.drawable.aurora_shutdown0016,
			com.aurora.R.drawable.aurora_shutdown0017,
			com.aurora.R.drawable.aurora_shutdown0018,
			com.aurora.R.drawable.aurora_shutdown0019,
			com.aurora.R.drawable.aurora_shutdown0020,
			com.aurora.R.drawable.aurora_shutdown0021,
			com.aurora.R.drawable.aurora_shutdown0022,
			com.aurora.R.drawable.aurora_shutdown0023,
			com.aurora.R.drawable.aurora_shutdown0024,
			com.aurora.R.drawable.aurora_shutdown0025,
			com.aurora.R.drawable.aurora_shutdown0026,
			com.aurora.R.drawable.aurora_shutdown0027,
			com.aurora.R.drawable.aurora_shutdown0028,
			com.aurora.R.drawable.aurora_shutdown0029,
			com.aurora.R.drawable.aurora_shutdown0030,
			com.aurora.R.drawable.aurora_shutdown0031,
			com.aurora.R.drawable.aurora_shutdown0032,
			com.aurora.R.drawable.aurora_shutdown0033,
			com.aurora.R.drawable.aurora_shutdown0034,
			com.aurora.R.drawable.aurora_shutdown0035,
			com.aurora.R.drawable.aurora_shutdown0036,
			com.aurora.R.drawable.aurora_shutdown0037,
			com.aurora.R.drawable.aurora_shutdown0038,
			com.aurora.R.drawable.aurora_shutdown0039,*/
			com.aurora.R.drawable.aurora_shutdown0040
	};
	private int[] mRebootPics = new int[]{
			com.aurora.R.drawable.aurora_reboot_normal,
		/*	com.aurora.R.drawable.aurora_reboot0016,
			com.aurora.R.drawable.aurora_reboot0017,
			com.aurora.R.drawable.aurora_reboot0018,
			com.aurora.R.drawable.aurora_reboot0019,
			com.aurora.R.drawable.aurora_reboot0020,
			com.aurora.R.drawable.aurora_reboot0021,
			com.aurora.R.drawable.aurora_reboot0022,
			com.aurora.R.drawable.aurora_reboot0023,
			com.aurora.R.drawable.aurora_reboot0024,
			com.aurora.R.drawable.aurora_reboot0025,
			com.aurora.R.drawable.aurora_reboot0026,
			com.aurora.R.drawable.aurora_reboot0027,
			com.aurora.R.drawable.aurora_reboot0028,
			com.aurora.R.drawable.aurora_reboot0029,
			com.aurora.R.drawable.aurora_reboot0030,
			com.aurora.R.drawable.aurora_reboot0031,
			com.aurora.R.drawable.aurora_reboot0032,
			com.aurora.R.drawable.aurora_reboot0033,
			com.aurora.R.drawable.aurora_reboot0034,
			com.aurora.R.drawable.aurora_reboot0035,
			com.aurora.R.drawable.aurora_reboot0036,
			com.aurora.R.drawable.aurora_reboot0037,
			com.aurora.R.drawable.aurora_reboot0038,
			com.aurora.R.drawable.aurora_reboot0039,*/
			com.aurora.R.drawable.aurora_reboot0040
	};
	
	private Runnable nextFrameRun = new Runnable() {
		public void run() {
			if (!isRun) {
				end();
				return;
			}
			currentTime += durations[currentFrame];
			if (callbacks[currentFrame] != null) {
				callbacks[currentFrame].onFrameEnd(currentFrame);
			}
			nextFrame();
		}
	};
	public interface FrameCallback {
			public void onFrameStart(int startTime);

			public void onFrameEnd(int endTime);
		}
	public interface AnimationImageListener {
			public void onAnimationStart(FrameAnimation animation);

			public void onAnimationEnd(FrameAnimation animation);

			public void onRepeat(int repeatIndex);

			public void onFrameChange(int repeatIndex, int frameIndex,
					int currentTime);
		}

	public FrameAnimation(Context context,  int iconType) {
		mContext = context;
		int animRes = 0;
		switch (iconType) {
		case ICON_SHUT_DOWN:
			getDrawables(mShutdownPics);
			break;
		case ICON_REBOOT_DOWN:
			getDrawables(mRebootPics);
			break;
		case ICON_AIRMODE_DOWN:
			getDrawables(mAirmodePics);
			break;

		default:
			break;
		}
//		mAnimDrawable =(AnimationDrawable) mContext.getResources()
//				.getDrawable(animRes);
		this.handler = new Handler();
			init();
	}
	
	private void getDrawables(int[] resArray){
		mFrames = new ArrayList<Drawable>();
//		frames = new Drawable[resArray.length];
		for(int i = 0;i<resArray.length;i++){
			Drawable drawable = mContext.getResources().getDrawable(resArray[i]); 
			mFrames.add(drawable);
//			frames[i] = mContext.getResources().getDrawable(resArray[i]);
		}
	}
	
	public void setView(View view){
		this.view = view;
	}

	private void init() {
		this.frameCount = mFrames.size();
		this.callbacks = new FrameCallback[frameCount];
		this.isRun = false;
		this.fillAfter = false;
		this.isOneShot = true;
		this.isLimitless = false;
		this.repeatTime = 2;
		durations = new int[frameCount];
		for (int i = 0; i < frameCount; i++) {
			durations[i] = 10;
		}
	}

	public void start() {
		if (isRun) {
			return;
		}
		this.isRun = true;
		this.currentRepeat = -1;
		this.currentFrame = -1;
		this.currentTime = 0;
		if (animationImageListener != null) {
			animationImageListener.onAnimationStart(this);
		}
		startProcess();
	}

	public void stop() {
		this.isRun = false;
	}

	private void startProcess() {
		this.currentFrame = -1;
		this.currentTime = 0;
		this.currentRepeat++;
		if (animationImageListener != null) {
			animationImageListener.onRepeat(currentRepeat);
		}
		nextFrame();
	}

	private void endProcess() {
		if (isOneShot || (!isLimitless && currentRepeat >= repeatTime - 1)
				|| !isRun) {
			end();
		} else {
			startProcess();
		}
	}

	private void end() {
		if (!fillAfter && frameCount > 0) {
			view.setBackgroundDrawable(mFrames.get(0));
		}
		if (animationImageListener != null) {
			animationImageListener.onAnimationEnd(this);
		}
		this.isRun = false;
	}

	private void nextFrame() {
		if (currentFrame == frameCount - 1) {
			endProcess();
			return;
		}

		currentFrame++;

		changeFrame(currentFrame);

		handler.postDelayed(nextFrameRun, durations[currentFrame]);
	}

	private void changeFrame(int frameIndex) {
			//if(mFrames.get(frameIndex)!=null){
			//	Log.e("ph", "index:"+frameIndex);
			//}
			view.setBackground(mFrames.get(frameIndex));
//			view.postInvalidate();
//			frames[frameIndex] = null;
			if (animationImageListener != null) {
				animationImageListener.onFrameChange(currentRepeat, frameIndex,
						currentTime);
			}
			if (callbacks[currentFrame] != null) {
				callbacks[currentFrame].onFrameStart(frameIndex);
			}
	}

	public int getSumDuration() {
		int sumDuration = 0;
		for (int duration : durations) {
			sumDuration += duration;
		}
		return sumDuration;
	}

	public boolean isOneShot() {
		return isOneShot;
	}

	public void setOneShot(boolean isOneShot) {
		this.isOneShot = isOneShot;
	}

	public boolean isFillAfter() {
		return fillAfter;
	}

	public void setFillAfter(boolean fillAfter) {
		this.fillAfter = fillAfter;
	}

	public boolean isLimitless() {
		return isLimitless;
	}

	public void setLimitless(boolean isLimitless) {
		if (isLimitless) {
			setOneShot(false);
		}
		this.isLimitless = isLimitless;
	}

	public void addFrameCallback(int index, FrameCallback callback) {
		this.callbacks[index] = callback;
	}

	public void setAnimationImageListener(
			AnimationImageListener animationImageListener) {
		this.animationImageListener = animationImageListener;
	}

	public int getRepeatTime() {
		return repeatTime;
	}

	public void setRepeatTime(int repeatTime) {
		this.repeatTime = repeatTime;
	}



}

 
