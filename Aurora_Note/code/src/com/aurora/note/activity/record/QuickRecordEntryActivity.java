package com.aurora.note.activity.record;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.aurora.note.NoteMainActivity;
import com.aurora.note.activity.BaseActivity;

public class QuickRecordEntryActivity extends BaseActivity {
	private static final String TAG = "QuickRecordEntryActivity";
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "Jim, quick record entry entered");
		if (!RecordActivity2.sIsRecording && !PlayActivity2.sIsPlaying) {
		    Intent intent = new Intent(this, NoteMainActivity.class);
	        intent.putExtra(NoteMainActivity.EXTRA_KEY_COME_FROM_QUICK_RECORD, true);
	        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        startActivity(intent);
		}/* else {
		    Intent intent = new Intent(this, RecordActivity2.class);
		    startActivity(intent);
		}*/
		finish();
	}
}