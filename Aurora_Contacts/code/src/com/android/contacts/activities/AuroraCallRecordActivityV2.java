package com.android.contacts.activities;

import gionee.provider.GnContactsContract;
import gionee.provider.GnContactsContract.Directory;
import gionee.provider.GnContactsContract.RawContacts;
import gionee.provider.GnContactsContract.CommonDataKinds.Phone;
import android.R.integer;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils.TruncateAt;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
//import aurora.widget.AuroraSpinner.ItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.R;
import com.privacymanage.service.AuroraPrivacyUtils;

import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraSpinner;
import aurora.widget.AuroraSwitch;
import aurora.preference.AuroraPreferenceManager;


// author: aurora wangth 20140325
public class AuroraCallRecordActivityV2 extends AuroraActivity{
    
    private static String TAG = "AuroraCallRecordActivityV2";
	private Context mContext;
	private RelativeLayout mGotoHistory;
	private AuroraSwitch mAutoRecord;
	private TextView mGotoCallRecordContactsList, mContactsName;
	SharedPreferences mPrefs;
	private int mRecordType = 0;
	private RadioGroup mRadio;
	private RadioButton mAll, mSelect; 
	
	private static final String AURORA_CALL_RECORD_TYPE = "aurora.call.record.type";
	private static final String AURORA_CALL_RECORD_ACTION = "com.android.contacts.AURORA_CALL_RECORD_ACTION";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ContactsApplication.sendSimContactBroad();
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		mContext = AuroraCallRecordActivityV2.this;
		setAuroraContentView(R.layout.aurora_call_record_activity_v2,
                AuroraActionBar.Type.Normal);
		AuroraActionBar actionBar = getAuroraActionBar();
        actionBar.setTitle(R.string.aurora_call_record);
        
        mGotoHistory = (RelativeLayout) findViewById(R.id.aurora_record_history);
        if (null != mGotoHistory) {
            mGotoHistory.setOnClickListener(mGotoHistoryClickListener);
        }
        
        mGotoCallRecordContactsList = (TextView) findViewById(R.id.edit_contacts);
        if (mGotoCallRecordContactsList != null) {
//            mGotoCallRecordContactsList.setVisibility(View.GONE);
            mGotoCallRecordContactsList.setOnClickListener(mGotoContactsClickListener);
        }

        mPrefs = AuroraPreferenceManager.getDefaultSharedPreferences(this);
        mRecordType = mPrefs.getInt(AURORA_CALL_RECORD_TYPE, 0);
        mIsAll = (mRecordType == 1) || (mRecordType == 0);
        mAutoRecord = (AuroraSwitch) findViewById(R.id.aurora_auto_record_switch);
        mAutoRecord.setChecked(mRecordType > 0);
        mAutoRecord.setOnCheckedChangeListener(mAutoSwitchListener);	
        sendBroast(mRecordType);
        
        mAll = (RadioButton) findViewById(R.id.all_record);
        mSelect = (RadioButton) findViewById(R.id.select_record);
        mRadio = (RadioGroup) findViewById(R.id.auto_group);
        if(mIsAll) {
        	mAll.setChecked(true);
        } else {
        	mSelect.setChecked(true);
        }
        mRadio.setOnCheckedChangeListener(mAutoRadioSelectListener);
        
        mContactsName = (TextView) findViewById(R.id.aurora_record_contacts);
        mContactsName.setMovementMethod(new ScrollingMovementMethod());
        updateUI();
	}
	
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		// TODO Auto-generated method stub
		super.onResume();
		if(mIsNeedRefresh) {
			mIsNeedRefresh = false;
		     updateUI();
		}
	}
        	
    
    // send to phone (0:close; 1:all; 2:select)
    private void sendBroast(int position) {
        Intent intent = new Intent(AURORA_CALL_RECORD_ACTION);
        intent.putExtra(AURORA_CALL_RECORD_TYPE, position);
        mContext.sendBroadcast(intent);
    }
    
    
    private OnClickListener mGotoHistoryClickListener = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub                	
        	Intent in = new Intent(AuroraCallRecordActivityV2.this, AuroraCallRecordHistoryActivity.class);
            startActivity(in);
        }
    };
    
    private OnClickListener mGotoContactsClickListener = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            
            if (AuroraCallRecordContactListActivity.mIsAddAutoRecording) {
                ContactsUtils.toastManager(mContext, R.string.aurora_auto_recording);
                return;
            }
            
            if (AuroraCallRecordContactListActivity.mIsRemoving) {
                ContactsUtils.toastManager(mContext, R.string.aurora_auto_recording_remove);
                return;
            }
            
            Intent in = new Intent(AuroraCallRecordActivityV2.this, AuroraCallRecordContactListActivity.class);
            in.putExtra("aurora_call_record_select", true);
            startActivity(in);
            mIsNeedRefresh = true;
        }
    };
    
    private OnCheckedChangeListener mAutoSwitchListener = new OnCheckedChangeListener() {    
    	@Override
 		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    		int value = isChecked ? 1 : 0;
    		if(isChecked) {
    			value = mIsAll ? 1 : 2;
    		} else {
    			value = 0;
    		}
    		mRecordType = value;
	    	setValue();
    	}
    };
    
    private boolean mIsAll = false;
    private RadioGroup.OnCheckedChangeListener mAutoRadioSelectListener = new RadioGroup.OnCheckedChangeListener() {    
    	@Override
 		public void onCheckedChanged(RadioGroup group, int checkedId) {
	    	if(mAll.getId()==checkedId){
	    		mIsAll = true;
	    	} else if(mSelect.getId()==checkedId){
	    		mIsAll = false;
	    	}
	    	mRecordType = mIsAll ? 1 : 2;
	    	setValue();
	    }    	
    };
    
    private void setValue() {
        mPrefs.edit().putInt(AURORA_CALL_RECORD_TYPE, mRecordType).apply();                
        sendBroast(mRecordType);
        updateUI();
    }
    
    private boolean mIsNeedRefresh;
	private void updateUI() {
		if(mAutoRecord.isChecked()) {
			mRadio.setVisibility(View.VISIBLE);
			mGotoCallRecordContactsList.setVisibility(View.VISIBLE);
			if(mSelect.isChecked()) {
				updateContactNames();
				mGotoCallRecordContactsList.setEnabled(true);
			} else {
				mGotoCallRecordContactsList.setEnabled(false);
				mContactsName.setText("");
			}		
		} else {
			mRadio.setVisibility(View.GONE);
			mGotoCallRecordContactsList.setVisibility(View.GONE);
			mContactsName.setText("");
			mGotoCallRecordContactsList.setEnabled(false);
		}			
	}
	
	private void updateContactNames() {
		long currentPrivacyId = AuroraPrivacyUtils.mCurrentAccountId;
		StringBuilder selection = new StringBuilder();
		selection.append(RawContacts.INDICATE_PHONE_SIM + " < 0");
		selection.append(" AND auto_record = 1");
		if (ContactsApplication.sIsAuroraPrivacySupport && currentPrivacyId > 0) {
			selection.append(" AND is_privacy IN (0, " + currentPrivacyId + ")");
		}
		Uri uri = Phone.CONTENT_URI.buildUpon().appendQueryParameter(
				GnContactsContract.DIRECTORY_PARAM_KEY, String.valueOf(Directory.DEFAULT))
				.build();
		uri = uri.buildUpon()
				.appendQueryParameter(GnContactsContract.REMOVE_DUPLICATE_ENTRIES, "true")
				.build();
				
		final String[] PROJECTION_PRIMARY = new String[] {
			Phone._ID,                          // 0
			Phone.TYPE,                         // 1
			Phone.LABEL,                        // 2
			Phone.NUMBER,                       // 3
			Phone.CONTACT_ID,                   // 4
			Phone.LOOKUP_KEY,                   // 5
			Phone.PHOTO_ID,                     // 6
			Phone.DISPLAY_NAME_PRIMARY,         // 7
			RawContacts.INDICATE_PHONE_SIM,     // 8

			"auto_record",
			"is_privacy",
		};

		Cursor cursor = getContentResolver().query(uri, PROJECTION_PRIMARY, selection.toString(), null, null);
		StringBuilder contactNames = new StringBuilder();
		int count = 0;
		if(cursor != null) {
			while (cursor.moveToNext()) {		
				count ++;
				String name = cursor.getString(cursor
						.getColumnIndex(Phone.DISPLAY_NAME_PRIMARY));
				contactNames.append(name);
				contactNames.append("\n");
				Log.d(TAG, "Name is: : " + name);
			}
			cursor.close();
		}
		if(count > 0) {
			mSelect.setText(getString(R.string.aurora_record_select_contact) + "(" + count + ")");
		} else {
			mSelect.setText(R.string.aurora_record_select_contact);
		}
		mContactsName.setText(contactNames.toString()); 
	}
}
