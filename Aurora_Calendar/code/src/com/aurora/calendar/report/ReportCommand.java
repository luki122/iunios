package com.aurora.calendar.report;

import android.content.Context;
import android.util.Log;

import java.util.concurrent.ThreadPoolExecutor;

public class ReportCommand implements Runnable {

	public static final String TAG = "ReportCommand";

	private Context context;
	private String itemTag;
	private int value;

	public ReportCommand(Context context, String itemTag) {
		this.context = context;
		this.itemTag = itemTag;
		this.value = 1;
	}

	public ReportCommand(Context context, String itemTag, int value) {
		this.context = context;
		this.itemTag = itemTag;
		this.value = value;
	}

	@Override
	public void run() {
		int result = ReportUtil.updateData(context, itemTag, value);
		Log.i(TAG, "itemTag : " + itemTag + ", value : " + value + " --> result = " + result);
	}

	public void updateData() {
		ThreadPoolExecutor threadPool = ReportManager.getThreadPoolExecutor();
		threadPool.execute(this);
	}

}