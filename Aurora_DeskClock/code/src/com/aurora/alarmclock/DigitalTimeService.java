package com.aurora.alarmclock;

import java.util.Calendar;
import java.util.Locale;

import com.android.deskclock.AlarmClock;
import com.android.deskclock.R;
import com.aurora.utils.MobileUtil;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;



public class DigitalTimeService extends Service {
	private String[] days;
	private int[] digits = new int[] { R.drawable.time0, R.drawable.time1,
			R.drawable.time2, R.drawable.time3, R.drawable.time4,
			R.drawable.time5, R.drawable.time6, R.drawable.time7,
			R.drawable.time8, R.drawable.time9 };

	private int[] digitViews = new int[] { R.id.img01, R.id.img02, R.id.img04,
			R.id.img05 };
	
	private boolean mWidgetEnable = true;

	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			
			String action = intent.getAction();
			//Log.d("cjslog", "mIntentReceiver " + action);
			if (action.equals(Intent.ACTION_TIME_CHANGED)
					|| action.equals(Intent.ACTION_DATE_CHANGED)
					|| action.equals(Intent.ACTION_TIMEZONE_CHANGED)
					|| action.equals(Intent.ACTION_TIME_TICK)) {
				Message msg = new Message();
				msg.obj = context;
				handler.sendMessage(msg);
			}
		}
	};

	private Handler handler  = new Handler(){
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			notifyWidget((Context)msg.obj);
		};
	};
	
	private boolean mFlag;
	private Handler mHandler = new Handler();
	
	private BroadcastReceiver mGuardReceiver = new BroadcastReceiver(){
		

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			mFlag = true;
			String action = intent.getAction();
/*			Log.d("cjslog", "mGuardReceiver " + action);
			if (action.equals("com.android.deskclock.TIMEWIDGETDISABLED")) {
				context.sendBroadcast(intent);
			}*/
			
			if (action.equals("com.android.deskclock.GUARDSURVIVAL")) {
				mFlag = false;
				return;
			}
			
			if (action.equals(Intent.ACTION_TIME_TICK)) {
				mHandler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						Context GuardContext ;
						// TODO Auto-generated method stub
						if (mFlag) {
							try {
								GuardContext = createPackageContext("com.android.deskclockguardian", 
											Context.CONTEXT_IGNORE_SECURITY);
								Intent intent = new Intent("com.android.deskclock.GUARDIAN");
								intent.setPackage("com.android.deskclockguardian");
								startService(intent);
							} catch (NameNotFoundException e) {
								// TODO: handle exception
							}
						}
					}
				}, 700);
			}
		}
		
	};
	
	@Override
	public void onCreate() {
		//Log.d("cjslog", "DigitalTimeService onCreate");
		super.onCreate();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_TIME_CHANGED);
		filter.addAction(Intent.ACTION_DATE_CHANGED);
		filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		filter.addAction(Intent.ACTION_TIME_TICK);
		registerReceiver(mIntentReceiver, filter);
		
		IntentFilter intentFilter = new IntentFilter("com.android.deskclock.GUARDSURVIVAL");
		intentFilter.addAction(Intent.ACTION_TIME_TICK);
		//intentFilter.addAction("com.android.deskclock.TIMEWIDGETDISABLED");
		registerReceiver(mGuardReceiver, intentFilter);
		mWidgetEnable = true;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return (IBinder) DigitalTimeService.this;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		//Log.d("cjslog", "onStart");
		notifyWidget(getApplicationContext());
	}

	private Bitmap makeBitMap(LinearLayout poplayout) {

		Bitmap bitmap = null;
		// 打开图像缓存
		poplayout.setDrawingCacheEnabled(true);
		// 必须调用measure和layout方法才能成功保存可视组件的截图到png图像文件
		// 测量View大小
		poplayout.measure(
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		// 发送位置和尺寸到View及其所有的子View
		poplayout.layout(0, 0, poplayout.getMeasuredWidth(),
				poplayout.getMeasuredHeight());
		try {
			// 获得可视组件的截图
			bitmap = poplayout.getDrawingCache();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return bitmap;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//Log.d("cjslog", "onStartCommand " + startId);
		//Log.d("cjslog", "DigitalTimeService onStartCommand");
		notifyWidget(getApplicationContext());
		return Service.START_NOT_STICKY;
	}

	private void notifyWidget(Context context) {
		String format = android.text.format.DateFormat.is24HourFormat(context) ? DigitalTimeAppWidgetProvider.M24
				: DigitalTimeAppWidgetProvider.M12;

		days = context.getResources().getStringArray(R.array.daysChinese);
		Calendar calendar = Calendar.getInstance();
		CharSequence newTime = DateFormat.format(format, calendar);
		String weekDay = String.valueOf(calendar.get(Calendar.MONTH) + 1)
				+ context.getResources().getString(R.string.appwidget_month)
				+ calendar.get(Calendar.DAY_OF_MONTH)
				+ context.getResources().getString(R.string.appwidget_date)
				+ days[calendar.get(Calendar.DAY_OF_WEEK) - 1];
		CharSequence newTime2 = newTime.subSequence(0, 2);
		CharSequence newTime3 = newTime.subSequence(3, 5);
		String newTime4 = newTime2.toString() + newTime3.toString();
		LinearLayout poplayout = MobileUtil.getAppWidgetTimeLayout(context);
		RemoteViews views = MobileUtil.getAppWidgetTimeRemoteViews(context);
		ImageView imageView1 = (ImageView) poplayout.findViewById(R.id.img01);
		ImageView imageView2 = (ImageView) poplayout.findViewById(R.id.img02);
		ImageView imageView4 = (ImageView) poplayout.findViewById(R.id.img04);
		ImageView imageView5 = (ImageView) poplayout.findViewById(R.id.img05);
		ImageView imageView3 = (ImageView) poplayout.findViewById(R.id.img03);
		TextView textView = (TextView) poplayout.findViewById(R.id.txtAll);
		ImageView[] imageViews = { imageView1, imageView2, imageView4,
				imageView5 };
		

		for (int i = 0; i < newTime4.length(); i++) {

			int num = newTime4.charAt(i) - 48;
			if (num < 0) {
				num = -num;

			}
			// views.setImageViewResource(imageViews[i], digits[num]);
			imageViews[i].setImageResource(digits[num]);
		}

		imageView3.setVisibility(View.VISIBLE);
		textView.setText(weekDay);

		Bitmap bitmap = makeBitMap(poplayout);

		views.setImageViewBitmap(R.id.imageview, bitmap);

		// views.setTextViewText(R.id.txtAll, weekDay);
		// views.setViewVisibility(R.id.img03, View.VISIBLE);
		Intent intent = new Intent(context, AlarmClock.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				intent, 0);
		views.setOnClickPendingIntent(R.id.imageview, pendingIntent);
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(getApplicationContext());

		ComponentName componentName = new ComponentName(
				getApplicationContext(), DigitalTimeAppWidgetProvider.class);

		appWidgetManager.updateAppWidget(componentName, views);
		
	
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Intent intent2 = new Intent("com.android.widget.WIDGET_TIME_CHANGE");
				intent2.putExtra("widgetenable", mWidgetEnable);
				getApplicationContext().sendBroadcast(intent2);
				
			}
		}, 100);
		
		bitmap.recycle();
		System.gc();

	}

	@Override
	public void onDestroy() {
		Log.d("cjslog", "DigitalTimeService onDestroy");
		mWidgetEnable = false;
		//Log.d("cjslog", "onDestroy");
		unregisterReceiver(mIntentReceiver);
		unregisterReceiver(mGuardReceiver);
		super.onDestroy();
	}

}
