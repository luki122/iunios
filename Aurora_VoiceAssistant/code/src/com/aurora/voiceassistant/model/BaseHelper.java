package com.aurora.voiceassistant.model;

import android.content.Context;

public class BaseHelper {

	protected static final String TAG = "DEBUG_BaseHelper";

	protected Context mContext;

	/** Log out control */
	protected final boolean DEBUG = false;

	public interface BaseHelperEvent {
		public void scheduleNewEvent();
	}

}
