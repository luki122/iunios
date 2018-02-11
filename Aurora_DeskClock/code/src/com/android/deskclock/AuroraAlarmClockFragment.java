package com.android.deskclock;

import java.util.ArrayList;
import java.util.Calendar;

import com.android.deskclock.Alarm;
import com.android.deskclock.Alarms;
import com.android.deskclock.AuroraSetAlarm;
import com.android.deskclock.GnSelectionManager;
import com.android.deskclock.ToastMaster;
import com.aurora.tosgallery.TosAdapterView;
import com.aurora.tosgallery.TosAdapterView.OnItemClickListener;
import com.aurora.tosgallery.TosAdapterView.OnItemSelectedListener;
import com.aurora.tosgallery.TosGallery;
import com.aurora.tosgallery.TosGallery.OnEndFlingListener;
import com.aurora.tosgallery.WheelView;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraListView.AuroraBackOnClickListener;
import aurora.widget.AuroraSwitch;

public class AuroraAlarmClockFragment extends Fragment implements
	OnItemClickListener, OnClickListener, OnEndFlingListener{
	
	public static final String NEAREST_ALARM_PREFERENCES = "NearestAlarm";
	private LayoutInflater mFactory;
	private WheelView mAlarmsList;
	private Cursor mCursor;
	private AuroraActivity mActivity;
	private Context mContext;
	private AlarmTimeAdapter adapter;
	private View mRootView;
	private TextView noAlarmTextView;
	private LinearLayout noAlarmView;
	
	private ImageView alarmSwitch;
	private int curSelectPositon = 0;
	
	public static final int REQUEST_CODE = 1;
	
	private class AlarmTimeAdapter extends CursorAdapter {
        // Gionee baorui 2013-01-05 modify for CR00746150 end
		
		private int curSelectPos = 0;

        public AlarmTimeAdapter(Context context, Cursor cursor) {
			super(context, cursor);
		}
        
        public void setChangedPosition( int pos ) {
        	curSelectPos = pos;
        	notifyDataSetChanged();
        }

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {

	        View view = (View) mFactory.inflate(R.layout.aurora_alarm_time, parent, false);
	        
			DigitalClock digitalClock = (DigitalClock) view.findViewById(R.id.digitalClock);
			digitalClock.setLive(false);
			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {

			final Alarm alarm = new Alarm(cursor);
			
			LinearLayout alarm_time_linear = (LinearLayout)view.findViewById(R.id.alarm_time_linear);
			
			if ( cursor.getPosition() == curSelectPos ) {
				alarm_time_linear.setBackgroundResource(R.drawable.alarmlargecycleopen);
			}
            
			DigitalClock digitalClock = (DigitalClock) view
					.findViewById(R.id.digitalClock);

			// set the alarm text
			final Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR_OF_DAY, alarm.hour);
			c.set(Calendar.MINUTE, alarm.minutes);
			digitalClock.updateTime(c);

			// Set the repeat text or leave it blank if it does not repeat.
			TextView daysOfWeekView = (TextView) view
					.findViewById(R.id.daysOfWeek);

			final String daysOfWeekStr = alarm.daysOfWeek.toString(mActivity,
					false);
            if (daysOfWeekStr != null && daysOfWeekStr.length() != 0) {

            	daysOfWeekView.setText(daysOfWeekStr);
            	Log.e("222222", "---------daysOfWeekStr = ---------" + daysOfWeekStr);
                daysOfWeekView.setVisibility(View.VISIBLE);
            } else {

                daysOfWeekView.setText(R.string.onlyonce);

                daysOfWeekView.setVisibility(View.VISIBLE);
            }
            
            LeftTimeTextView timetoalarmText = (LeftTimeTextView) view.findViewById(R.id.timetoalarm);
            timetoalarmText.setLeftTimeAlarm(alarm);

            /*
			// Display the label
			TextView labelView = (TextView) view.findViewById(R.id.label);
			if (alarm.label != null && alarm.label.length() != 0) {
				labelView.setText(alarm.label);
				labelView.setVisibility(View.VISIBLE);

			} else {
				labelView.setText(null);
				labelView.setHint(R.string.label);
				labelView.setVisibility(View.VISIBLE);
			}
			*/
		}
	};
	
    private void updateAlarm(boolean enabled, Alarm alarm) {
        Alarms.enableAlarm(mActivity, alarm.id, enabled);

        if (enabled) {
            AuroraSetAlarm.popAlarmSetToast(mActivity, alarm.hour, alarm.minutes, alarm.daysOfWeek);
        }
    }
    
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		if (adapter.getCount() > 0) {
			noAlarmView.setVisibility(View.GONE);
		} else {
			noAlarmView.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public void onAttach(Activity activity) {
			Log.i("222222", "onAttach");
		super.onAttach(activity);
        mActivity = (AuroraActivity) activity;
        // mContext = mActivity.getApplicationContext();
        mContext = mActivity;
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.i("222222", "onCreate");
		mFactory = LayoutInflater.from(mActivity);
		mCursor = Alarms.getAlarmsCursor(mActivity.getContentResolver());
		super.onCreate(savedInstanceState);
	}
	
	/**
	 * @param position auroraDeleteAlarm
	 */
	public void auroraDeleteAlarm( int position ) {
        final Cursor c = (Cursor) mAlarmsList.getAdapter().getItem(position);
        final Alarm alarm = new Alarm(c);
        
        Alarms.deleteAlarm(mContext, alarm.id);
        if (Alarms.mIfDismiss == true && Alarms.mAlarmId == alarm.id) {

            mActivity.stopService(new Intent(Alarms.ALARM_ALERT_ACTION));
        }
        
        // aurora mo by tangjun 2013.12.20
        //NotificationOperate.cancelNotification(mContext, alarm.id);

        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = Alarms.getAlarmsCursor(mActivity.getContentResolver());
        adapter.changeCursor(mCursor);
        onResume();
	}

	private void updateLayout() {
		Log.i("222222", "updateLayout()");

		noAlarmTextView = (TextView) mRootView.findViewById(R.id.no_alarm_text);
		noAlarmTextView.setText(R.string.no_set);

		mAlarmsList = (WheelView) mRootView.findViewById(R.id.wheel_alarm);
		noAlarmView = (LinearLayout) mRootView.findViewById(R.id.alarm_list_empty);
		adapter = new AlarmTimeAdapter(mActivity, mCursor);

		mAlarmsList.setAdapter(adapter);
		//mAlarmsList.setVerticalScrollBarEnabled(true);
		mAlarmsList.setOnItemClickListener(this);
		mAlarmsList.setOnEndFlingListener(this);
		mAlarmsList.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(TosAdapterView<?> parent, View view,
					final int position, long id) {
				// TODO Auto-generated method stub
				Log.e("111111", "------onEndFling pos = " + position);
				new Handler().postDelayed(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						//adapter.setChangedPosition(position);
						changeAlarmSwitchState( );
					}
				}, 300);
			}

			@Override
			public void onNothingSelected(TosAdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
		
		//adapter.setChangedPosition(mAlarmsList.getSelectedItemPosition());

		alarmSwitch = (ImageView)mRootView.findViewById(R.id.alarmswitch);
		alarmSwitch.setOnClickListener(this);
		changeAlarmSwitchState( );
	}
	
	private void changeAlarmSwitchState( ) {
		curSelectPositon = mAlarmsList.getSelectedItemPosition();
		final Cursor c = (Cursor) mAlarmsList.getAdapter().getItem(curSelectPositon);
        Alarm alarm = new Alarm(c);
        if ( alarm.enabled ) {
        	alarmSwitch.setImageResource(R.drawable.alarmopen);
        } else {
        	alarmSwitch.setImageResource(R.drawable.alarmclose);
        }
	}
	
    @Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  
            Bundle savedInstanceState) {  
    	mRootView = inflater.inflate(R.layout.aurora_alarm_clock,  
                container, false);  
        updateLayout();

        //initGnActionModeHandler();

        //mActivity.registerReceiver(mReceiver, filter);
        
        return mRootView;  
    }  
    
    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();
        //mActivity.unregisterReceiver(mReceiver);
    }
    
	@Override
	public void onDestroy() {
		super.onDestroy();
		ToastMaster.cancelToast();
		if (mCursor != null) {
			mCursor.close();
		}
	}
	
    public void auroraAddNewAlarm() {
    	startActivity(new Intent(mActivity, AuroraSetAlarm.class));
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if ( v.getId() == R.id.alarmswitch) {
			final Cursor c = (Cursor) mAlarmsList.getAdapter().getItem(curSelectPositon);
	        Alarm alarm = new Alarm(c);
	        if ( alarm.enabled ) {
	        	alarmSwitch.setImageResource(R.drawable.alarmclose);
	        } else {
	        	alarmSwitch.setImageResource(R.drawable.alarmopen);
	        }
	        updateAlarm(!alarm.enabled, alarm);
		}
	}

	@Override
	public void onItemClick(TosAdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		if ( curSelectPositon != position ) {
			return;
		}
		final Cursor c = (Cursor) mAlarmsList.getAdapter().getItem(position);
        final Alarm alarm = new Alarm(c);

        Intent intent = new Intent(mActivity, AuroraSetAlarm.class);
        intent.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
        
        intent.putExtra("position", position);
        //startActivity(intent);
        mActivity.startActivityForResult(intent, REQUEST_CODE);
	}

	@Override
	public void onEndFling(TosGallery v) {
		// TODO Auto-generated method stub
		//Thread.dumpStack();
		int pos = v.getSelectedItemPosition();
	}
}
