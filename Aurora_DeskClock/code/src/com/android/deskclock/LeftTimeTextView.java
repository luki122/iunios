package com.android.deskclock;

import java.util.Calendar;

import com.android.deskclock.Log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.TextView;
import aurora.provider.AuroraSettings;

public class LeftTimeTextView extends TextView{
	
	private boolean mLive = true;
	private boolean mAttached;
	private Context mContext;
	private Alarm mAlarm;
    /* called by system on minute ticks */
    private final Handler mHandler = new Handler(){
    	public void handleMessage(android.os.Message msg) {
    		super.handleMessage(msg);
    		updateTime();
    	};
    };
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Post a runnable to avoid blocking the broadcast.
                mHandler.sendEmptyMessage(0);
            }
        };
	
	public LeftTimeTextView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public LeftTimeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
	}
	
	public void setLeftTimeAlarm( Alarm alarm ) {
		mAlarm = alarm;
		
		updateTime();
	}
	
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

         //Log.v("onAttachedToWindow " + this);

        if (mAttached) return;
        mAttached = true;

        if (mLive) {
            /* monitor time ticks, time changed, timezone */
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            getContext().registerReceiver(mIntentReceiver, filter);
            
            //Log.e("LeftTimeTextView registerReceiver");
        }

        //updateTime();
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (!mAttached) return;
        mAttached = false;

        if (mLive) {
        	//Log.e("LeftTimeTextView unregisterReceiver");
            getContext().unregisterReceiver(mIntentReceiver);
        }
    }
    
    private void updateTime() {
    	if ( mAlarm != null ) {
    		long timeInMillis = Alarms.calculateAlarm(mAlarm.hour, mAlarm.minutes, mAlarm.daysOfWeek, mAlarm.id).getTimeInMillis();
    		leftTime=timeInMillis;
    		if(AlarmReceiver.wakeupAlarmId==mAlarm.id&&!mAlarm.enabled)
    		{
    			Calendar now=Calendar.getInstance();
    			
    			if(now.get(Calendar.HOUR_OF_DAY)>=mAlarm.hour&&now.get(Calendar.MINUTE)>=(mAlarm.minutes))
    			{
    				AlarmReceiver.wakeupAlarmId=-1;
    				AlarmReceiver.is_wakeup_noalarm=false;
    			}
    		}
    		String toastText = AuroraSetAlarm.formatToast(mContext, timeInMillis);
    		//Log.e("---toastText = " + toastText);
    		setText(toastText);
    	}
    }
    
    private long leftTime;
    public long getLeftTime(){
    	return leftTime;
    }
    
    void setLive(boolean live) {
        mLive = live;
    }
    
    private void sendLeftTimeTextViewBroadcast( String action ) {
    	Intent intent = new Intent();
    	intent.setAction(action);

    	//发送 一个无序广播
    	mContext.sendBroadcast(intent);
    }

}
