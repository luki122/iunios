package com.android.contacts.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
//import aurora.widget.AuroraSpinner.ItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.android.contacts.ContactsUtils;
import com.android.contacts.R;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraSpinner;
import aurora.preference.AuroraPreferenceManager;


// author: aurora wangth 20140325
public class AuroraCallRecordActivity extends AuroraActivity{
    
    private static String TAG = "AuroraCallRecordActivity";
	private Context mContext;
	private RelativeLayout mGotoHistory;
	private AuroraSpinner mAutoRecord;
	private RelativeLayout mGotoCallRecordContactsList;
	private AutoRecordTypeAdapter mAdapter;
	private SharedPreferences mPrefs;
	private int mRecordType = 0;
	
	private static final String AURORA_CALL_RECORD_TYPE = "aurora.call.record.type";
	private static final String AURORA_CALL_RECORD_ACTION = "com.android.contacts.AURORA_CALL_RECORD_ACTION";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		mContext = AuroraCallRecordActivity.this;
		setAuroraContentView(R.layout.aurora_call_record_activity,
                AuroraActionBar.Type.Normal);
		AuroraActionBar actionBar = getAuroraActionBar();
        actionBar.setTitle(R.string.aurora_call_record);
        
        mGotoHistory = (RelativeLayout) findViewById(R.id.aurora_record_history);
        if (null != mGotoHistory) {
            mGotoHistory.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
//                    Intent in = new Intent();
//                    in.setAction("com.aurora.filemanager.action");
//                    in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    if (null == in) {
//                        return;
//                    }
//                    
//                    try {
//                        Bundle bundle = new Bundle();
//                        bundle.putBoolean("com.aurora.audio.file.manager.action", true);
//                        in.putExtras(bundle);
//                        startActivity(in);
//                    } catch (ActivityNotFoundException a) {
//                        a.printStackTrace();
//                    }
                	
                	Intent in = new Intent(AuroraCallRecordActivity.this, AuroraCallRecordHistoryActivity.class);
                    startActivity(in);
                }
            });
        }
        
        mGotoCallRecordContactsList = (RelativeLayout) findViewById(R.id.special_auto_record);
        if (mGotoCallRecordContactsList != null) {
            mGotoCallRecordContactsList.setVisibility(View.GONE);
            mGotoCallRecordContactsList.setOnClickListener(new OnClickListener() {
                
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
                    
                    Intent in = new Intent(AuroraCallRecordActivity.this, AuroraCallRecordContactListActivity.class);
                    in.putExtra("aurora_call_record_select", true);
                    startActivity(in);
                }
            });
        }
        
        mAutoRecord = (AuroraSpinner) findViewById(R.id.aurora_auto_record_sp);
        mAutoRecord.setPosition(AuroraSpinner.POSITION_RIGHT_NORMAL);
        mAdapter = new AutoRecordTypeAdapter(mContext);
        mAutoRecord.setAdapter(mAdapter);
        mPrefs = AuroraPreferenceManager.getDefaultSharedPreferences(this);
        mRecordType = mPrefs.getInt(AURORA_CALL_RECORD_TYPE, 0);
        mAutoRecord.setSelection(mRecordType);
        sendBroast(mRecordType);
        mAutoRecord.setOnItemSelectedListener(mSpinnerListener);
        
//        if (mRecordType == 2) {
//            mGotoCallRecordContactsList.setVisibility(View.VISIBLE);
//            mGotoCallRecordContactsList.setEnabled(true);
//            
//        } else {
//            mGotoCallRecordContactsList.setVisibility(View.GONE);
//            mGotoCallRecordContactsList.setEnabled(false);
//        }
	}
	
	private OnItemSelectedListener mSpinnerListener = new OnItemSelectedListener() {

	    @Override
        public void onItemSelected(
                AdapterView<?> parent, View view, int position, long id) {
	        
            mPrefs.edit().putInt(AURORA_CALL_RECORD_TYPE, position).apply();
            
            Log.e(TAG, "position = " + position);
            // select contacts
            if (position == 2 && null != mGotoCallRecordContactsList) {
                mGotoCallRecordContactsList.setVisibility(View.VISIBLE);
                mGotoCallRecordContactsList.setEnabled(true);
            } else if (null != mGotoCallRecordContactsList){
                mGotoCallRecordContactsList.setVisibility(View.GONE);
                mGotoCallRecordContactsList.setEnabled(false);
            }
            
            sendBroast(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
	    
//        @Override
//        public void itemClick(AdapterView<?> parent, View view, int position, long id) {
//            mPrefs.edit().putInt(AURORA_CALL_RECORD_TYPE, position).apply();
//            
//            Log.e("wangth", "position = " + position);
//            // select contacts
//            if (position == 2 && null != mGotoCallRecordContactsList) {
//                mGotoCallRecordContactsList.setVisibility(View.VISIBLE);
//                mGotoCallRecordContactsList.setEnabled(true);
//            } else if (null != mGotoCallRecordContactsList){
//                mGotoCallRecordContactsList.setVisibility(View.GONE);
//                mGotoCallRecordContactsList.setEnabled(false);
//            }
//            
//            sendBroast(position);
//        }
    };
    
    // send to phone (0:close; 1:all; 2:select)
    private void sendBroast(int position) {
        Intent intent = new Intent(AURORA_CALL_RECORD_ACTION);
        intent.putExtra(AURORA_CALL_RECORD_TYPE, position);
        mContext.sendBroadcast(intent);
    }
    
    private class AutoRecordTypeAdapter extends ArrayAdapter<String> {
        
        private final LayoutInflater mInflater;
        
        public AutoRecordTypeAdapter(Context context) {
            super(context, 0);
            
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            
            add(context.getResources().getString(R.string.aurora_record_close));
            add(context.getResources().getString(R.string.aurora_record_all_contacts));
            add(context.getResources().getString(R.string.aurora_record_select_contact));
        }
        
        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return createViewFromResource(
                    position, convertView, parent, com.aurora.R.layout.aurora_spinner_list_item);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return createViewFromResource(
                    position, convertView, parent, R.layout.aurora_call_record_spinner_item);
        }
        
        private View createViewFromResource(int position, View convertView, ViewGroup parent,
                int resource) {
            TextView textView;

            if (convertView == null) {
                textView = (TextView) mInflater.inflate(resource, parent, false);
                textView.setEllipsize(TruncateAt.MARQUEE);
            } else {
                textView = (TextView) convertView;
            }

            String text = getItem(position);
            textView.setText(text);
            return textView;
        }
    }
}
