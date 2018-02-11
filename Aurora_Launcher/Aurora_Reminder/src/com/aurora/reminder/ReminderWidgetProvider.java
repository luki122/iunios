package com.aurora.reminder;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ReminderWidgetProvider extends AppWidgetProvider {

	public static final String TAG = "ReminderWidget";
	public static final String ACTION_WIDGET_SERVICE = "com.aurora.reminder.WIDGET_SERVICE";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.e(TAG, "ReminderWidgetProvider->onReceive() action = " + action);

		super.onReceive(context, intent);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.e(TAG, "ReminderWidgetProvider->onUpdate()");

		startReminderService(context);

		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	@Override
	public void onDisabled(Context context) {
		Log.e(TAG, "ReminderWidgetProvider->onDisabled()");

		stopReminderService(context);

		super.onDisabled(context);
	}

	private void startReminderService(Context context) {
		Intent intent = new Intent(ACTION_WIDGET_SERVICE);
		intent.setPackage(context.getPackageName());
		context.startService(intent);
	}

	private void stopReminderService(Context context) {
		Intent intent = new Intent(ACTION_WIDGET_SERVICE);
		intent.setPackage(context.getPackageName());
		context.stopService(intent);
	}

}
