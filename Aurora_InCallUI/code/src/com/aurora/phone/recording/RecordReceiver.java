package com.android.incallui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class RecordReceiver extends BroadcastReceiver {
	
	private static final String AURORA_CALL_RECORD_TYPE = "aurora.call.record.type";
	private static final String AURORA_CALL_RECORD_ACTION = "com.android.contacts.AURORA_CALL_RECORD_ACTION";
	private static int mRecordSelection = 0;
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(AURORA_CALL_RECORD_ACTION)) {
				// send to phone (0:close; 1:all; 2:select)
				mRecordSelection = intent.getIntExtra(AURORA_CALL_RECORD_TYPE,
						0);
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
				SharedPreferences.Editor editor = sp.edit();
				editor.putInt("record_mode", mRecordSelection);
				editor.commit();
			}
		}
	}