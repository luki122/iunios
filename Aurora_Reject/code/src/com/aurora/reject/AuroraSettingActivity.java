package com.aurora.reject;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.aurora.reject.util.RejectApplication;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraSwitchPreference;
import aurora.widget.AuroraActionBar;

public class AuroraSettingActivity extends AuroraPreferenceActivity implements OnPreferenceChangeListener {
	private AuroraActionBar mActionBar;
	private AuroraPreference black_name,mark;
	private AuroraSwitchPreference sms,intercept;
	private int black_list_size=0;
	private int mark_list_size=0;
	private static Uri uri_black = Uri.parse("content://com.android.contacts/black");
	private static Uri uri_mark= Uri.parse("content://com.android.contacts/mark");
	private ContentResolver mContentResolver;
	private AsyncQueryHandler mQueryHandler;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mActionBar = getAuroraActionBar();
		mActionBar.setTitle(getResources().getString(R.string.setting));
		mActionBar.setDisplayHomeAsUpEnabled(true);
		addPreferencesFromResource(R.layout.set);
		
		
		sms=(AuroraSwitchPreference)findPreference("sms");
		intercept=(AuroraSwitchPreference)findPreference("intercept");
		black_name=(AuroraPreference)findPreference("black_name");
		mark=(AuroraPreference)findPreference("mark");
		
		
		
		sms.setOnPreferenceChangeListener(this);
		intercept.setOnPreferenceChangeListener(this);
		mContentResolver = getContentResolver();
		mQueryHandler = new QueryHandler(mContentResolver, this);
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mQueryHandler.startQuery(0, null, uri_black, null, "isblack=1 and reject>0", null, null);
		mQueryHandler.startQuery(1, null, uri_mark, null, null, null, null);
	}

	@Override
	public boolean onPreferenceChange(AuroraPreference arg0, Object arg1) {
		// TODO Auto-generated method stub
		boolean b=(Boolean) arg1;
		String s=arg0.getKey();
		if(s.equals("sms")){
			Intent intent = new  Intent("com.android.reject.RUBBISH_MSG_REJECT" );
	        intent.putExtra("isRejectRubbishMsg", b);
	        sendBroadcast(intent); 
		}else {
			Intent intent = new  Intent("com.android.reject.BLACK_MSG_REJECT" );
	        intent.putExtra("isRejectBlack", b);
	        sendBroadcast(intent); 
		}
		return true;
	}
	
	@Override
	public boolean onPreferenceTreeClick(
			AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
		// TODO Auto-generated method stub
		String key=preference.getKey();
		if(key.equals("black_name")){
			Intent intent = new Intent(getApplicationContext(), AuroraBlackNameActivity.class);
			startActivity(intent);
		}else if(key.equals("mark")){
			Intent intent = new Intent(getApplicationContext(), AuroraMarkActivity.class);
			startActivity(intent);
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
	
	private class QueryHandler extends AsyncQueryHandler {
		private final Context context;

		public QueryHandler(ContentResolver cr, Context context) {
			super(cr);
			this.context = context;
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			// TODO Auto-generated method stub
			super.onQueryComplete(token, cookie, cursor);
			if (cursor != null) {
				if(token==0){
					black_list_size=cursor.getCount();
					black_name.auroraSetArrowText(black_list_size+getResources().getString(R.string.item), true);
					
				}else{
					mark_list_size=cursor.getCount();
					mark.auroraSetArrowText(mark_list_size+getResources().getString(R.string.item), true);
				}
				
			}
			if(cursor!=null){
				cursor.close();
			}
		}

		@Override
		protected void onUpdateComplete(int token, Object cookie, int result) {
			// TODO Auto-generated method stub
			super.onUpdateComplete(token, cookie, result);
		}

		@Override
		protected void onInsertComplete(int token, Object cookie, Uri uri) {
			// TODO Auto-generated method stub
			super.onInsertComplete(token, cookie, uri);
		}

		@Override
		protected void onDeleteComplete(int token, Object cookie, int result) {
			// TODO Auto-generated method stub
			super.onDeleteComplete(token, cookie, result);
			System.out.println("删除完毕" + result);
		}

	}

}
