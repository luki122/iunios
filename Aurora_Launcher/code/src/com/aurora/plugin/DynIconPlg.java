package com.aurora.plugin;

import com.aurora.calender.FlipViewController;
import com.aurora.launcher.R;
import com.aurora.launcher.ShortcutInfo;
import com.aurora.launcher.FolderInfo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.aurora.launcher.BubbleTextView;
import com.aurora.launcher.CellLayout;
import com.aurora.launcher.FolderIcon;
import com.aurora.launcher.ItemInfo;
import com.aurora.launcher.LauncherApplication;
import com.aurora.launcher.Utilities;
import com.aurora.launcher.Utils;
import com.aurora.util.DeviceProperties;


/**
 * Vulcan added these code in 下午2:34:09
 * 
 * HOW TO USE DYNAMIC ICON
 * 1.You could add dynamic feature for any instance of View
 * 2.add DynIconPlg member in your View class.
 * 3.if your View class has a DynIconPlg member, it will be likely dynamic, but not surely.
 * 4.because there is another flag mIsDynamic in DynIconPlg class.
 * 5.For example, a folder is dynamic after it is created.
 * 6.In some time, you could move or change some items in the folders.
 * 7.Once we found there isn't any dynamic icon in the folder, we will change the flag mIsDynamic.
 * 8.In this case, we would not refresh the folder to save battery volume.
 *
 *
 */

public abstract class DynIconPlg {
	
	//indicate if this module is created so as to prevent repeat creating.
	private static boolean mIsCreated = false;
	public static int nowday;
	public  static int yestoday;
		
	
	//variable only for this class
	public View hostView;
	public boolean mIsDynamic = true;//used to pause refreshing icon
	public boolean hostViewIsAttached = false;
	//private static List<DynIconPlg> mIconTypeList = new ArrayList<DynIconPlg>();

	//protected int mWidth;
	//protected int mHeight;
	
	//variable size about icon
	static int mBgWidth = 0;
	static int mBgHeight = 0;
	static int mBgWidthWithShadow = 0;
	static int mBgHeightWithShadow = 0;
	static int mBgOffsetXToShadow = 0;
	static int mBgOffsetYToShadow = 0;
	
	protected final static Canvas mCanvas = new Canvas();
	protected float mLastMinute;
	protected float mLastHour;
	protected static int mTimerCounter;
	
	public static int SHOW_FREQ_INFINITE = -1;
	public abstract Drawable getCurDrawContent();
	public abstract int getRedrawMultipleFreq();
	public abstract boolean refreshDynIcon();
	public abstract boolean isDirty();
	public abstract void resetDirty();
	public abstract void dump();
	
	static FlipViewController myflipViewController;
	static LinearLayout mylayout;

	protected static Context mContext;	
	
	//public abstract void loadRes(Context context);

	public DynIconPlg(View v) {
		hostView = v;
		hostViewIsAttached = false;
		mIsDynamic = true;
	}
	
	
	public boolean showTimeIsUp() {
		int multiple = getRedrawMultipleFreq();
		if(mTimerCounter % multiple != 0) {
			return false;
		}
		return true;
	}


	private final Handler mHandler = new Handler();
	static Timer mTimer = null;
	final static List<DynIconPlg> mDynIconList = new ArrayList<DynIconPlg>();
	final static int mTimerHz = 10;
	final static int mIntervalMs = 1000/mTimerHz;
	final static int mDelayMs = 0;
	final static Handler uiHandler = new Handler();
	final static Runnable timelyRun = new Runnable() {
		@Override
		public void run() {
			
			boolean bSuccess;

			mTimerCounter ++;

			for (DynIconPlg dip : mDynIconList) {
				if(!dip.mIsDynamic) {
					continue;
				}
				if(!dip.isDirty()) {
					continue;
				}
				if(dip instanceof CalendarIcon){
					continue;					
				}
				
				//START 2015-03-04 fix BUG#10808 
				if(dip instanceof WeatherMovingIcon){
					if(dip.isDirty()){
						bSuccess = dip.refreshDynIcon();
						if(bSuccess){
							dip.resetDirty();
						}
					}
				}
				//END 2015-03-04

				if (dip.showTimeIsUp()) {
					bSuccess = dip.refreshDynIcon();
					if(bSuccess) {
						dip.resetDirty();
					}
				}
			}
			
			//observe DynIconPlg every minute
			if((mTimerCounter % 600) == 0) {
				dumpAll();
			}
		}
	};

	//step of using Dynamic Icon plug-in:
	//1.if you want to support dynamic icon, you have to createPlugin at first.
	//2.every time you create view of your dynamic icon, 
	//   you have to check if it is supported 
	//   by calling the static method:IsDynamicIcon
	//3.At this moment, you have to tell the method about:redrawHz
	public static void createPlugin(Context context) {
		final long time = System.currentTimeMillis();
		final Calendar mCalendar = Calendar.getInstance();
		mCalendar.setTimeInMillis(time);
		nowday = mCalendar.get(Calendar.DAY_OF_MONTH);
		mCalendar.add(mCalendar.DATE,-1);
		yestoday = mCalendar.get(Calendar.DAY_OF_MONTH);
	
		if(mIsCreated) {
			LauncherApplication.logVulcan.print("createPlugin: cancel because already created");
			Log.d("vulcan-dynamic","createPlugin: cancel because already created");
			return;
		}
		mIsCreated = true;

		loadResSizeOfDevice(context);
		ClockMovingIcon.loadRes(context);
		CalendarIcon.loadRes(context);
		CalendarIcon.measure();
		TrafficMovingIcon.loadResOfDevice(context);
		TrafficMovingIcon.measure(context);
		TrafficMovingIcon.requestTrafficInfo(context);
		WeatherMovingIcon.loadResOfDevice(context); 
		//secheduleTimer(true);
		LauncherApplication.logVulcan.print("createPlugin: succeed in creating");
		Log.d("vulcan-dynamic","createPlugin: succeed in creating");
		return;
	}

	/**
	 * vulcan created and tested it in 2014-6-23
	 * this method add a DynIconPlg into a list.
	 * 1.
	 * @param DynIconPlg dip instance of DynIconPlg
	 */
	public static void addDynIcon(DynIconPlg dip) {
		mDynIconList.add(dip);
		if ((dip.hostView instanceof FolderIcon)  || true) {
			Log.d("vulcan-iconlist", "called addDynIcon = " + dip.hostView
					+ ", size = " + mDynIconList.size());
		}
		//Log.d("vulcan-iconlist",LogWriter.StackToString(new Throwable()));
		LauncherApplication.logVulcan.print("called addDynIcon:v = " + dip.hostView
				+ ", new size = " + mDynIconList.size());
		return;
	}
	public static void createCalendar(FlipViewController flipViewController,LinearLayout layout){
		myflipViewController=flipViewController;	
	  	mylayout=	layout;
	
	}

	public static void rmDynIcon(DynIconPlg dip) {
		mDynIconList.remove(dip);
		if ((dip.hostView instanceof FolderIcon) || true) {
			Log.d("vulcan-iconlist","called rmDynIcon:v = " + dip.hostView + ", size = " + mDynIconList.size());
		}
		//Log.d("vulcan-iconlist",LogWriter.StackToString(new Throwable()));
		LauncherApplication.logVulcan.print("called rmDynIcon:v = " + dip.hostView
				+ ", new size = " + mDynIconList.size());
	}
	
	public static boolean viewIsDynamicClock(View view) {		
		if(!(view instanceof BubbleTextView)) {
			return false;
		}
		BubbleTextView myView = (BubbleTextView)view;
		Object tag = myView.getTag();
		
		if(!(tag instanceof ShortcutInfo)) {
			return false;
		}
		
		ShortcutInfo info = (ShortcutInfo)tag;
		Intent intent = info.getIntent();
		if(intent == null) {
			return false;
		}
		
		ComponentName comName = intent.getComponent();
		if(comName != null) {
			String strComName = comName.getClassName();
			if(strComName != null) {
				return strComName.contains("com.android.deskclock");
			}
		}

		return false;
	}
	
	public static void loadResSizeOfDevice(Context context) {
		Resources res = context.getResources();
		Drawable shadowDrawable = res.getDrawable(R.drawable.shadow);
		
		BitmapDrawable bmpdBg2 = (BitmapDrawable) res.getDrawable(R.drawable.launcher_net_traff_bg1);
		Bitmap bmpBg2 = bmpdBg2.getBitmap().copy(Bitmap.Config.ARGB_8888,true);
		if(DeviceProperties.isNeedScale()) {
			bmpBg2 = Utilities.zoomBitmap(bmpBg2, context);
		}
		mBgWidth = bmpBg2.getWidth();
		mBgHeight = bmpBg2.getHeight();
		Log.d("vulcan-size",String.format("loadResSizeOfDevice: icon base size = %d", mBgWidth));

		
		BitmapDrawable bmpdBg1 = (BitmapDrawable) res.getDrawable(R.drawable.launcher_net_traff_bg1);
		Log.d("vulcan-size",String.format("loadResSizeOfDevice: drawable: bmpdBg1.width = %d",bmpdBg1.getIntrinsicWidth()));
		Bitmap bmpBg1 = bmpdBg1.getBitmap().copy(Bitmap.Config.ARGB_8888,true);
		Log.d("vulcan-size",String.format("loadResSizeOfDevice: bitmap: bmpBg1.width = %d",bmpBg1.getWidth()));
		//mBgWidth = bmpBg.getWidth();
		//mBgHeight = bmpBg.getHeight();

		bmpBg1 = Utils.getRoundedBitmap(bmpBg1, context);
		Log.d("vulcan-size",String.format("loadResSizeOfDevice: after rounded, bmpBg1.width = %d",bmpBg1.getWidth()));
		bmpBg1 = Utils.getShadowBitmap1(bmpBg1, shadowDrawable);
		Log.d("vulcan-size",String.format("loadResSizeOfDevice: after shadowed, bmpBg1.width = %d",bmpBg1.getWidth()));
		if(DeviceProperties.isNeedScale()) {
			bmpBg1 = Utilities.zoomBitmap(bmpBg1, context);
		}
		Log.d("vulcan-size",String.format("loadResSizeOfDevice: after scaled, bmpBg1.width = %d",bmpBg1.getWidth()));

		mBgWidthWithShadow = bmpBg1.getWidth();
		mBgHeightWithShadow = bmpBg1.getHeight();
		mBgOffsetXToShadow = (mBgWidthWithShadow - mBgWidth)/2;
		mBgOffsetYToShadow = (mBgHeightWithShadow - mBgHeight)/2;
		
		Log.d("vulcan-n900",String.format("loadResSizeOfDevice: mBgWidthWithShadow = %d, mBgHeightWithShadow = %d",mBgWidthWithShadow, mBgHeightWithShadow));
		Log.d("vulcan-n900",String.format("loadResSizeOfDevice: mBgOffsetXToShadow = %d, mBgOffsetYToShadow = %d",mBgOffsetXToShadow, mBgOffsetYToShadow));

		return;
	}
	
	public static boolean viewIsDynamic(View view) {
		if(view instanceof BubbleTextView) {		
			return (((BubbleTextView)view).mDynIconPlg != null);
		}
		else if (view instanceof FolderIcon) {
			FolderIcon fIcon = (FolderIcon)view;
			//FolderInfo fInfo = (FolderInfo)fIcon.getTag();
			ArrayList<View> previewList = fIcon.getDynPreviewItems();
			if(previewList.size() > 0) {
				for(int i = 0;i < previewList.size();i ++) {
					if(previewList.get(i) instanceof BubbleTextView) {
						BubbleTextView v = (BubbleTextView)previewList.get(i);
						//Log.d("vulcan-iconop","viewIsDynamic: v: " + v.getText());
						if(v.mDynIconPlg != null) {
							return true;							
						}
					}
				}
			}
			
		}
		return false;
	}
	
	public static void dumpAll() {
		if (mDynIconList != null) {
			LauncherApplication.logVulcan
					.print("+++++++++++++++mDynIconList++++++++++++++++++++++++, size = "
							+ mDynIconList.size());
			for (DynIconPlg dip : mDynIconList) {
				dip.dump();
			}
		}
		return;
	}

	/**
	 * vulcan created and tested it in 2014-6-20
	 * create a new instance for icon view
	 * @param item. itemInfo of the dynamic icon, such as ShortcutInfo, FolderInfo
	 * @param view. view of the dynamic icon, such as BubbleTextView,FolderIcon and so on
	 */
	public static DynIconPlg produceDynIconPlg(ItemInfo item,View itemView) {
		DynIconPlg dip = null;
		
		if (item instanceof ShortcutInfo) {

			Intent intent = ((ShortcutInfo)item).getIntent();

			if (intent == null) {
				return null;
			}

			if (intent.getComponent() == null) {
				return null;
			}
			
			//Log.d("vulcan-iconlist", "ShortcutInfo.itemView.window = " + itemView.getWindowToken());

			String componentName = intent.getComponent().getClassName();
			// LauncherApplication.logVulcan.print("produceDynIconPlg:pkgName = "
			// + componentName);
			if (componentName.contains("com.android.deskclock")) {
				dip = new ClockMovingIcon(itemView);
				//return null;
			}
			else if (componentName.contains("com.android.calendar")&&(intent.getComponent().getPackageName().equals("com.android.calendar"))) {
				dip = new CalendarIcon(itemView);
				//return null;
			}
			else if( componentName.contains("com.netmanage.activity.NetMainActivity")) {
				dip = new TrafficMovingIcon(itemView);
				//dip = null;
			}
			else if( componentName.contains("com.aurora.weatherforecast")&&(intent.getComponent().getPackageName().equals("com.aurora.weatherforecast"))) {
				dip = new WeatherMovingIcon(itemView, intent);
				//dip = null;
			}
		}
		else if(item instanceof FolderInfo) {
			//Log.d("vulcan-iconlist", "FolderMovingIcon.itemView.window = " + itemView.getWindowToken());
			dip = new FolderMovingIcon(itemView);
			//return null;
		}
		
		if(dip != null) {
			if(itemView.getWindowToken() == null) {
				dip.hostViewIsAttached = false;
			}
			else {
				dip.hostViewIsAttached = true;
			}
		}
		//dip = null;
		return dip;
	}

	public static void secheduleTimer(boolean toOpen) {
		if (toOpen && mTimer == null) {
			LauncherApplication.logVulcan.print("secheduleTimer: opening timer for dynamic icon!!");
			Log.d("vulcan-iconop", "secheduleTimer: toOpen = " + toOpen);
			mTimer = new Timer();
			mTimer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					uiHandler.post(DynIconPlg.timelyRun);
				}
			}, mDelayMs, mIntervalMs);
			return;
		}
		
		if(!toOpen && mTimer != null) {
			Log.d("vulcan-iconop", "secheduleTimer: toOpen = " + toOpen);
			LauncherApplication.logVulcan.print("secheduleTimer: closing timer for dynamic icon!!");
			mTimer.cancel();
			mTimer = null;
			return;
		}
		return;
	}
	
	public void resume() {
		this.mIsDynamic = true;
	}

	public void pause() {
		this.mIsDynamic = false;
	}
/*
	public static void onDestroy() {
		LauncherApplication.logVulcan.print("Destroying window: mDynIconList.size() = " + mDynIconList.size());
		Log.d("vulcan2", "Destroying window: mDynIconList.size() = " + mDynIconList.size());
		for (DynIconPlg dip : mDynIconList) {
			if(dip.mReceiverWorking) {
				LauncherApplication.logVulcan.print("calling unregisterReceiver, dip.mIntentReceiver = " + dip.mIntentReceiver);
				Log.d("vulcan2", "calling unregisterReceiver, dip.mIntentReceiver = " + dip.mIntentReceiver);
				dip.hostView.getContext().unregisterReceiver(dip.mIntentReceiver);
				dip.mReceiverWorking = false;
			}
			else {
				LauncherApplication.logVulcan.print("NO NEED TO CALL unregisterReceiver");
				Log.d("vulcan2", "NO NEED TO CALL unregisterReceiver");
			}
		}
		return;
	}

	
	private static boolean isTimerOnBeforeScreenOff = false;
	private static void onScreenOn() {
		secheduleTimer(isTimerOnBeforeScreenOff);
		return;
	}
	
	private static void onScreenOff() {
		isTimerOnBeforeScreenOff = (mTimer != null);
		secheduleTimer(false);
	}
	
	public static void onLauncherReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		
		if (Intent.ACTION_SCREEN_OFF.equals(action)) {
			onScreenOff();
		} else if(Intent.ACTION_SCREEN_ON.equals(action)) {
			onScreenOn();
		}
	}
*/

	public void onAttachedToWindow() {
		if (!this.hostViewIsAttached) {

			this.hostViewIsAttached = true;

			LauncherApplication.logVulcan.print("onAttachedToWindow: v = "
					+ hostView);
			//Log.d("vulcan-iconop","onAttachedToWindow: " + hostView);
			DynIconPlg.addDynIcon(this);

			if (!mReceiverWorking) {
				IntentFilter filter = new IntentFilter();
				// filter.addAction(Intent.ACTION_TIME_TICK);
				filter.addAction(Intent.ACTION_TIME_CHANGED);
				// filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
				hostView.getContext().registerReceiver(mIntentReceiver, filter,
						null, mHandler);
				this.mReceiverWorking = true;
			}
              
			refreshDynIcon();
		}
		return;
	}

	public void onDetachedFromWindow() {
		if (this.hostViewIsAttached) {

			this.hostViewIsAttached = false;
			
			if (mReceiverWorking) {
				hostView.getContext().unregisterReceiver(mIntentReceiver);
				mReceiverWorking = false;
			}

			LauncherApplication.logVulcan.print("onDetachedFromWindow: v = " + hostView);
			//Log.d("vulcan-iconop","onDetachedFromWindow: " + hostView);
			DynIconPlg.rmDynIcon(this);
		}
		return;
	}

	private boolean mReceiverWorking = false;
	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
                Log.i("xiexiujie", "_______DynIconPlg________onReceive_____");
			if (intent.getAction().equals(Intent.ACTION_TIME_CHANGED)) {
				 Log.i("xiexiujie", "_______DynIconPlg________recovery2_____");
				secheduleTimer(false);
				secheduleTimer(true);
			//	refreshDynIcon();
				Log.d("vulcan-iconop","ACTION_TIME_CHANGED: this is: " + DynIconPlg.this);
				LauncherApplication.logVulcan.print("DynIconPlg:onReceive: " + Thread.currentThread().getName());
			}
		}
	};
}