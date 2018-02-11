package com.aurora.reminder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

// import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
// import android.os.SystemClock;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.widget.RemoteViews;

public class ReminderWidgetService extends Service {

	public static final String TAG = "ReminderWidget";

	public static final String ACTION_PROVIDER_CHANGED = "com.aurora.reminder.PROVIDER_CHANGED";
	// private static final String ACTION_DO_WINK = "com.aurora.reminder.DO_WINK";
	private static final String ACTION_EMOTICON_CLICKED = "com.aurora.reminder.EMOTICON_CLICKED";
	// private static final String ACTION_OPEN_VOICE_ASSISTANT = "com.aurora.voiceassistant.ACTION_ENTER_MODE_BY_WEIGHT";
	private static final String ACTION_UPDATE_WEATHER = "com.aurora.weatherfoecast.request.updateweather";

	private static final String[] PROJECTION = {
		ReminderUtils.TITLE,
		ReminderUtils.ACTION,
		ReminderUtils.PACKAGE
	};

	private static final int INDEX_TITLE = 0;
	private static final int INDEX_ACTION = 1;
	private static final int INDEX_PACKAGE = 2;

	private static final int[] EMOTICON_DOZE = new int[] {
		R.drawable.emoticon_doze_00,
		R.drawable.emoticon_doze_01,
		R.drawable.emoticon_doze_02,
		R.drawable.emoticon_doze_03,
		R.drawable.emoticon_doze_04,
		R.drawable.emoticon_doze_05,
		R.drawable.emoticon_doze_06,
		R.drawable.emoticon_doze_07,
		R.drawable.emoticon_doze_08,
		R.drawable.emoticon_doze_09,
		R.drawable.emoticon_doze_10,
		R.drawable.emoticon_doze_11,
		R.drawable.emoticon_doze_12,
		R.drawable.emoticon_doze_13,
		R.drawable.emoticon_doze_14,
		R.drawable.emoticon_doze_15,
		R.drawable.emoticon_doze_16,
		R.drawable.emoticon_doze_17,
		R.drawable.emoticon_doze_18,
		R.drawable.emoticon_doze_19,
		R.drawable.emoticon_doze_20,
		R.drawable.emoticon_doze_21,
		R.drawable.emoticon_doze_22,
		R.drawable.emoticon_doze_23,
		R.drawable.emoticon_doze_24,
		R.drawable.emoticon_doze_25,
		R.drawable.emoticon_doze_26,
		R.drawable.emoticon_doze_27,
		R.drawable.emoticon_doze_28,
		R.drawable.emoticon_doze_29,
		R.drawable.emoticon_doze_30,
		R.drawable.emoticon_doze_31,
		R.drawable.emoticon_doze_32,
		R.drawable.emoticon_doze_33,
		R.drawable.emoticon_doze_34,
		R.drawable.emoticon_doze_35,
		R.drawable.emoticon_doze_36,
		R.drawable.emoticon_doze_37,
		R.drawable.emoticon_doze_38,
		R.drawable.emoticon_doze_39,
		R.drawable.emoticon_doze_40,
		R.drawable.emoticon_doze_41,
		R.drawable.emoticon_doze_42,
		R.drawable.emoticon_doze_43,
		R.drawable.emoticon_doze_44,
		R.drawable.emoticon_doze_45,
		R.drawable.emoticon_doze_46,
		R.drawable.emoticon_doze_47,
		R.drawable.emoticon_doze_48,
		R.drawable.emoticon_doze_49,
		R.drawable.emoticon_doze_50,
		R.drawable.emoticon_doze_51,
		R.drawable.emoticon_default
	};

	private static final int[] EMOTICON_JUMP = new int[] {
		R.drawable.emoticon_jump_00,
		R.drawable.emoticon_jump_01,
		R.drawable.emoticon_jump_02,
		R.drawable.emoticon_jump_03,
		R.drawable.emoticon_jump_04,
		R.drawable.emoticon_jump_05,
		R.drawable.emoticon_jump_06,
		R.drawable.emoticon_jump_07,
		R.drawable.emoticon_jump_08,
		R.drawable.emoticon_jump_09,
		R.drawable.emoticon_jump_10,
		R.drawable.emoticon_jump_11,
		R.drawable.emoticon_jump_12,
		R.drawable.emoticon_jump_13,
		R.drawable.emoticon_jump_14,
		R.drawable.emoticon_jump_15,
		R.drawable.emoticon_jump_16,
		R.drawable.emoticon_default
	};

	private static final int[] EMOTICON_LOOKAROUND = new int[] {
		R.drawable.emoticon_lookaround_00,
		R.drawable.emoticon_lookaround_01,
		R.drawable.emoticon_lookaround_02,
		R.drawable.emoticon_lookaround_03,
		R.drawable.emoticon_lookaround_04,
		R.drawable.emoticon_lookaround_05,
		R.drawable.emoticon_lookaround_06,
		R.drawable.emoticon_lookaround_07,
		R.drawable.emoticon_lookaround_08,
		R.drawable.emoticon_lookaround_09,
		R.drawable.emoticon_lookaround_10,
		R.drawable.emoticon_lookaround_11,
		R.drawable.emoticon_lookaround_12,
		R.drawable.emoticon_lookaround_13,
		R.drawable.emoticon_lookaround_14,
		R.drawable.emoticon_lookaround_15,
		R.drawable.emoticon_lookaround_16,
		R.drawable.emoticon_lookaround_17,
		R.drawable.emoticon_lookaround_18,
		R.drawable.emoticon_lookaround_19,
		R.drawable.emoticon_lookaround_20,
		R.drawable.emoticon_lookaround_21,
		R.drawable.emoticon_lookaround_22,
		R.drawable.emoticon_lookaround_23,
		R.drawable.emoticon_lookaround_24,
		R.drawable.emoticon_lookaround_25,
		R.drawable.emoticon_lookaround_26,
		R.drawable.emoticon_lookaround_27,
		R.drawable.emoticon_lookaround_28,
		R.drawable.emoticon_lookaround_29,
		R.drawable.emoticon_lookaround_30,
		R.drawable.emoticon_lookaround_31,
		R.drawable.emoticon_lookaround_32,
		R.drawable.emoticon_lookaround_33,
		R.drawable.emoticon_lookaround_34,
		R.drawable.emoticon_lookaround_35,
		R.drawable.emoticon_lookaround_36,
		R.drawable.emoticon_lookaround_37,
		R.drawable.emoticon_lookaround_38,
		R.drawable.emoticon_lookaround_39,
		R.drawable.emoticon_lookaround_40,
		R.drawable.emoticon_lookaround_41,
		R.drawable.emoticon_lookaround_42,
		R.drawable.emoticon_lookaround_43,
		R.drawable.emoticon_lookaround_44,
		R.drawable.emoticon_lookaround_45,
		R.drawable.emoticon_lookaround_46,
		R.drawable.emoticon_lookaround_47,
		R.drawable.emoticon_lookaround_48,
		R.drawable.emoticon_lookaround_49,
		R.drawable.emoticon_lookaround_50,
		R.drawable.emoticon_lookaround_51,
		R.drawable.emoticon_lookaround_52,
		R.drawable.emoticon_lookaround_53,
		R.drawable.emoticon_lookaround_54,
		R.drawable.emoticon_lookaround_55,
		R.drawable.emoticon_lookaround_56,
		R.drawable.emoticon_default
	};

	private static final int[] EMOTICON_ROLL = new int[] {
		R.drawable.emoticon_roll_00,
		R.drawable.emoticon_roll_01,
		R.drawable.emoticon_roll_02,
		R.drawable.emoticon_roll_03,
		R.drawable.emoticon_roll_04,
		R.drawable.emoticon_roll_05,
		R.drawable.emoticon_roll_06,
		R.drawable.emoticon_roll_07,
		R.drawable.emoticon_roll_08,
		R.drawable.emoticon_roll_09,
		R.drawable.emoticon_roll_10,
		R.drawable.emoticon_roll_11,
		R.drawable.emoticon_roll_12,
		R.drawable.emoticon_roll_13,
		R.drawable.emoticon_roll_14,
		R.drawable.emoticon_roll_15,
		R.drawable.emoticon_roll_16,
		R.drawable.emoticon_roll_17,
		R.drawable.emoticon_roll_18,
		R.drawable.emoticon_roll_19,
		R.drawable.emoticon_roll_20,
		R.drawable.emoticon_roll_21,
		R.drawable.emoticon_roll_22,
		R.drawable.emoticon_roll_23,
		R.drawable.emoticon_roll_24,
		R.drawable.emoticon_roll_25,
		R.drawable.emoticon_roll_26,
		R.drawable.emoticon_roll_27,
		R.drawable.emoticon_roll_28,
		R.drawable.emoticon_roll_29,
		R.drawable.emoticon_roll_30,
		R.drawable.emoticon_roll_31,
		R.drawable.emoticon_roll_32,
		R.drawable.emoticon_roll_33,
		R.drawable.emoticon_roll_34,
		R.drawable.emoticon_roll_35,
		R.drawable.emoticon_roll_36,
		R.drawable.emoticon_roll_37,
		R.drawable.emoticon_roll_38,
		R.drawable.emoticon_roll_39,
		R.drawable.emoticon_roll_40,
		R.drawable.emoticon_default
	};

	private static final int[] EMOTICON_SPIN = new int[] {
		R.drawable.emoticon_spin_00,
		R.drawable.emoticon_spin_01,
		R.drawable.emoticon_spin_02,
		R.drawable.emoticon_spin_03,
		R.drawable.emoticon_spin_04,
		R.drawable.emoticon_spin_05,
		R.drawable.emoticon_spin_06,
		R.drawable.emoticon_spin_07,
		R.drawable.emoticon_spin_08,
		R.drawable.emoticon_spin_09,
		R.drawable.emoticon_spin_10,
		R.drawable.emoticon_spin_11,
		R.drawable.emoticon_spin_12,
		R.drawable.emoticon_spin_13,
		R.drawable.emoticon_spin_14,
		R.drawable.emoticon_spin_15,
		R.drawable.emoticon_spin_16,
		R.drawable.emoticon_spin_17,
		R.drawable.emoticon_spin_18,
		R.drawable.emoticon_spin_19,
		R.drawable.emoticon_spin_20,
		R.drawable.emoticon_spin_21,
		R.drawable.emoticon_spin_22,
		R.drawable.emoticon_spin_23,
		R.drawable.emoticon_spin_24,
		R.drawable.emoticon_spin_25,
		R.drawable.emoticon_spin_26,
		R.drawable.emoticon_spin_27,
		R.drawable.emoticon_spin_28,
		R.drawable.emoticon_spin_29,
		R.drawable.emoticon_spin_30,
		R.drawable.emoticon_default
	};

	private static final int ANIMATION_PERIOD = 40;
	// private static final long WINK_ALARM_PERIOD = DateUtils.SECOND_IN_MILLIS * 10;

	private static final int DEFAULT_BACKGROUND_COLOR = 0xff84888f;

	private static final int[][] SPECIAL_WALLPAPER_COLORS = new int[][] {
		{-8347448, -16244680, -6242064, -10450776, -13082504, -6770480},
		{-12017456, -16773080, -6764320, -11501400, -14141352, -7296824},
		{-12566384, -14079652, -1523504, -10985304, -8091505, -7296824},
		{-13070176, -15177616, -6768432, -10970960, -10999760, -3100488},
		{-8091505, -8091505, -8091505, -10981216, -13615008, -5195576},
		{-11965002, -15722456, -8091505, -11513712, -13619112, -7303000},
		{-8091505, -8091505, -8091505, -8882056, -13092808, -7303024},
		{-14643024, -16226176, -10442552, -11497304, -8091505, -5721912},
		{-4704200, -9431016, -3112840, -8091505, -8091505, -3620672}
	};

	private static final int[] SPECIAL_BACKGROUND_COLORS = new int[] {
		0xff81adeb,
		0xff4388cc,
		0xfffcc5d3,
		0xff34b8c7,
		0xff1c1c21,
		0xff4a4d8f,
		0xff212121,
		0xff267794,
		0xffd4cdcb
	};

	private ContentResolver mResolver;
	// private AlarmManager mAlarmManager;

	private String[] goodMorningArray, goodNoonArray, goodNightArray, goodMidnightArray;
	private int width, height, radius, color;
	private Bitmap bgBitmap;

	private String timeStr, ampmStr, dateStr, title, action, packageName, weatherTitle, weatherAction, greetTitle;
	private String preTitle, nextTitle, tmpTitle;
	private String preAction, nextAction, tmpAction;
	private String prePackage, nextPackage, tmpPackage;

	private int preHour;

	private boolean changeBackgroundLater = false;
	private boolean onEmoticon = false;
	private int[] emoticon;
	private int index = 0;
	private Timer timer;

	private Handler mHanlder = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			setPreTitle();

			int what = msg.what;
			if (what == 0) {
				timeChange();
			} else if (what == 1) {
				dataChange();
			} else if (what == 2) {
				if (!onEmoticon) {
					changeBackgroundLater = false;
					bgChange();
				} else {
					changeBackgroundLater = true;
				}
			} else if (what == 3) {
				if (!onEmoticon) {
					startEmoticon();
				}
			}
		}
	};

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.e(TAG, "BroadcastReceiver->onReceive() action = " + action);

			if (ACTION_EMOTICON_CLICKED.equals(action)) {
				mHanlder.sendEmptyMessage(3);
			} else if (Intent.ACTION_WALLPAPER_CHANGED.equals(action)) {
				mHanlder.sendEmptyMessage(2);
			} else if (ACTION_PROVIDER_CHANGED.equals(action)) {
				mHanlder.sendEmptyMessage(1);
			} else {
				if (Intent.ACTION_TIME_TICK.equals(action)) {
					Time time = new Time();
					time.setToNow();
					if (time.minute == 0) {
						sendBroadcast(new Intent(ACTION_UPDATE_WEATHER));
					}
				} else {
					sendBroadcast(new Intent(ACTION_UPDATE_WEATHER));
				}
				mHanlder.sendEmptyMessage(0);
			}
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		Log.e(TAG, "ReminderWidgetService->onCreate()");

		width = getResources().getDimensionPixelOffset(R.dimen.reminder_widget_width);
		height = getResources().getDimensionPixelOffset(R.dimen.reminder_widget_height);
		radius = getResources().getDimensionPixelOffset(R.dimen.reminder_widget_corner_radius);

		goodMorningArray = getResources().getStringArray(R.array.good_morning);
		goodNoonArray = getResources().getStringArray(R.array.good_noon);
		goodNightArray = getResources().getStringArray(R.array.good_night);
		goodMidnightArray = getResources().getStringArray(R.array.good_midnight);

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_TIME_CHANGED);
		filter.addAction(Intent.ACTION_DATE_CHANGED);
		filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		filter.addAction(Intent.ACTION_TIME_TICK);
		filter.addAction(Intent.ACTION_WALLPAPER_CHANGED);
		filter.addAction(ACTION_PROVIDER_CHANGED);
		filter.addAction(ACTION_EMOTICON_CLICKED);

		registerReceiver(mReceiver, filter);

		mResolver = getContentResolver();
		// mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		getTimeDateStr();
		getReminderInfo();
		getBackgroudBitmap();
	}

	@Override
	public void onDestroy() {
		Log.e(TAG, "ReminderWidgetService->onDestroy()");

		unregisterReceiver(mReceiver);

		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e(TAG, "ReminderWidgetService->onStartCommand()");

		handleCommand();

		return Service.START_STICKY;
	}

	private void handleCommand() {
		notifyWidget();

		sendBroadcast(new Intent(ACTION_UPDATE_WEATHER));
	}

	private Bitmap getWallpaerBitmap() {
		WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
		Drawable drawable = wallpaperManager.getDrawable();
		if (drawable == null) {
			return null;
		}
		Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
		if (bitmap == null) {
			return null;
		}
		return ReminderUtils.zoomBitmap(bitmap, 0.1f);
	}

	private void getBackgroudColor() {
		Bitmap newBitmap = getWallpaerBitmap();
		if (newBitmap == null) {
			color = DEFAULT_BACKGROUND_COLOR;
			return;
		}

		Palette palette = Palette.generate(newBitmap);
		color = palette.getVibrantColor(DEFAULT_BACKGROUND_COLOR);

		int color1 = palette.getDarkVibrantColor(DEFAULT_BACKGROUND_COLOR);
		int color2 = palette.getLightVibrantColor(DEFAULT_BACKGROUND_COLOR);
		int color3 = palette.getMutedColor(DEFAULT_BACKGROUND_COLOR);
		int color4 = palette.getDarkMutedColor(DEFAULT_BACKGROUND_COLOR);
		int color5 = palette.getLightMutedColor(DEFAULT_BACKGROUND_COLOR);

		Log.e("likai", color + ", " + color1 + ", " + color2 + ", " + color3 + ", " + color4 + ", " + color5);

		for (int i = 0; i < SPECIAL_WALLPAPER_COLORS.length; i++) {
			Log.e("likai", "i = " + i);
			int[] wallpaperColors = SPECIAL_WALLPAPER_COLORS[i];
			if (color == wallpaperColors[0] && color1 == wallpaperColors[1] &&
					color2 == wallpaperColors[2] && color3 == wallpaperColors[3] &&
					color4 == wallpaperColors[4] && color5 == wallpaperColors[5]) {
				color = SPECIAL_BACKGROUND_COLORS[i];
				Log.e("likai", "color = " + color);
				break;
			}
		}

		if (color == DEFAULT_BACKGROUND_COLOR) {
			color = color3;
			Log.e("likai", "color = " + color);
		}
	}

	private void getBackgroudBitmap() {
		getBackgroudColor();

		if (bgBitmap != null) {
			bgBitmap.recycle();
			bgBitmap = null;
		}
		bgBitmap = ReminderUtils.createBitmap(width, height, color, radius);
	}

	private void getTimeDateStr() {
		long millis = System.currentTimeMillis();
		timeStr = DateUtils.formatDateTime(this, millis, DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR);
		if (!DateFormat.is24HourFormat(this)) {
			String hourStr = timeStr.substring(0, 2);
			String otherStr = timeStr.substring(2);

			int hour = Integer.valueOf(hourStr);
			if (hour < 12) {
				ampmStr = getString(R.string.AM);
			} else {
				ampmStr = getString(R.string.PM);
			}

			if (hour == 0 || hour == 12) {
				hour = 12;
			} else {
				hour = hour % 12;
			}

			hourStr = String.valueOf(hour);
			timeStr = hourStr + otherStr;
		} else {
			ampmStr = "";
		}

		dateStr = new SimpleDateFormat(getString(R.string.date_format), Locale.getDefault()).format(new Date(millis));
	}

	private void getReminderInfo() {
		Cursor cursor = mResolver.query(ReminderUtils.CONTENT_URI, PROJECTION, 
				ReminderUtils.VISIBLE + "=1", null, ReminderUtils.LEVEL + " ASC");
		if (cursor != null && cursor.moveToFirst()) {
			title = cursor.getString(INDEX_TITLE);
			action = cursor.getString(INDEX_ACTION);
			packageName = cursor.getString(INDEX_PACKAGE);

			if (ReminderUtils.PACKAGE_WEATHER.equals(packageName)) {
				weatherTitle = title;
				weatherAction = action;
			}
		} else {
			title = "";
			action = "";
			packageName = ReminderUtils.PACKAGE_OWN;
		}
		if (cursor != null) {
			cursor.close();
			cursor = null;
		}
	}

	private void notifyWidget() {
		resetTitle();

		if (onEmoticon) {
			if (titleChange(title, tmpTitle)) {
				nextTitle = title;
				title = tmpTitle;

				nextAction = action;
				action = tmpAction;

				nextPackage = packageName;
				packageName = tmpPackage;
			}
			return;
		} else {
			nextTitle = "";
			nextAction = "";
			nextPackage = "";
		}

		if (titleChange(title, preTitle)) {
			startEmoticon();
			return;
		}

		RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.reminder_widget_layout);
		remoteViews.setImageViewBitmap(R.id.reminder_widget_bg, bgBitmap);
		remoteViews.setTextViewText(R.id.reminder_widget_time, timeStr);
		remoteViews.setTextViewText(R.id.reminder_widget_ampm, ampmStr);
		remoteViews.setTextViewText(R.id.reminder_widget_date, dateStr);
		remoteViews.setTextViewText(R.id.reminder_widget_title, title);

		remoteViews.setOnClickPendingIntent(R.id.reminder_widget_title, null);
		if (!TextUtils.isEmpty(action)) {
			Intent intent = new Intent();
			if (ReminderUtils.PACKAGE_PHONE.equals(packageName)) {
				intent.setAction(Intent.ACTION_VIEW);
				intent.setType("vnd.android.cursor.dir/calls");
			} else {
				intent.setClassName(ReminderUtils.PACKAGE_WEATHER, "com.aurora.weatherforecast.AuroraWeatherMain");
			}
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
			remoteViews.setOnClickPendingIntent(R.id.reminder_widget_title, pendingIntent);
		}

		Intent intent2 = new Intent(ACTION_EMOTICON_CLICKED);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent2, 0);
		remoteViews.setOnClickPendingIntent(R.id.reminder_widget_icon, pi);

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
		appWidgetManager.updateAppWidget(new ComponentName(this, ReminderWidgetProvider.class), remoteViews);
	}

	private void timeChange() {
		getTimeDateStr();
		notifyWidget();
	}

	private void dataChange() {
		getReminderInfo();
		notifyWidget();
	}

	private void bgChange() {
		getBackgroudBitmap();
		notifyWidget();
	}

	private void resetTitle() {
		if (ReminderUtils.PACKAGE_OWN.equals(packageName) || ReminderUtils.PACKAGE_WEATHER.equals(packageName)) {
			Time time = new Time();
			time.setToNow();

			boolean onGreet = false;
			String[] greetArray = null;

			int hour = time.hour;
			if (hour >= 6 && hour < 9) {
				onGreet = true;
				greetArray = goodMorningArray;
			} else if (hour >= 12 && hour < 14) {
				onGreet = true;
				greetArray = goodNoonArray;
			} else if (hour >= 18 && hour < 21) {
				onGreet = true;
				greetArray = goodNightArray;
			} else if (hour >= 23 || hour < 3) {
				onGreet = true;
				greetArray = goodMidnightArray;
			}

			if (onGreet && (TextUtils.isEmpty(greetTitle) || preHour != hour)) {
				preHour = hour;

				while (true) {
					int i = (int) (greetArray.length * Math.random());
					String greetStr = greetArray[i];
					if (!greetStr.equals(greetTitle)) {
						greetTitle = greetStr;
						break;
					}
				}
			}

			if (!onGreet) {
				title = weatherTitle;
				action = weatherAction;
			} else if (ReminderUtils.PACKAGE_OWN.equals(packageName)) {
				title = greetTitle;
				action = "";
			} else {
				int min = time.minute;
				if (min >= 0 && min < 15) {
					title = greetTitle;
					action = "";
				} else if (min >= 15 && min < 30) {
					title = weatherTitle;
					action = weatherAction;
				} else if (min >= 30 && min < 45) {
					title = greetTitle;
					action = "";
				} else {
					title = weatherTitle;
					action = weatherAction;
				}
			}
		}
	}

	private void setPreTitle() {
		if (onEmoticon) {
			tmpTitle = title;
			tmpAction = action;
			tmpPackage = packageName;
		} else {
			preTitle = title;
			preAction = action;
			prePackage = packageName;
		}
	}

	private boolean titleChange(String title1, String title2) {
		boolean titleChanged = false;
		if (!TextUtils.isEmpty(title1) && !title1.equals(title2)) {
			titleChanged = true;
		}
		return titleChanged;
	}

	/*private void scheduleWinkAlarm() {
		Intent intent = new Intent(ACTION_DO_WINK);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_NO_CREATE);
		if (pi != null) {
			mAlarmManager.cancel(pi);
		}

		pi = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
		long triggerAtMillis = SystemClock.currentThreadTimeMillis() + WINK_ALARM_PERIOD;
		mAlarmManager.set(AlarmManager.RTC, triggerAtMillis, pi);
	}

	private void scheduleWinkAlarmIfnot() {
		Intent intent = new Intent(ACTION_DO_WINK);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
		if (pi == null) {
			pi = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
			long triggerAtMillis = SystemClock.currentThreadTimeMillis() + WINK_ALARM_PERIOD;
			mAlarmManager.set(AlarmManager.RTC, triggerAtMillis, pi);
		}
	}*/

	private void resetEmoticon() {
		if (!TextUtils.isEmpty(title)) {
			if (ReminderUtils.PACKAGE_PHONE.equals(packageName)) {
				emoticon = EMOTICON_SPIN;
			} else if (!titleChange(title, greetTitle)) {
				Time time = new Time();
				time.setToNow();

				int hour = time.hour;
				if (hour >= 6 && hour < 9) {
					emoticon = EMOTICON_JUMP;
				} else if (hour >= 12 && hour < 14) {
					emoticon = EMOTICON_JUMP;
				} else if (hour >= 18 && hour < 21) {
					emoticon = EMOTICON_JUMP;
				} else if (hour >= 23 || hour < 3) {
					emoticon = EMOTICON_DOZE;
				} else {
					emoticon = EMOTICON_JUMP;
				}
			} else if (!titleChange(title, weatherTitle)) {
				int i = (int) (2 * Math.random());
				if (i == 1) {
					emoticon = EMOTICON_LOOKAROUND;
				} else {
					emoticon = EMOTICON_ROLL;
				}
			} else {
				emoticon = EMOTICON_JUMP;
			}
		} else {
			emoticon = EMOTICON_JUMP;
		}
	}

	private void startEmoticon() {
		resetEmoticon();

		if (emoticon == null) return;

		onEmoticon = true;
		index = 0;

		if (timer == null) {
			timer = new Timer();
		}
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				onEmoticon();
			}
		}, 0, ANIMATION_PERIOD);
	}

	private void onEmoticon() {
		if (index < emoticon.length) {
			notifyWidgetAnimation();
			if (index == emoticon.length - 1) {
				stopEmoticon();
			} else {
				index++;
			}
		} else {
			stopEmoticon();
		}
	}

	private void stopEmoticon() {
		onEmoticon = false;
		index = 0;

		timer.cancel();
		timer = null;

		if (!TextUtils.isEmpty(nextTitle)) {
			preTitle = title;
			title = nextTitle;
			nextTitle = "";

			preAction = action;
			action = nextAction;
			nextAction = "";

			prePackage = packageName;
			packageName = nextPackage;
			nextPackage = "";

			startEmoticon();
		} else if (changeBackgroundLater) {
			mHanlder.sendEmptyMessageDelayed(2, 50);
		}
	}

	private void notifyWidgetAnimation() {

		String titleStr, actionStr, packageStr;
		if (!titleChange(title, preTitle)) {
			titleStr = title;
			actionStr = action;
			packageStr = packageName;
		} else if (index < 13) {
			titleStr = preTitle;
			actionStr = preAction;
			packageStr = prePackage;
		} else if (index < 16) {
			titleStr = "";
			actionStr = "";
			packageStr = "";
		} else {
			titleStr = title;
			actionStr = action;
			packageStr = packageName;
		}

		RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.reminder_widget_layout);
		remoteViews.setImageViewBitmap(R.id.reminder_widget_bg, bgBitmap);
		remoteViews.setTextViewText(R.id.reminder_widget_time, timeStr);
		remoteViews.setTextViewText(R.id.reminder_widget_ampm, ampmStr);
		remoteViews.setTextViewText(R.id.reminder_widget_date, dateStr);
		remoteViews.setTextViewText(R.id.reminder_widget_title, titleStr);
		remoteViews.setImageViewResource(R.id.reminder_widget_icon, emoticon[index]);

		remoteViews.setOnClickPendingIntent(R.id.reminder_widget_title, null);
		if (!TextUtils.isEmpty(actionStr)) {
			Intent intent = new Intent();
			if (ReminderUtils.PACKAGE_PHONE.equals(packageStr)) {
				intent.setAction(Intent.ACTION_VIEW);
				intent.setType("vnd.android.cursor.dir/calls");
			} else {
				intent.setClassName(ReminderUtils.PACKAGE_WEATHER, "com.aurora.weatherforecast.AuroraWeatherMain");
			}
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
			remoteViews.setOnClickPendingIntent(R.id.reminder_widget_title, pendingIntent);
		}

		Intent intent2 = new Intent(ACTION_EMOTICON_CLICKED);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent2, 0);
		remoteViews.setOnClickPendingIntent(R.id.reminder_widget_icon, pi);

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
		try {
			appWidgetManager.updateAppWidget(new ComponentName(this, ReminderWidgetProvider.class), remoteViews);
		} catch (IllegalStateException e) {
			Log.e(TAG, "ReminderWidgetService->notifyWidgetAnimation() IllegalStateException");
		}
	}

}
